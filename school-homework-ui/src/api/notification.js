import request from '@/utils/request'

// 获取用户所有通知
export function getNotificationList(userId, page = 1, pageSize = 10) {
  return request({
    url: `/user/notification/list/${userId}`,
    method: 'get',
    params: { page, pageSize }
  })
}

// 获取未读通知数量
export function getUnreadCount(userId) {
  return request({
    url: `/user/notification/unread/count/${userId}`,
    method: 'get'
  })
}

// 标记单条通知为已读
export function markNotificationRead(notificationId) {
  return request({
    url: `/user/notification/read/${notificationId}`,
    method: 'post'
  })
}

// 全部标记为已读
export function markAllNotificationsRead(userId) {
  return request({
    url: `/user/notification/readAll/${userId}`,
    method: 'post'
  })
}

// 删除通知
export function deleteNotification(notificationId) {
  return request({
    url: `/user/notification/${notificationId}`,
    method: 'delete'
  })
}

// 删除所有通知
export function deleteAllNotifications() {
  return request.delete(`/user/notification/all`)
}
