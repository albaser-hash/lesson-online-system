package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Order implements Serializable {
    // 对应表中 order_id 字段，varchar 类型，长度 50，主键、非空
    @TableId(type = IdType.INPUT)
    private String orderId;
    // 对应 user_id 字段，int 类型，非空
    private Integer userId;
    // 对应 course_id 字段，int 类型，非空
    private Integer courseId;
    // 对应 payment_method 字段，enum 类型，非空，这里用 String 接收枚举值
    private String paymentMethod;
    // 对应 order_amount 字段，decimal(10,2) 类型，非空，用 BigDecimal 处理精度
    private double orderAmount;
    // 对应 order_status 字段，enum 类型，非空，用 String 接收枚举值
    private String orderStatus;
    // 对应 create_time 字段，timestamp 类型，默认 CURRENT_TIMESTAMP
    private Timestamp createTime;
    // 对应 pay_time 字段，timestamp 类型
    private Timestamp payTime;
}
