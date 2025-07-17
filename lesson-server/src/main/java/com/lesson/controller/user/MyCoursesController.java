package com.lesson.controller.user;

import com.lesson.dto.PageQueryDTO;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.MyCoursesService;
import com.lesson.vo.MyCoursesStatsVO;
import com.lesson.vo.MyCourseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/my-courses")
@Api(tags = "我的课程相关接口")
@Slf4j
public class MyCoursesController {
    
    @Autowired
    private MyCoursesService myCoursesService;
    
    @GetMapping("/stats")
    @ApiOperation("获取学习统计信息")
    public Result<MyCoursesStatsVO> getStudyStats() {
        MyCoursesStatsVO stats = myCoursesService.getStudyStats();
        return Result.success(stats);
    }
    
    @PostMapping("/list")
    @ApiOperation("获取我的课程列表")
    public Result<PageResult> getMyCourses(@RequestBody PageQueryDTO pageQueryDTO) {
        PageResult pageResult = myCoursesService.getMyCourses(pageQueryDTO);
        return Result.success(pageResult);
    }
    
    @GetMapping("/list/all")
    @ApiOperation("获取我的所有课程（不分页）")
    public Result<List<MyCourseVO>> getAllMyCourses() {
        List<MyCourseVO> courses = myCoursesService.getAllMyCourses();
        return Result.success(courses);
    }
} 