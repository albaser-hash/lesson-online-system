import request from '@/utils/request'

// 新增课程分类
export function addCourseCategory(data) {
  return request({
    url: '/course/category',
    method: 'post',
    data
  });
}

// 修改课程分类
export function updateCourseCategory(data) {
  return request({
    url: '/course/category',
    method: 'put',
    data
  });
}

// 删除课程分类
export function deleteCourseCategory(id) {
  return request({
    url: `/course/category/${id}`,
    method: 'delete'
  });
}

// 根据ID查询课程分类
export function getCourseCategoryById(id) {
  return request({
    url: `/course/category/${id}`,
    method: 'get'
  });
}

// 分页查询课程分类列表
export function listCourseCategories(params) {
  return request({
    url: '/course/category/list',
    method: 'get',
    params
  });
}


export function deleteCourser(id) {
  return request({
    url: `/course/${id}`,
    method: 'delete'
  })
}
