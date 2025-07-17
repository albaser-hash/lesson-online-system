package com.lesson.controller.comm;

import com.lesson.annotation.CheckPermission;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.QaAnswer;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.QaAnswerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qa/answer")
@Api(tags = "问答答案管理")
public class QaAnswerController {
    @Autowired
    private QaAnswerService qaAnswerService;

    @PostMapping
    @ApiOperation("新增问答答案")
    public Result<String> add(@RequestBody QaAnswer qaAnswer) {
        qaAnswerService.add(qaAnswer);
        return Result.success("新增成功");
    }

    @PutMapping
    @ApiOperation("修改问答答案")
    @CheckPermission(resourceType = "qa_answer", operation = "update")
    public Result<String> update(@RequestBody QaAnswer qaAnswer) {
        qaAnswerService.update(qaAnswer);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除问答答案")
    @CheckPermission(resourceType = "qa_answer", operation = "delete")
    public Result<String> delete(@PathVariable Integer id) {
        qaAnswerService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取问答答案")
    public Result<QaAnswer> getById(@PathVariable Integer id) {
        return Result.success(qaAnswerService.getById(id));
    }

    @GetMapping("/list")
    @ApiOperation("获取问答答案列表")
    public Result<PageResult> list(PageQueryDTO pageQueryDTO) {
        return Result.success(qaAnswerService.list(pageQueryDTO));
    }
} 