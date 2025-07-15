import request from '@/utils/request'

// 获取动态导航栏菜单
export function getNavMenu(userType) {
  return request({
    url: '/system/nav-menu',
    headers: {
      isToken: false
    },
    method: 'get',
    params: { userType }
  })
}
