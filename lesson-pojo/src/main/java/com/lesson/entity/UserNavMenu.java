package com.lesson.entity;

import lombok.Data;

@Data
public class UserNavMenu {
    private Integer id;
    private String userType;   // GUEST/STUDENT/TEACHER/ADMIN
    private String menuName;
    private String menuPath;
    private Integer menuOrder;


} 