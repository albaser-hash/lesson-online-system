import request from '@/utils/request'

// 购物车相关API
export function getCartList() {
  return request({
    url: '/cart/list',
    method: 'get'
  })
}

export function addToCart(data) {
  return request({
    url: '/cart/add',
    method: 'post',
    data
  })
}

export function removeFromCart(courseId) {
  return request({
    url: `/cart/remove/${courseId}`,
    method: 'delete'
  })
}

export function clearCart() {
  return request({
    url: '/cart/clear',
    method: 'delete'
  })
}

export function checkInCart(courseId) {
  return request({
    url: `/cart/check/${courseId}`,
    method: 'get'
  })
}

// 订单相关API
export function createOrder(data) {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

export function payOrder(orderId) {
  return request({
    url: `/order/pay/${orderId}`,
    method: 'post'
  })
}

export function getUserOrders(params) {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

export function getOrderDetail(orderId) {
  return request({
    url: `/order/detail/${orderId}`,
    method: 'get'
  })
}

export function checkPurchased(courseId) {
  return request({
    url: `/order/check/${courseId}`,
    method: 'get'
  })
} 