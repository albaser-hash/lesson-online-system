import request from '@/utils/request'

/**
 * 分页查询帖子
 * @param params
 */
export function listForumTopics(params) {
  return request({
    url: '/user/forum/list',
    method: 'get',
    params
  })
}

/**
 * 发布论坛
 * @param data
 */
export function publishTopic(data) {
  return request({
    url: '/user/forum/topics',
    method: 'post',
    data
  })
}

/**
 * 更新论坛主题
 * @param data
 */
export function updateTopic(data) {
  return request({
    url: '/user/forum/topics',
    method: 'put',
    data
  })
}

/**
 * 回复论坛
 * @param data
 */
export function replyToTopic(data) {
  return request({
    url: '/user/forum/reply',
    method: 'post',
    data
  })
}

/**
 * 获取帖子详情
 * @param topicId
 */
export function getTopicDetail(topicId) {
  return request({
    url: '/user/forum/detail',
    method: 'get',
    params: { topicId }
  })
}

/**
 * 删除帖子
 * @param id
 */
export function deleteTopic(id) {
  return request({
    url: `/user/forum/topics/${id}`,
    method: 'delete'
  })
}

/**
 * 删除回复
 * @param id
 */
export function deleteReply(id) {
  return request({
    url: `/user/forum/reply/${id}`,
    method: 'delete'
  })
}

/**
 * 更新回复
 * @param data
 */
export function updateReply(data) {
  return request({
    url: '/user/forum/reply',
    method: 'put',
    data
  })
}


