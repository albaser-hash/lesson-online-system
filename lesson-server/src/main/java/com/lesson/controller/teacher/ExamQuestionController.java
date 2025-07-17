package com.lesson.controller.teacher;

import com.lesson.dto.ExamQuestionSaveDTO;
import com.lesson.entity.ExamQuestion;
import com.lesson.service.ExamQuestionService;
import com.lesson.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher/exam/questions")
@Api(tags = "考试题目管理")
public class ExamQuestionController {
    @Autowired
    private ExamQuestionService examQuestionService;

    @GetMapping
    @ApiOperation("获取考试题目列表")
    public Result<List<ExamQuestion>> getQuestions(@RequestParam Integer examId) {
        return Result.success(examQuestionService.getQuestionsByExamId(examId));
    }

    @PostMapping("/save")
    @ApiOperation("保存考试题目")
    public Result<?> saveQuestions(@RequestBody ExamQuestionSaveDTO dto) {
        examQuestionService.saveQuestions(dto.getExamId(), dto.getQuestions());
        return Result.success();
    }

    @PostMapping("/clear")
    @ApiOperation("清空考试题目")
    public Result<?> clearQuestions(@RequestBody Map<String, Integer> params) {
        Integer examId = params.get("examId");
        examQuestionService.clearQuestions(examId);
        return Result.success();
    }

    @GetMapping("/course/questions")
    @ApiOperation("根据课程ID获取题目列表")  //教师端导入题目调用
    public Result<List<ExamQuestion>> getQuestionsByCourseId(@RequestParam Integer courseId) {
        return Result.success(examQuestionService.getQuestionsByCourseId(courseId));
    }
} 