package com.lesson.controller.student;

import com.lesson.dto.AddToCartDTO;
import com.lesson.dto.CheckoutDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.CartService;
import com.lesson.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/student/course")
@Api(tags = "学生课程管理")
@Slf4j
public class PayCourseController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderService orderService;
    
    /*
     * 添加到购物车
     */
    @PostMapping("/add-to-cart")
    @ApiOperation("添加课程到购物车")
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
    
    /*
     * 直接购买课程（单课程购买）
     */
    @PostMapping("/buy")
    @ApiOperation("直接购买课程")
    public Result<String> buyCourse(@RequestBody AddToCartDTO addToCartDTO) {
        try {
            // 先添加到购物车
            boolean addSuccess = cartService.addToCart(addToCartDTO);
            if (!addSuccess) {
                return Result.error("添加课程失败");
            }
            
            // 创建订单
            CheckoutDTO checkoutDTO = new CheckoutDTO();
            checkoutDTO.setCourseIds(Arrays.asList(addToCartDTO.getCourseId()));
            checkoutDTO.setPaymentMethod("ALIPAY"); // 默认支付宝
            
            String orderId = orderService.createOrder(checkoutDTO);
            
            // 模拟支付
            boolean paySuccess = orderService.payOrder(orderId);
            if (paySuccess) {
                return Result.success("购买成功");
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            log.error("购买课程失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /*
     * 获取我的课程（已购买的课程）
     */
    @GetMapping("/my-courses")
    @ApiOperation("获取我的课程")
    public Result<PageResult> getMyCourses(PageQueryDTO pageQueryDTO) {
        try {
            PageResult pageResult = orderService.getUserOrders(pageQueryDTO);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取我的课程失败", e);
            return Result.error("获取我的课程失败");
        }
    }
    
    /*
     * 检查是否已购买课程
     */
    @GetMapping("/check-purchased/{courseId}")
    @ApiOperation("检查是否已购买课程")
    public Result<Boolean> checkPurchased(@PathVariable Integer courseId) {
        try {
            boolean hasPurchased = orderService.hasPurchased(courseId);
            return Result.success(hasPurchased);
        } catch (Exception e) {
            log.error("检查购买状态失败", e);
            return Result.error("检查失败");
        }
    }
}
