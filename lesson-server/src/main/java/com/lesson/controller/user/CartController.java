package com.lesson.controller.user;

import com.lesson.dto.AddToCartDTO;
import com.lesson.result.Result;
import com.lesson.service.CartService;
import com.lesson.vo.CartItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@Api(tags = "购物车管理")
@Slf4j
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    /**
     * 添加到购物车
     */
    @PostMapping("/add")
    @ApiOperation("添加到购物车")
    public Result<String> addToCart(@RequestBody AddToCartDTO addToCartDTO) {
        try {
            boolean success = cartService.addToCart(addToCartDTO);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            log.error("添加到购物车失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户购物车列表
     */
    @GetMapping("/list")
    @ApiOperation("获取用户购物车列表")
    public Result<List<CartItemVO>> getCartList() {
        try {
            List<CartItemVO> cartItems = cartService.getUserCart();
            return Result.success(cartItems);
        } catch (Exception e) {
            log.error("获取购物车列表失败", e);
            return Result.error("获取购物车列表失败");
        }
    }
    
    /**
     * 从购物车移除
     */
    @DeleteMapping("/remove/{courseId}")
    @ApiOperation("从购物车移除")
    public Result<String> removeFromCart(@PathVariable Integer courseId) {
        try {
            boolean success = cartService.removeFromCart(courseId);
            if (success) {
                return Result.success("移除成功");
            } else {
                return Result.error("移除失败");
            }
        } catch (Exception e) {
            log.error("从购物车移除失败", e);
            return Result.error("移除失败");
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    @ApiOperation("清空购物车")
    public Result<String> clearCart() {
        try {
            boolean success = cartService.clearCart();
            if (success) {
                return Result.success("清空成功");
            } else {
                return Result.error("清空失败");
            }
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return Result.error("清空失败");
        }
    }
    
    /**
     * 检查课程是否在购物车中
     */
    @GetMapping("/check/{courseId}")
    @ApiOperation("检查课程是否在购物车中")
    public Result<Boolean> isInCart(@PathVariable Integer courseId) {
        try {
            boolean isInCart = cartService.isInCart(courseId);
            return Result.success(isInCart);
        } catch (Exception e) {
            log.error("检查购物车状态失败", e);
            return Result.error("检查失败");
        }
    }
} 