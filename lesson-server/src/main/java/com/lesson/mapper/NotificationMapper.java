package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
 
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
} 