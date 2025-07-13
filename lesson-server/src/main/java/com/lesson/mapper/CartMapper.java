package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.CartItem;
import com.lesson.vo.CartItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper extends BaseMapper<CartItem> {
    
    // 查询用户的购物车列表（包含课程详情）
    List<CartItemVO> selectUserCartItems(@Param("userId") Integer userId);
    
    // 检查课程是否已在购物车中
    CartItem selectByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);
} 