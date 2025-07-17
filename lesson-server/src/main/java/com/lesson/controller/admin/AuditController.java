package com.lesson.controller.admin;

import com.lesson.dto.AuditCourseDTO;
import com.lesson.dto.CreateChapterDTO;
import com.lesson.dto.CreateCourseDTO;
import com.lesson.dto.UpdateCourseCategoryDTO;
import com.lesson.dto.ToggleRecommendDTO;
import com.lesson.result.Result;
import com.lesson.result.PageResult;
import com.lesson.service.CourseService;
import com.lesson.vo.CreateChapterVO;
import com.lesson.vo.CreateCourseVO;
import com.lesson.vo.ScanCourseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Api(tags = "课程审核管理")
@Slf4j
public class AuditController {
    @Autowired
    private CourseService courseService;
    
    /**
     * 获取待审核课程列表
     */
    @GetMapping("/audit/pending")
    @ApiOperation("获取待审核课程列表")
    public Result<List<ScanCourseVO>> getPendingCourses() {
        List<ScanCourseVO> courses = courseService.getPendingCourses();
        return Result.success(courses);
    }
    
    /**
     * 根据审核状态获取课程列表（分页）
     */
    @GetMapping("/audit/courses")
    @ApiOperation("根据审核状态获取课程列表（分页）")
    public Result<PageResult> getCoursesByStatus(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        PageResult pageResult = courseService.getCoursesByStatusWithPage(status, page, pageSize);
        return Result.success(pageResult);
    }
    
    /**
     * 审核课程
     */
    @PostMapping("/audit")
    @ApiOperation("审核课程")
    public Result auditCourse(@RequestBody AuditCourseDTO auditCourseDTO) {
        if(courseService.AuditCourse(auditCourseDTO))
            return Result.success("审核成功");
        else
            return Result.error("审核失败");
    }

    /**
     * 修改课程分类
     */
    @PostMapping("/audit/category")
    @ApiOperation("修改课程分类")
    public Result updateCourseCategory(@RequestBody UpdateCourseCategoryDTO dto) {
        boolean ok = courseService.updateCourseCategory(dto);
        if (ok) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败，请确保课程已审核通过");
        }
    }

    /**
     * 推荐/取消推荐课程（加/减50分）
     */
    @PostMapping("/audit/recommend")
    @ApiOperation("推荐/取消推荐课程")
    public Result toggleRecommend(@RequestBody ToggleRecommendDTO dto) {
        boolean ok = courseService.toggleRecommend(dto);
        if (ok) {
            return Result.success("操作成功");
        } else {
            return Result.error("操作失败，请确保课程已审核通过");
        }
    }
}
