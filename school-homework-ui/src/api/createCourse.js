import request from '@/utils/request'
import { getToken } from '@/utils/auth'

// 获取课程分类（分页/全部）
export function listCourseCategories(params) {
  return request({
    url: '/course/category/list',
    method: 'get',
    params
  })
}

// 上传图片到OSS
export async function uploadImage(file) {
  try {
    // 检查用户登录状态
    const token = getToken()

    // 验证文件对象
    if (!file || !(file instanceof File || file instanceof Blob)) {
      throw new Error('无效的文件对象')
    }

    // 只传 file 字段
    const formData = new FormData()
    formData.append('file', file)

    const response = await request({
      url: '/admin/common/upload-image',
      method: 'post',
      data: formData
      // 不要设置 Content-Type
    })

    // 检查响应结构
    const responseData = response.data || response

    if (responseData.code !== 200) {
      throw new Error(responseData.msg || '文件上传失败')
    }

    // 返回图片URL
    return {
      code: 200,
      data: responseData.data
    }

  } catch (error) {
    console.error('图片上传失败:', error)
    return {
      code: 500,
      msg: error.message || '图片上传失败'
    }
  }
}

// 创建课程
export function createCourse(data) {
  return request({
    url: '/course/create',
    method: 'post',
    data
  })
}

// 更新课程
export function updateCourse(data) {
  return request({
    url: '/course/update',
    method: 'put',
    data
  })
}

// 获取课程详情
export function getCourseDetail(courseId) {
  return request({
    url: `/course/detail/${courseId}`,
    method: 'get'
  })
}

// 获取教师课程列表
export function getTeacherCourses(data) {
  return request({
    url: '/course/teacher/all',
    method: 'post',
    data
  })
}
export function getTeacherTestCourses(data) {
  return request({
    url: '/course/teacher/test',
    method: 'post',
    data
  })
}

// 创建章节
export function createChapter(data) {
  return request({
    url: '/course/create/chapter',
    method: 'post',
    data
  })
}

// 获取课程列表（分页）
export function getCourseList(data) {
  return request({
    url: '/course/list',
    method: 'post',
    data
  })
}
