package com.lesson.controller.system;

import com.lesson.entity.UserNavMenu;
import com.lesson.service.NavMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/system/nav-menu")
@Api(tags = "导航菜单管理")
public class NavMenuController {
    @Autowired
    private NavMenuService navMenuService;

    @GetMapping
    @ApiOperation("获取导航菜单")
    public List<UserNavMenu> getNavMenu(@RequestParam(required = false) String userType) {
        if (userType == null || userType.isEmpty()) {
            userType = "STUDENT";
        }
        return navMenuService.getMenusByUserType(userType);
    }

    /**
     * 清除指定用户类型的导航菜单缓存
     */
    @DeleteMapping("/cache")
    @ApiOperation("清除指定用户类型的导航菜单缓存")
    public String clearNavMenuCache(@RequestParam String userType) {
        navMenuService.clearNavMenuCache(userType);
        return "清除缓存成功";
    }

    /**
     * 清除所有导航菜单缓存
     */
    @DeleteMapping("/cache/all")
    @ApiOperation("清除所有导航菜单缓存")
    public String clearAllNavMenuCache() {
        navMenuService.clearAllNavMenuCache();
        return "清除所有缓存成功";
    }
} 