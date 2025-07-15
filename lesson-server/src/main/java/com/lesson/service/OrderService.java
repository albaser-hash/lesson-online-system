package com.lesson.service;

import com.lesson.dto.CheckoutDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.result.PageResult;
import com.lesson.vo.OrderVO;

public interface OrderService {
    
    // 创建订单（结算）
    String createOrder(CheckoutDTO checkoutDTO);
    
    // 模拟支付
    boolean payOrder(String orderId);
    
    // 获取用户订单列表
    PageResult getUserOrders(PageQueryDTO pageQueryDTO);
    
    // 获取订单详情
    OrderVO getOrderDetail(String orderId);
    
    // 检查用户是否已购买课程
    boolean hasPurchased(Integer courseId);

    /**
     * 检查指定用户是否已购买指定课程
     */
    boolean hasPurchased(Integer userId, Integer courseId);
}
