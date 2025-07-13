package com.lesson.controller.comm;

import com.lesson.dto.PageQueryDTO;
import com.lesson.result.Result;
import com.lesson.result.PageResult;
import com.lesson.service.CourseService;
import com.lesson.vo.CourseScanDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
@Api(tags = "浏览课程")
@Slf4j

public class ScanSourseController {
    @Autowired
    private CourseService courseService;
    /*
     * 获取课程列表
     */
    @GetMapping("/scan")
    @ApiOperation("浏览课程列表（分页）")
    public Result<PageResult> getCourseList(PageQueryDTO pageQueryDTO) {
        PageResult pageResult = courseService.getCourseList(pageQueryDTO);
        return Result.success(pageResult);
    }
    /*
     * 获取课程详情
     */
    @GetMapping("/scan/{id}")
    @ApiOperation("浏览课程详情")
    public Result<CourseScanDetailVO> getCourseDetail(@PathVariable("id") Integer id) {//查询详情
        CourseScanDetailVO courseDetail = courseService.getCourseScanDetail(id);
        return Result.success(courseDetail);
    }




}
