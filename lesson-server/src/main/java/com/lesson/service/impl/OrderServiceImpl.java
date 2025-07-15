package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.dto.CheckoutDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.CartItem;
import com.lesson.entity.Course;
import com.lesson.entity.Order;
import com.lesson.entity.User;
import com.lesson.mapper.CartMapper;
import com.lesson.mapper.CourseMapper;
import com.lesson.mapper.OrderMapper;
import com.lesson.mapper.UserMapper;
import com.lesson.result.PageResult;
import com.lesson.service.OrderService;
import com.lesson.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.lesson.context.BaseContext.getCurrentId;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(CheckoutDTO checkoutDTO) {
        Integer currentUserId = getCurrentId();
        // 校验用户类型，只允许学生下单
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("TEACHER".equalsIgnoreCase(user.getUserType()) || "ADMIN".equalsIgnoreCase(user.getUserType())) {
            throw new RuntimeException("只有学生可以下单，老师和管理员无法下单");
        }
        List<Integer> courseIds = checkoutDTO.getCourseIds();
        String paymentMethod = checkoutDTO.getPaymentMethod();
        
        if (courseIds == null || courseIds.isEmpty()) {
            throw new RuntimeException("请选择要购买的课程");
        }
        
        // 验证支付方式
        if (!"ALIPAY".equals(paymentMethod) && !"WECHAT".equals(paymentMethod)) {
            throw new RuntimeException("不支持的支付方式");
        }
        
        // 检查课程是否在购物车中
        for (Integer courseId : courseIds) {
            CartItem cartItem = cartMapper.selectByUserIdAndCourseId(currentUserId, courseId);
            if (cartItem == null) {
                throw new RuntimeException("课程不在购物车中，无法购买");
            }
        }
        
        // 生成订单ID
        String orderId = generateOrderId();
        
        // 计算总金额并创建订单
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Integer courseId : courseIds) {
            Course course = courseMapper.selectById(courseId);
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            if (!"APPROVED".equals(course.getAuditStatus())) {
                throw new RuntimeException("课程未审核通过，无法购买");
            }
            
            // 检查是否已购买
            Order existingOrder = orderMapper.selectByUserIdAndCourseId(currentUserId, courseId);
            if (existingOrder != null) {
                throw new RuntimeException("您已购买过该课程");
            }
            
            totalAmount = totalAmount.add(BigDecimal.valueOf(course.getPrice().doubleValue()));
            
            // 创建订单记录
            Order order = new Order();
            order.setOrderId(orderId + "_" + courseId); // 为每个课程创建单独的订单记录
            order.setUserId(currentUserId);
            order.setCourseId(courseId);
            order.setPaymentMethod(paymentMethod);
            order.setOrderAmount(course.getPrice());
            order.setOrderStatus("UNPAID");
            order.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
            
            orderMapper.insert(order);
        }
        
        log.info("创建订单成功 - 用户ID: {}, 订单ID: {}, 课程数量: {}, 总金额: {}", 
                currentUserId, orderId, courseIds.size(), totalAmount);
        
        return orderId;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(String orderId) {
        Integer currentUserId = getCurrentId();
        
        // 查找所有相关的订单记录
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Order::getOrderId, orderId + "_%")
                   .eq(Order::getUserId, currentUserId)
                   .eq(Order::getOrderStatus, "UNPAID");
        
        List<Order> orders = orderMapper.selectList(queryWrapper);
        
        if (orders.isEmpty()) {
            throw new RuntimeException("订单不存在或已支付");
        }
        
        // 模拟支付过程
        Timestamp payTime = Timestamp.valueOf(LocalDateTime.now());
        
        for (Order order : orders) {
            order.setOrderStatus("PAID");
            order.setPayTime(payTime);
            orderMapper.updateById(order);
            
            // 从购物车中移除已购买的课程
            cartMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, currentUserId)
                    .eq(CartItem::getCourseId, order.getCourseId()));
        }
        
        log.info("支付成功 - 用户ID: {}, 订单ID: {}, 支付时间: {}", currentUserId, orderId, payTime);
        
        return true;
    }
    
    @Override
    public PageResult getUserOrders(PageQueryDTO pageQueryDTO) {
        Integer currentUserId = getCurrentId();
        
        Page<OrderVO> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<OrderVO> pageResult = orderMapper.selectUserOrders(page, currentUserId);
        
        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
    }
    
    @Override
    public OrderVO getOrderDetail(String orderId) {
        Integer currentUserId = getCurrentId();
        
        OrderVO orderVO = orderMapper.selectOrderDetail(orderId);
        if (orderVO == null || !orderVO.getUserId().equals(currentUserId)) {
            throw new RuntimeException("订单不存在或无权限查看");
        }
        
        return orderVO;
    }
    
    @Override
    public boolean hasPurchased(Integer courseId) {
        Integer currentUserId = getCurrentId();
        return hasPurchased(currentUserId, courseId);
    }
    
    @Override
    public boolean hasPurchased(Integer userId, Integer courseId) {
        if (userId == null || courseId == null) return false;
        Order order = orderMapper.selectByUserIdAndCourseId(userId, courseId);
        return order != null;
    }
    
    /**
     * 生成订单ID
     */
    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8);
        return "ORDER" + timestamp + random;
    }
}
