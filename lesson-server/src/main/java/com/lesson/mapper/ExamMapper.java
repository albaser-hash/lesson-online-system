package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.Exam;
import com.lesson.vo.ExamVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ExamMapper extends BaseMapper<Exam> {
    
    // 获取学生的考试列表
    List<ExamVO> selectStudentExams(@Param("userId") Integer userId);
    
    // 获取教师的考试列表
    List<ExamVO> selectTeacherExams(@Param("teacherId") Integer teacherId);
    
    // 获取课程的所有考试
    List<ExamVO> selectCourseExams(@Param("courseId") Integer courseId);

    /**
     * 批量更新某个老师的所有考试时间
     */
    void updateExamsTimeByTeacherId(@Param("teacherId") Long teacherId,
                                    @Param("startTime") Date startTime,
                                    @Param("endTime") Date endTime);
}
