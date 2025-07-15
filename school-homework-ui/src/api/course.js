import request from '@/utils/request'

// 获取课程列表
export function getCourseList(params) {
  return request({
    url: '/course/scan',
    method: 'get',
    params
  })
}

// 获取课程详情
export function getCourseDetail(id) {
  return request({
    url: `/course/scan/${id}`,
    method: 'get'
  })
}

// 删除课程
export function deleteCourse(courseId) {
  return request({
    url: `/course/${courseId}`,
    method: 'delete'
  })
}
