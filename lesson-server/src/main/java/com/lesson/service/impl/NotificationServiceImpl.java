package com.lesson.service.impl;

import com.lesson.entity.Notification;
import com.lesson.mapper.NotificationMapper;
import com.lesson.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void add(Notification notification) {
        notificationMapper.insert(notification);
    }

    @Override
    public void update(Notification notification) {
        notificationMapper.updateById(notification);
    }

    @Override
    public void delete(Integer notificationId) {
        notificationMapper.deleteById(notificationId);
    }

    @Override
    public Notification getById(Integer notificationId) {
        return notificationMapper.selectById(notificationId);
    }

    @Override
    public List<Notification> listByUserId(Integer userId) {
        return notificationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>().eq("user_id", userId)
        );
    }

    @Override
    public int countUnread(Integer userId) {
        return notificationMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>()
                .eq("user_id", userId)
                .eq("is_read", false)
        ).intValue();
    }

    @Override
    public void markRead(Integer notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n != null && !Boolean.TRUE.equals(n.getIsRead())) {
            n.setIsRead(true);
            notificationMapper.updateById(n);
        }
    }

    @Override
    public void markAllRead(Integer userId) {
        Notification update = new Notification();
        update.setIsRead(true);
        notificationMapper.update(update,
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>()
                .eq("user_id", userId)
                .eq("is_read", false)
        );
    }

    @Override
    public IPage<Notification> pageByUserId(Integer userId, int page, int pageSize) {
        Page<Notification> p = new Page<>(page, pageSize);
        return notificationMapper.selectPage(p,
            new QueryWrapper<Notification>().eq("user_id", userId).orderByDesc("create_time")
        );
    }

    @Override
    public void deleteAllByUserId(Integer userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("user_id", userId);
        notificationMapper.delete(wrapper);
    }
} 