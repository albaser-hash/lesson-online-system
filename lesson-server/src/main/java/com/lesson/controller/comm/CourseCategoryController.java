package com.lesson.controller.comm;

import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.CourseCategory;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course/category")
@Api(tags = "课程分类管理")
public class CourseCategoryController {
    @Autowired
    private CourseCategoryService courseCategoryService;

    @PostMapping
    @ApiOperation("新增课程分类")
    public Result<String> add(@RequestBody CourseCategory category) {
        courseCategoryService.add(category);
        return Result.success("新增成功");
    }

    @PutMapping
    @ApiOperation("修改课程分类")
    public Result<String> update(@RequestBody CourseCategory category) {
        courseCategoryService.update(category);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除课程分类")
    public Result<String> delete(@PathVariable Integer id) {
        courseCategoryService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取课程分类")
    public Result<CourseCategory> getById(@PathVariable Integer id) {
        return Result.success(courseCategoryService.getById(id));
    }

    @GetMapping("/list")
    @ApiOperation("获取课程分类列表")
    public Result<PageResult> list(PageQueryDTO pageQueryDTO) {
        return Result.success(courseCategoryService.list(pageQueryDTO));
    }

    /**
     * 清除课程分类缓存
     */
    @DeleteMapping("/cache")
    @ApiOperation("清除课程分类缓存")
    @CacheEvict(value = "courseCategory", allEntries = true)
    public Result<String> clearCache() {
        return Result.success("清除缓存成功");
    }
} 