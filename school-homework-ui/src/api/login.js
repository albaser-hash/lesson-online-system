import request from '@/utils/request'

// 登录方法
export function login({ userName, passWord }) {
  return request({
    url: '/user/login',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { userName, passWord }
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/user/getUserInfo',
    method: 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    'url': '/user/logout',
    'method': 'post'
  })
}

// 注册方法
export function register({ userName, passWord, email, phone, userType, name, sex, bio }) {
  return request({
    url: '/user/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: { userName, passWord, email, phone, userType, name, sex, bio }
  })
}


