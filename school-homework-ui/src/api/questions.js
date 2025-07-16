import request from '@/utils/request'

// 新增问题
export function addQuestion(data) {
  return request({
    url: '/qa/question',
    method: 'post',
    data
  })
}

// 修改问题
export function updateQuestion(data) {
  return request({
    url: '/qa/question',
    method: 'put',
    data
  })
}

// 删除问题
export function deleteQuestion(id) {
  return request({
    url: `/qa/question/${id}`,
    method: 'delete'
  })
}

// 获取问题详情
export function getQuestionById(id) {
  return request({
    url: `/qa/question/${id}`,
    method: 'get'
  })
}

// 分页获取问题列表
export function getQuestionList(params) {
  return request({
    url: '/qa/question/list',
    method: 'get',
    params
  })
}

// 设置最佳回答
export function setBestAnswer(questionId, answerId) {
  return request({
    url: '/qa/question/setBestAnswer',
    method: 'post',
    params: { questionId, answerId }
  })
}

