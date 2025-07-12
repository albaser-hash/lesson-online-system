package com.lesson.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    private Integer userId;
    private String userName;
    private String avatar;
    private String token;
    private String name;
    private String sex;
    private String bio;
    private String refreshToken;

}
