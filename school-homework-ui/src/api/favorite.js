import request from '@/utils/request'

// 收藏课程
export function favoriteCourse(courseId) {
  return request({
    url: `/user/favorite/${courseId}`,
    method: 'get'
  })
}

export function favoriteCourseStatus(courseId,userId) {
  return request({
    url: `/user/favorite/${courseId}/${userId}`,
    method: 'get'
  })
}

// 获取收藏课程列表
export function getFavoriteList(params) {
  return request({
    url: '/user/favorite/list',
    method: 'post',
    data: params
  })
}
