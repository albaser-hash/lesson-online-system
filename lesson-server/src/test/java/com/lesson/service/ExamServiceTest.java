package com.lesson.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import com.lesson.mapper.ExamPaperMapper;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {"com.lesson.service"})
public class ExamServiceTest {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Test
    public void testSetAllExamsToNowByTeacherId() {
        Long teacherId = 4L; //TODO.



    // 你可以改成你想测试的老师ID
        examService.setAllExamsToNowByTeacherId(teacherId);
        System.out.println("已将老师ID=" + teacherId + "的所有考试时间设置为现在开始+1小时后结束");
    }

    @Test
    public void testDeleteAllPapersByUserId() {
        Long userId = 1L; // TODO: 修改为你要删除答卷的学生ID
        int deleted = examPaperMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.lesson.entity.ExamPaper>().eq("user_id", userId));
        System.out.println("已删除学生ID=" + userId + " 的所有答卷，删除数量：" + deleted);
    }

    @Test
    public void testDeleteAllExamsByTeacherId() {
        Long teacherId = 4L; // TODO: 修改为你要删除考试的老师ID
        int deleted = examService.deleteAllExamsByTeacherId(teacherId);
        System.out.println("已删除老师ID=" + teacherId + " 的所有考试，删除数量：" + deleted);
    }
}
