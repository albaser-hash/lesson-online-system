package com.lesson.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)//id自增
    private Integer userId;

    private String userName;

    private String passWord;

    private String email;

    private String phone;

    private String userType;

    private String avatar;

    private java.sql.Timestamp registerTime;

    private String name; // 昵称，可空
    private Integer sex; // 性别，0=保密，1=男，2=女
    private String bio; // 个人简介，可空
}
