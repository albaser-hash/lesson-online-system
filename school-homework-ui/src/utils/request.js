import axios from 'axios'
import { Notification, MessageBox, Message, Loading } from 'element-ui'
import store from '@/store'
import { getToken, setToken, getRefreshToken, setRefreshToken, removeToken, removeRefreshToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { tansParams, blobValidate } from "@/utils/edu"
import cache from '@/plugins/cache'
import { saveAs } from 'file-saver'

let downloadLoadingInstance
// 是否显示重新登录
export let isRelogin = { show: false }
let isRefreshing = false
let requests = []

// 移除默认的Content-Type设置，让每个请求根据需要设置
// axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: process.env.VUE_APP_BASE_API,
  // 超时
  timeout: 20000
})

// request拦截器
service.interceptors.request.use(config => {
  // 登录已过期强制锁，直接reject
  if (window.isForceLogout) {
    return Promise.reject(new Error('登录已过期，请重新登录'))
  }
  
  // 为普通请求设置Content-Type，文件上传请求不设置
  if (!config.headers['Content-Type'] && !(config.data instanceof FormData)) {
    config.headers['Content-Type'] = 'application/json;charset=utf-8'
  }
  
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  // 是否需要防止数据重复提交
  const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
  // 每次请求都用最新token
  const currentToken = getToken()
  if (currentToken && !isToken) {
    config.headers['online-token'] = currentToken // 按后端要求传递 token
  }
  // get请求映射params参数
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.params = {}
    config.url = url
  }
  if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
    const requestObj = {
      url: config.url,
      data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
      time: new Date().getTime()
    }
    const requestSize = Object.keys(JSON.stringify(requestObj)).length // 请求数据大小
    const limitSize = 5 * 1024 * 1024 // 限制存放数据5M
    if (requestSize >= limitSize) {
      console.warn(`[${config.url}]: ` + '请求数据大小超出允许的5M限制，无法进行防重复提交验证。')
      return config
    }
    const sessionObj = cache.session.getJSON('sessionObj')
    if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
      cache.session.setJSON('sessionObj', requestObj)
    } else {
      const s_url = sessionObj.url                  // 请求地址
      const s_data = sessionObj.data                // 请求数据
      const s_time = sessionObj.time                // 请求时间
      const interval = 1000                         // 间隔时间(ms)，小于此时间视为重复提交
      if (s_data === requestObj.data && requestObj.time - s_time < interval && s_url === requestObj.url) {
        const message = '数据正在处理，请勿重复提交'
        console.warn(`[${s_url}]: ` + message)
        return Promise.reject(new Error(message))
      } else {
        cache.session.setJSON('sessionObj', requestObj)
      }
    }
  }
  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(
  response => response,
  async error => {
    const { config, response } = error
    if (response && response.status === 401 && !config._retry) {
      config._retry = true
      if (!isRefreshing) {
        isRefreshing = true
        try {
          const res = await service.post('/user/refreshToken', { refreshToken: getRefreshToken() })
          const accessToken = res.data?.data?.accessToken
          setToken(accessToken)
          isRefreshing = false
          requests.forEach(cb => cb(accessToken))
          requests = []
          config.headers['online-token'] = accessToken
          return service(config)
        } catch (e) {
          isRefreshing = false
          removeToken()
          removeRefreshToken()
          store.dispatch('FedLogOut')
          // 清空防重复提交sessionObj
          try {
            const cache = require('@/plugins/cache').default
            cache.session.setJSON('sessionObj', null)
          } catch (e) {}
          window.isForceLogout = true
          MessageBox.confirm(
            '登录已过期，您可以取消停留在此页面，或重新登录',
            '系统提示',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            location.href = '/login'
          })
          return Promise.reject(e)
        }
      } else {
        return new Promise(resolve => {
          requests.push(token => {
            config.headers['online-token'] = getToken() // 用最新token
            resolve(service(config))
          })
        })
      }
    }
    return Promise.reject(error)
  }
)

// 通用下载方法
export function download(url, params, filename, config) {
  downloadLoadingInstance = Loading.service({ text: "正在下载数据，请稍候", spinner: "el-icon-loading", background: "rgba(0, 0, 0, 0.7)", })
  return service.post(url, params, {
    transformRequest: [(params) => { return tansParams(params) }],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
    ...config
  }).then(async (data) => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      Message.error(errMsg)
    }
    downloadLoadingInstance.close()
  }).catch((r) => {
    console.error(r)
    Message.error('下载文件出现错误，请联系管理员！')
    downloadLoadingInstance.close()
  })
}

export default service
