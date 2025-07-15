package com.lesson.controller.user;

import com.lesson.dto.CheckoutDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.OrderService;
import com.lesson.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Api(tags = "订单管理")
@Slf4j
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单（结算）
     */
    @PostMapping("/create")
    @ApiOperation("创建订单（结算）")
    public Result<String> createOrder(@RequestBody CheckoutDTO checkoutDTO) {
        try {
            String orderId = orderService.createOrder(checkoutDTO);
            return Result.success(orderId);
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/pay/{orderId}")
    @ApiOperation("支付订单")
    public Result<String> payOrder(@PathVariable String orderId) {
        try {
            boolean success = orderService.payOrder(orderId);
            if (success) {
                return Result.success("支付成功");
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            log.error("支付订单失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping("/list")
    @ApiOperation("获取用户订单列表")
    public Result<PageResult> getUserOrders(PageQueryDTO pageQueryDTO) {
        try {
            PageResult pageResult = orderService.getUserOrders(pageQueryDTO);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return Result.error("获取订单列表失败");
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{orderId}")
    @ApiOperation("获取订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable String orderId) {
        try {
            OrderVO orderVO = orderService.getOrderDetail(orderId);
            return Result.success(orderVO);
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查用户是否已购买课程
     */
    @GetMapping("/check/{courseId}")
    @ApiOperation("检查用户是否已购买课程")
    public Result<Boolean> hasPurchased(@PathVariable Integer courseId) {
        try {
            boolean hasPurchased = orderService.hasPurchased(courseId);
            return Result.success(hasPurchased);
        } catch (Exception e) {
            log.error("检查购买状态失败", e);
            return Result.error("检查失败");
        }
    }
} 