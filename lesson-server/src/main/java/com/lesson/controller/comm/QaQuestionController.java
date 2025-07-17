package com.lesson.controller.comm;

import com.lesson.annotation.CheckPermission;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.QaQuestion;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.QaQuestionService;
import com.lesson.vo.QaQuestionWithAnswersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qa/question")
@Api(tags = "问答问题管理")
public class QaQuestionController {
    @Autowired
    private QaQuestionService qaQuestionService;

    @PostMapping
    @ApiOperation("新增问答问题")
    public Result<String> add(@RequestBody QaQuestion qaQuestion) {
        qaQuestionService.add(qaQuestion);
        return Result.success("新增成功");
    }

    @PutMapping
    @ApiOperation("修改问答问题")
    @CheckPermission(resourceType = "qa_question", operation = "update")
    public Result<String> update(@RequestBody QaQuestion qaQuestion) {
        qaQuestionService.update(qaQuestion);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除问答问题")
    @CheckPermission(resourceType = "qa_question", operation = "delete")
    public Result<String> delete(@PathVariable Integer id) {
        qaQuestionService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取问答问题详情")
    public Result<QaQuestionWithAnswersVO> getById(@PathVariable Integer id) {
        return Result.success(qaQuestionService.getFullDetailById(id));
    }

    @GetMapping("/list")
    @ApiOperation("获取问答问题列表")
    public Result<PageResult> list(PageQueryDTO pageQueryDTO) {
        return Result.success(qaQuestionService.listWithUserInfo(pageQueryDTO));
    }

    @PostMapping("/setBestAnswer")
    @ApiOperation("设置最佳答案")
    @CheckPermission(resourceType = "qa_question", operation = "update")
    public Result<String> setBestAnswer(@RequestParam Integer questionId, @RequestParam Integer answerId) {
        qaQuestionService.setBestAnswer(questionId, answerId);
        return Result.success("设置成功");
    }
} 