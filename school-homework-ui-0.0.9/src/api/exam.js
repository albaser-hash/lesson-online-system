import request from '@/utils/request'

// 教师端API

// 获取考试列表
export function getExamList() {
  return request({
    url: '/exam/teacher/list',
    method: 'get'
  })
}

// 创建考试
export function createExam(data) {
  return request({
    url: '/exam/create',
    method: 'post',
    data
  })
}

// 获取考试详情
export function getExamDetail(examId) {
  return request({
    url: `/exam/teacher/detail/${examId}`,
    method: 'get'
  })
}

// 发布考试
export function publishExam(examId) {
  return request({
    url: `/exam/publish/${examId}`,
    method: 'post'
  })
}

// 编辑考试
export function updateExam(examId, data) {
  return request({
    url: `/exam/update/${examId}`,
    method: 'put',
    data
  })
}

// 删除考试
export function deleteExam(examId) {
  return request({
    url: `/exam/delete/${examId}`,
    method: 'delete'
  })
}

// 获取考试统计
export function getExamStatistics(examId) {
  return request({
    url: `/exam/statistics/${examId}`,
    method: 'get'
  })
}

// 获取待批改试卷列表
export function getReviewPapers(examId, isReviewed = 0) {
  return request({
    url: '/exam/papers',
    method: 'get',
    params: { examId, isReviewed }
  })
}

// 获取单份试卷详情
export function getPaperDetail(paperId) {
  return request({
    url: `/exam/paper/${paperId}`,
    method: 'get'
  })
}

// 提交批改
export function submitReview(data) {
  return request({
    url: '/exam/review',
    method: 'post',
    data
  })
}

// 学生端API

// 获取学生考试列表
export function getStudentExams() {
  return request({
    url: '/exam/student/list',
    method: 'get'
  })
}

// 获取学生考试详情
export function getStudentExamDetail(examId) {
  return request({
    url: `/exam/detail/${examId}`,
    method: 'get'
  })
}

// 提交考试
export function submitExamAnswers(data) {
  return request({
    url: '/exam/submit',
    method: 'post',
    data
  })
}

// 获取考试结果
export function getExamResult(examId) {
  return request({
    url: `/exam/result/${examId}`,
    method: 'get'
  })
}

// 获取考试题目
export function getExamQuestions(examId) {
  return request({
    url: '/teacher/exam/questions',
    method: 'get',
    params: { examId }
  })
}

// 批量保存考试题目
export function saveExamQuestions(examId, questions) {
  return request({
    url: '/teacher/exam/questions/save',
    method: 'post',
    data: { examId, questions }
  })
}

// 清空考试题目
export function clearExamQuestions(examId) {
  return request({
    url: '/teacher/exam/questions/clear',
    method: 'post',
    data: { examId }
  })
}

// 按课程ID获取题库题目
export function getCourseQuestions(courseId) {
  return request({
    url: '/teacher/exam/questions/course/questions',
    method: 'get',
    params: { courseId }
  })
}