import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)


// 公共路由
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/user/login.vue'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/user/register.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/common/404.vue'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/common/401.vue'),
    hidden: true
  },
  {
    path: '/',
    component: () => import('@/views/index.vue'),
    name: 'Index',
    meta: { title: '首页' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/profile.vue')
  }
]

export default new Router({
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
