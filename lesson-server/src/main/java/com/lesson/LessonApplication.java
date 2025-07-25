package com.lesson;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching//开启缓存
@EnableScheduling//开启定时任务调度
@MapperScan("com.lesson.mapper")
public class LessonApplication {
    public static void main(String[] args) {
        SpringApplication.run(LessonApplication.class, args);
        log.info("server started");
    }
}
