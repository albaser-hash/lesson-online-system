package com.lesson.service;

import com.lesson.dto.AddToCartDTO;
import com.lesson.vo.CartItemVO;

import java.util.List;

public interface CartService {
    
    // 添加到购物车
    boolean addToCart(AddToCartDTO addToCartDTO);
    
    // 获取用户购物车列表
    List<CartItemVO> getUserCart();
    
    // 从购物车移除
    boolean removeFromCart(Integer courseId);
    
    // 清空购物车
    boolean clearCart();
    
    // 检查课程是否在购物车中
    boolean isInCart(Integer courseId);
} 