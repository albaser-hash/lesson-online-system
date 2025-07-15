import request from '@/utils/request'

// 获取待审核课程列表
export function getPendingCourses() {
  return request({
    url: '/admin/audit/pending',
    method: 'get'
  })
}

// 根据状态获取课程列表
export function getCoursesByStatus(status = 'PENDING', page = 1, pageSize = 10) {
  return request({
    url: `/admin/audit/courses?status=${status}&page=${page}&pageSize=${pageSize}`,
    method: 'get'
  })
}

// 审核课程
export function auditCourse(data) {
  return request({
    url: '/admin/audit',
    method: 'post',
    data
  })
}

// 修改课程分类
export function updateCourseCategory(data) {
  return request({
    url: '/admin/audit/category',
    method: 'post',
    data
  })
}

// 推荐/取消推荐课程
export function toggleRecommend(data) {
  return request({
    url: '/admin/audit/recommend',
    method: 'post',
    data
  })
}
