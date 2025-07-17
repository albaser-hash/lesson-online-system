package com.lesson.service;

import com.lesson.dto.CreateExamDTO;
import com.lesson.dto.SubmitExamDTO;
import com.lesson.dto.ReviewDTO;
import com.lesson.dto.UpdateExamDTO;
import com.lesson.result.Result;
import com.lesson.vo.ExamDetailVO;
import com.lesson.vo.ExamVO;

import java.util.List;

public interface ExamService {
    
    // 创建考试
    Result<ExamVO> createExam(CreateExamDTO createExamDTO);
    
    // 发布考试（通知学生）
    Result<String> publishExam(Integer examId);
    
    // 获取学生的考试列表
    List<ExamVO> getStudentExams(Integer userId);
    
    // 获取教师的考试列表
    List<ExamVO> getTeacherExams(Integer teacherId);
    
    // 获取考试详情（学生参加考试）
    ExamDetailVO getExamDetailForStudent(Integer examId, Integer userId);
    
    // 获取考试详情（教师查看）
    ExamDetailVO getExamDetailForTeacher(Integer examId);
    
    // 提交考试
    Result<String> submitExam(SubmitExamDTO submitExamDTO, Integer userId);
    
    // 获取考试结果
    ExamDetailVO getExamResult(Integer examId, Integer userId);
    
    // 获取考试统计信息
    Result<Object> getExamStatistics(Integer examId);
    
    // 更新考试状态（定时任务调用）
    void updateExamStatus();
    
    /**
     * 批量将某个老师的所有考试时间设置为现在开始，1小时后结束
     */
    void setAllExamsToNowByTeacherId(Long teacherId);
    
    /**
     * 删除指定老师的所有考试
     * @param teacherId 老师ID
     * @return 删除的考试数量
     */
    int deleteAllExamsByTeacherId(Long teacherId);

    Result<?> reviewPaper(ReviewDTO reviewDTO);
    
    // 编辑考试
    Result<ExamVO> updateExam(UpdateExamDTO updateExamDTO);
    
    // 删除考试
    Result<String> deleteExam(Integer examId);
}
