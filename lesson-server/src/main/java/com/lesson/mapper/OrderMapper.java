package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.entity.Order;
import com.lesson.vo.OrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {
    
    // 查询用户的订单列表（包含课程详情）
    Page<OrderVO> selectUserOrders(Page<OrderVO> page, @Param("userId") Integer userId);
    
    // 查询单个订单详情
    OrderVO selectOrderDetail(@Param("orderId") String orderId);
    
    // 检查用户是否已购买课程
    Order selectByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);
}