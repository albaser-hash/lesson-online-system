import request from '@/utils/request'

// 新增回答
export function addAnswer(data) {
  return request({
    url: '/qa/answer',
    method: 'post',
    data
  })
}

// 修改回答
export function updateAnswer(data) {
  return request({
    url: '/qa/answer',
    method: 'put',
    data
  })
}

// 删除回答
export function deleteAnswer(id) {
  return request({
    url: `/qa/answer/${id}`,
    method: 'delete'
  })
}

// 根据ID获取回答
export function getAnswerById(id) {
  return request({
    url: `/qa/answer/${id}`,
    method: 'get'
  })
}

// 分页查询回答列表
export function listAnswers(params) {
  return request({
    url: '/qa/answer/list',
    method: 'get',
    params
  })
}


