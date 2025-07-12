package com.lesson.mapper;

import com.lesson.entity.UserNavMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserNavMenuMapper {
    @Select("SELECT id, user_type AS userType, menu_name AS menuName, menu_path AS menuPath, menu_order AS menuOrder FROM user_nav_menu WHERE user_type = #{userType} ORDER BY menu_order ASC")
    List<UserNavMenu> selectMenusByUserType(String userType);
} 