package com.lesson.controller.comm;

import com.lesson.context.BaseContext;
import com.lesson.entity.Notification;
import com.lesson.result.Result;
import com.lesson.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lesson.annotation.CheckPermission;

@RestController
@RequestMapping("/user/notification")
@Api(tags = "通知")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    @ApiOperation("新增通知")
    public Result<String> add(@RequestBody Notification notification) {
        notificationService.add(notification);
        return Result.success("新增成功");
    }

    @PutMapping
    @ApiOperation("修改通知")
    public Result<String> update(@RequestBody Notification notification) {
        notificationService.update(notification);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除通知")
    @CheckPermission(resourceType = "notification", operation = "delete")
    public Result<String> delete(@PathVariable Integer id) {
        notificationService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("查询单条通知")
    public Result<Notification> getById(@PathVariable Integer id) {
        return Result.success(notificationService.getById(id));
    }

    @GetMapping("/list/{userId}")
    @ApiOperation("分页查询用户所有通知")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<Notification>> listByUserId(
        @PathVariable Integer userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(notificationService.pageByUserId(userId, page, pageSize));
    }

    @GetMapping("/unread/count/{userId}")
    @ApiOperation("获取未读通知数量")
    public Result<Integer> getUnreadCount(@PathVariable Integer userId) {
        return Result.success(notificationService.countUnread(userId));
    }

    @PostMapping("/read/{id}")
    @ApiOperation("标记单条通知为已读")
    public Result<String> markRead(@PathVariable Integer id) {
        notificationService.markRead(id);
        return Result.success("标记已读成功");
    }

    @PostMapping("/readAll/{userId}")
    @ApiOperation("全部标记为已读")
    public Result<String> markAllRead(@PathVariable Integer userId) {
        notificationService.markAllRead(userId);
        return Result.success("全部标记已读成功");
    }

    @DeleteMapping("/all")
    @ApiOperation("删除用户全部通知")
    public Result<String> deleteAll() {
        notificationService.deleteAllByUserId(BaseContext.getCurrentId());
        return Result.success("全部删除成功");
    }
} 