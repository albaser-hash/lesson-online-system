import request from '@/utils/request'

// 获取课程列表
export function getChaptersList(courseId) {
  return request({
    url: `/course/${courseId}/chapters`,
    method: 'get',
  })
}

// 获取课程详情
export function getChaptersDetail(id) {
  return request({
    url: `/course/chapter/${id}`,
    method: 'get'
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

export function updateChapter(data) {
  return request({
    url: '/course/chapter',
    method: 'put',
    data
  });
}

export function deleteChapter(chapterId) {
  return request({
    url: `/course/chapter/${chapterId}`,
    method: 'delete'
  });
}

// 上报学习进度
export function reportChapterProgress({ courseId, chapterId }) {
  return request({
    url: '/student/learn/complete',
    method: 'post',
    data: { courseId, chapterId }
  })
}

// 获取章节文档分页内容
export function getChapterDocPages(chapterId, userId) {
  return request({
    url: '/admin/common/chapter/getDocPages',
    method: 'get',
    params: { chapterId, userId }
  })
}

// 获取已购买课程的完整详情（包含章节内容）
export function getPurchasedCourseDetail(courseId) {
  return request({
    url: `/student/learn/course/${courseId}/detail`,
    method: 'get'
  })
}

// 获取文档签名下载URL（5分钟有效）
export function getDocDownloadUrl(chapterId, userId) {
  return request({
    url: '/admin/common/api/doc/download',
    method: 'get',
    params: { chapterId, userId }
  })
}
