package com.lesson.controller.teacher;

import com.lesson.dto.CreateExamDTO;
import com.lesson.dto.SubmitExamDTO;
import com.lesson.dto.ReviewDTO;
import com.lesson.dto.UpdateExamDTO;
import com.lesson.result.Result;
import com.lesson.service.ExamService;
import com.lesson.vo.ExamDetailVO;
import com.lesson.vo.ExamVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lesson.entity.ExamPaper;
import com.lesson.entity.ExamQuestion;
import com.lesson.mapper.ExamPaperMapper;
import com.lesson.mapper.ExamQuestionMapper;

import java.util.List;
import java.util.Map;

import static com.lesson.context.BaseContext.getCurrentId;

@RestController
@RequestMapping("/exam")
@Api(tags = "考试管理")
@Slf4j
public class ExamController {
    
    @Autowired
    private ExamService examService;
    
    @Autowired
    private ExamPaperMapper examPaperMapper;
    
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    
    @PostMapping("/create")
    @ApiOperation("创建考试")
    public Result<ExamVO> createExam(@RequestBody CreateExamDTO createExamDTO) {
        return examService.createExam(createExamDTO);
    }
    
    @PostMapping("/publish/{examId}")
    @ApiOperation("发布考试")
    public Result<String> publishExam(@PathVariable Integer examId) {
        return examService.publishExam(examId);
    }
    
    @GetMapping("/student/list")
    @ApiOperation("获取学生考试列表")
    public Result<List<ExamVO>> getStudentExams() {
        // 从JWT中获取用户ID
        Integer userId = getCurrentId();
        List<ExamVO> exams = examService.getStudentExams(userId);
       return Result.success(exams);
    }
    
    @GetMapping("/teacher/list")
    @ApiOperation("获取教师考试列表")
    public Result<List<ExamVO>> getTeacherExams() {
        Integer teacherId = getCurrentId();
        log.info("当前教师ID: {}", teacherId);
        List<ExamVO> exams = examService.getTeacherExams(teacherId);
        return Result.success(exams);
    }
    
    @GetMapping("/detail/{examId}")
    @ApiOperation("获取考试详情（学生）")
    public Result<ExamDetailVO> getExamDetail(@PathVariable Integer examId) {
        Integer userId = getCurrentId();
        ExamDetailVO examDetail = examService.getExamDetailForStudent(examId, userId);
        if (examDetail == null) {
            return Result.error("考试不存在或无权限访问");
        }
        return Result.success(examDetail);
    }
    
    @GetMapping("/teacher/detail/{examId}")
    @ApiOperation("获取考试详情（教师）")
    public Result<ExamDetailVO> getTeacherExamDetail(@PathVariable Integer examId) {
        ExamDetailVO examDetail = examService.getExamDetailForTeacher(examId);
        if (examDetail == null) {
            return Result.error("考试不存在或无权限访问");
        }
        return Result.success(examDetail);
    }
    
    @PostMapping("/submit")
    @ApiOperation("提交考试")
    public Result<String> submitExam(@RequestBody SubmitExamDTO submitExamDTO) {
        Integer userId = getCurrentId();
        return examService.submitExam(submitExamDTO, userId);
    }
    
    @GetMapping("/result/{examId}")
    @ApiOperation("获取考试结果")
    public Result<ExamDetailVO> getExamResult(@PathVariable Integer examId) {
        Integer userId = getCurrentId();
        ExamDetailVO result = examService.getExamResult(examId, userId);
        if (result == null) {
            return Result.error("未找到考试结果");
        }
        return Result.success(result);
    }
    
    @GetMapping("/statistics/{examId}")
    @ApiOperation("获取考试统计信息")
    public Result<Object> getExamStatistics(@PathVariable Integer examId) {
        return examService.getExamStatistics(examId);
    }
    
    @PostMapping("/review")
    @ApiOperation("老师批改主观题")
    public Result<?> reviewPaper(@RequestBody ReviewDTO reviewDTO) {
        return examService.reviewPaper(reviewDTO);
    }
    
    @GetMapping("/papers")
    @ApiOperation("获取考试下所有试卷（可筛选是否已批改）")
    public Result<List<ExamPaper>> getPapers(@RequestParam Integer examId, @RequestParam(required = false) Integer isReviewed) {
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamPaper::getExamId, examId);
        if (isReviewed != null) {
            wrapper.eq(ExamPaper::getIsReviewed, isReviewed == 1);
        }
        List<ExamPaper> papers = examPaperMapper.selectList(wrapper);
        return Result.success(papers);
    }

    @GetMapping("/paper/{paperId}")
    @ApiOperation("获取单份试卷详情（含题目和学生答案）")
    public Result<Object> getPaperDetail(@PathVariable Integer paperId) {
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null) return Result.error("试卷不存在");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<Integer, String> userAnswers = objectMapper.readValue(paper.getAnswer(), Map.class);
            List<ExamQuestion> questions = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, paper.getExamId())
            );
            List<Map<String, Object>> questionList = new java.util.ArrayList<>();
            for (ExamQuestion q : questions) {
                Map<String, Object> qMap = new java.util.HashMap<>();
                qMap.put("questionId", q.getQuestionId());
                qMap.put("questionType", q.getQuestionType());
                qMap.put("questionContent", q.getQuestionContent());
                qMap.put("score", q.getScore());
                qMap.put("correctAnswer", q.getCorrectAnswer());
                String ans = userAnswers.get(q.getQuestionId());
                if (ans == null) ans = userAnswers.get(String.valueOf(q.getQuestionId()));
                qMap.put("userAnswer", ans == null ? "" : ans);
                questionList.add(qMap);
            }
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("paperId", paper.getPaperId());
            result.put("userId", paper.getUserId());
            result.put("questions", questionList);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("解析试卷失败");
        }
    }

    @PutMapping("/update/{examId}")
    @ApiOperation("编辑考试")
    public Result<ExamVO> updateExam(@PathVariable Integer examId, @RequestBody UpdateExamDTO updateExamDTO) {
        updateExamDTO.setExamId(examId);
        return examService.updateExam(updateExamDTO);
    }
    
    @DeleteMapping("/delete/{examId}")
    @ApiOperation("删除考试")
    public Result<String> deleteExam(@PathVariable Integer examId) {
        return examService.deleteExam(examId);
    }
} 