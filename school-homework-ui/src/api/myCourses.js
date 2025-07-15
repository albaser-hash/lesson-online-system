import request from '@/utils/request'

// 获取学习统计信息
export function getStudyStats() {
  return request({
    url: '/user/my-courses/stats',
    method: 'get'
  })
}

// 获取我的课程列表（分页）
export function getMyCourses(data) {
  return request({
    url: '/user/my-courses/list',
    method: 'post',
    data
  })
}

// 获取我的所有课程（不分页）
export function getAllMyCourses() {
  return request({
    url: '/user/my-courses/list/all',
    method: 'get'
  })
} 