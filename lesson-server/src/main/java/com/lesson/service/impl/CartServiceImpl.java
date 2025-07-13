package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lesson.dto.AddToCartDTO;
import com.lesson.entity.CartItem;
import com.lesson.entity.Course;
import com.lesson.mapper.CartMapper;
import com.lesson.mapper.CourseMapper;
import com.lesson.service.CartService;
import com.lesson.vo.CartItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.lesson.context.BaseContext.getCurrentId;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Override
    public boolean addToCart(AddToCartDTO addToCartDTO) {
        Integer currentUserId = getCurrentId();
        
        // 检查用户是否登录
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录，请先登录");
        }
        
        Integer courseId = addToCartDTO.getCourseId();
        
        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 检查课程是否已审核通过
        if (!"APPROVED".equals(course.getAuditStatus())) {
            throw new RuntimeException("课程未审核通过，无法购买");
        }
        
        // 检查是否已在购物车中
        CartItem existingItem = cartMapper.selectByUserIdAndCourseId(currentUserId, courseId);
        if (existingItem != null) {
            throw new RuntimeException("课程已在购物车中");
        }
        
        // 添加到购物车
        CartItem cartItem = new CartItem();
        cartItem.setUserId(currentUserId);
        cartItem.setCourseId(courseId);
        cartItem.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        
        int result = cartMapper.insert(cartItem);
        
       // log.info("添加到购物车成功 - 用户ID: {}, 课程ID: {}, 结果: {}", currentUserId, courseId, result > 0);
        
        return result > 0;
    }
    
    @Override
    public List<CartItemVO> getUserCart() {
        Integer currentUserId = getCurrentId();
        
        // 检查用户是否登录
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录，请先登录");
        }
        
        return cartMapper.selectUserCartItems(currentUserId);
    }
    
    @Override
    public boolean removeFromCart(Integer courseId) {
        Integer currentUserId = getCurrentId();
        
        // 检查用户是否登录
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录，请先登录");
        }
        
        LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItem::getUserId, currentUserId)
                   .eq(CartItem::getCourseId, courseId);
        
        int result = cartMapper.delete(queryWrapper);
        
      //  log.info("从购物车移除成功 - 用户ID: {}, 课程ID: {}, 结果: {}", currentUserId, courseId, result > 0);
        
        return result > 0;
    }
    
    @Override
    public boolean clearCart() {
        Integer currentUserId = getCurrentId();
        
        // 检查用户是否登录
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录，请先登录");
        }
        
        LambdaQueryWrapper<CartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CartItem::getUserId, currentUserId);
        
        int result = cartMapper.delete(queryWrapper);
        
       // log.info("清空购物车成功 - 用户ID: {}, 结果: {}", currentUserId, result > 0);
        
        return result > 0;
    }
    
    @Override
    public boolean isInCart(Integer courseId) {
        Integer currentUserId = getCurrentId();
        
        // 检查用户是否登录
        if (currentUserId == null) {
            return false; // 未登录用户认为不在购物车中
        }
        
        CartItem cartItem = cartMapper.selectByUserIdAndCourseId(currentUserId, courseId);
        return cartItem != null;
    }
} 