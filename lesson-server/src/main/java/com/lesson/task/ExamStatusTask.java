package com.lesson.task;

import com.lesson.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 考试状态更新定时任务
 */
@Component
@Slf4j
public class ExamStatusTask {

    @Autowired
    private ExamService examService;

    /**
     * 每分钟检查一次考试状态
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void updateExamStatus() {
        log.info("开始更新考试状态...");
        try {
            examService.updateExamStatus();
            log.info("考试状态更新完成");
        } catch (Exception e) {
            log.error("更新考试状态失败", e);
        }
    }
} 