import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import store from '@/store'
import { MessageBox } from 'element-ui'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/']

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const hasToken = getToken()
  
  if (hasToken) {
    // 检查是否需要获取用户信息
    if (!store.state.user.userType) {
      try {
        await store.dispatch('GetInfo')
      } catch (error) {
        console.error('获取用户信息失败:', error)
        if (whiteList.includes(to.path)) {
          // 白名单页面允许游客访问，不跳转
          next()
        } else {
          next('/')
        }
        NProgress.done()
        return
      }
    }
    
    // 检查路由权限
    if (to.meta && to.meta.requiresAuth) {
      const userType = store.state.user.userType
      const requiredRole = to.meta.role
      
      if (requiredRole && userType !== requiredRole.toUpperCase()) {
        console.log(`权限不足: 需要${requiredRole}角色，当前用户类型: ${userType}`)
        next('/')
        NProgress.done()
        return
      }
    }
    
    next()
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      MessageBox.confirm(
        '您还未登录，是否前往登录页？',
        '提示',
        {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
        NProgress.done()
      }).catch(() => {
        NProgress.done()
        // 不跳转，停留在当前页面
      })
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
