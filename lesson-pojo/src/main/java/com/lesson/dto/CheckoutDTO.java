package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CheckoutDTO implements Serializable {
    
    private List<Integer> courseIds;
    
    private String paymentMethod; // ALIPAY, WECHAT
} 