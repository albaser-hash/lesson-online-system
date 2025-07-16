import request from '@/utils/request'

export function getUserName(userId) {
  return request({
    url: `/user/name/${userId}`,
    method: 'get'
  })
}