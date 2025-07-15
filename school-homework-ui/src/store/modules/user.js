import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken, getRefreshToken, setRefreshToken, removeRefreshToken } from '@/utils/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import defAva from '@/assets/images/profile.jpg'

const user = {
  state: {
    token: getToken(),
    id: '',
    userName: '',
    name: '',
    avatar: '',
    userType: '',
    email: '',
    phone: '',
    sex: '',
    bio: ''
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_ID: (state, id) => {
      state.id = id
    },
    SET_USERNAME: (state, userName) => {
      state.userName = userName
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_EMAIL: (state, email) => {
      state.email = email
    },
    SET_PHONE: (state, phone) => {
      state.phone = phone
    },
    SET_USERTYPE: (state, userType) => {
      state.userType = userType
    },
    SET_SEX: (state, sex) => {
      state.sex = (sex === undefined || sex === null || sex === '') ? 0 : Number(sex)
    },
    SET_BIO: (state, bio) => {
      state.bio = bio
    }
  },

  actions: {
    // 登录
    Login({ commit, dispatch }, userInfo) {
      const { userName, passWord } = userInfo
      return new Promise((resolve, reject) => {
        login({ userName, passWord }).then(res => {

          // 检查是否登录成功
          if (res.data.code !== 200) {
            // 登录失败，直接 reject 错误信息
            reject(new Error(res.msg))
            return
          }
          // 登录成功，获取 token 和 id
          const { token, userId, refreshToken } = res.data.data
          setToken(token)
          setRefreshToken(refreshToken)
          commit('SET_TOKEN', token)
          commit('SET_ID', userId)
          // 登录后立即获取用户信息
          dispatch('GetInfo').then(userRes => {
            // 用户信息获取完成后再触发菜单获取
            dispatch('forceRefreshMenus', null, { root: true })
            resolve(userRes)
          }).catch(error => {
            reject(error)
          })
        }).catch(error => {
          console.error('[Login] 登录失败:', error)
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          const user = res.data.data
          let avatar = ''
          if (user && user.avatar) {
            avatar = user.avatar
          }
          if (!isHttp(avatar)) {
            avatar = (!avatar || isEmpty(avatar)) ? defAva : process.env.VUE_APP_BASE_API + avatar
          }
          if (user && user.userType) {
            commit('SET_USERTYPE', user.userType)
          } else {
            commit('SET_USERTYPE', '')
          }
          commit('SET_ID', user && user.userId ? user.userId : '')
          commit('SET_USERNAME', user && user.userName ? user.userName : '')
          commit('SET_NAME', user && user.name ? user.name : '')
          commit('SET_AVATAR', avatar)
          commit('SET_EMAIL', user && user.email ? user.email : '')
          commit('SET_PHONE', user && user.phone ? user.phone : '')
          commit('SET_SEX', user && user.sex ? user.sex : 0)
          commit('SET_BIO', user && user.bio ? user.bio : '')
          setTimeout(() => {
          }, 0)
          resolve(res)
        }).catch(error => {
          console.error('[GetInfo] 获取用户信息失败:', error)
          reject(error)
        })
      })
    },
    // 恢复FedLogOut，包含所有清理逻辑和清空sessionObj，返回Promise
    FedLogOut({ commit }) {
      return new Promise(resolve => {
        commit('SET_TOKEN', '')
        commit('SET_USERTYPE', '')
        commit('SET_ID', '')
        commit('SET_NAME', '')
        commit('SET_AVATAR', '')
        commit('SET_EMAIL', '')
        commit('SET_PHONE', '')
        commit('SET_SEX', '')
        commit('SET_BIO', '')
        removeToken()
        removeRefreshToken()
        try {
          const cache = require('@/plugins/cache').default
          cache.session.setJSON('sessionObj', null)
        } catch (e) {}
        resolve()
      })
    }
  }
}

export default user
