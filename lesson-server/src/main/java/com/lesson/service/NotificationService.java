package com.lesson.service;

import com.lesson.entity.Notification;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface NotificationService {
    void add(Notification notification);
    void update(Notification notification);
    void delete(Integer notificationId);
    Notification getById(Integer notificationId);
    List<Notification> listByUserId(Integer userId);
    int countUnread(Integer userId);
    void markRead(Integer notificationId);
    void markAllRead(Integer userId);
    IPage<Notification> pageByUserId(Integer userId, int page, int pageSize);
    void deleteAllByUserId(Integer userId);
} 