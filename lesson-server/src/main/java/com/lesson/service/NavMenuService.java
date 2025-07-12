package com.lesson.service;

import com.lesson.entity.UserNavMenu;
import com.lesson.mapper.UserNavMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NavMenuService {
    @Autowired
    private UserNavMenuMapper userNavMenuMapper;
    /**
     * 根据用户类型获取导航菜单列表
     * 此方法使用Spring的缓存机制，缓存键为用户类型，以优化性能和减少数据库访问
     * 当方法返回的结果为null时，不会缓存，以避免缓存空值
     *
     * @param userType 用户类型，用于确定导航菜单的类型
     * @return 导航菜单列表，如果用户类型没有对应的菜单，则返回空列表或null
     */
    @Cacheable(value = "navMenu", key = "#userType", unless = "#result == null")
    public List<UserNavMenu> getMenusByUserType(String userType) {
        return userNavMenuMapper.selectMenusByUserType(userType);
    }
    /**
     * 清除指定用户类型的导航菜单缓存
     */
    public void clearNavMenuCache(String userType) {
        // 方法体可以为空，注解会自动清除缓存
    }

    /**
     * 清除所有导航菜单缓存
     */
    public void clearAllNavMenuCache() {
        // 方法体可以为空，注解会自动清除缓存
    }
} 