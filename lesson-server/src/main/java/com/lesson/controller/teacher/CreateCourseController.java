package com.lesson.controller.teacher;

import com.lesson.dto.CreateChapterDTO;
import com.lesson.dto.CreateCourseDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.dto.UpdateCourseDTO;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.CourseService;
import com.lesson.vo.CreateChapterVO;
import com.lesson.vo.CreateCourseVO;
import com.lesson.vo.DetailCourseVO;
import com.lesson.vo.ScanCourseVO;
import com.lesson.vo.UpdateCourseVO;
import com.lesson.vo.ChapterVO;
import com.lesson.dto.UpdateChapterDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
@Api(tags = "创建课程课程")
@Slf4j

public class CreateCourseController {
    @Autowired
    private CourseService courseService;
    /*
     * 创建课程
     */
    @PostMapping("/create")
    @ApiOperation("创建课程")
    public Result<CreateCourseVO> CreateCourse(@RequestBody CreateCourseDTO createCourseDTO) {//创建课程
        CreateCourseVO createcoursevo = courseService.CreateCourse(createCourseDTO);
        return Result.success(createcoursevo);
    }

    @PostMapping("/create/chapter")
    @ApiOperation("创建章节")
    public Result<CreateChapterVO> CreateChapter(@RequestBody CreateChapterDTO createChapterDTO) {//创建课程
        CreateChapterVO createchaptervo = courseService.CreateChapter(createChapterDTO);
        return Result.success(createchaptervo);
    }
    
    @PutMapping("/update")
    @ApiOperation("更新课程")
    public Result<UpdateCourseVO> updateCourse(@RequestBody UpdateCourseDTO updateCourseDTO) {
        UpdateCourseVO updateCourseVO = courseService.updateCourse(updateCourseDTO);
        return Result.success(updateCourseVO);
    }
    
    @GetMapping("/detail/{courseId}")
    @ApiOperation("获取课程详情")
    public Result<DetailCourseVO> getCourseDetail(@PathVariable Integer courseId) {
        DetailCourseVO detailCourseVO = courseService.getCourseDetail(courseId);
        return Result.success(detailCourseVO);
    }
    
    /*
    老师查看已创建课程列表
     */
    @PostMapping("/teacher/all")
    @ApiOperation("获取我的课程")
    public Result<PageResult> getTeacherCourse(@RequestBody PageQueryDTO pageQueryDTO) {//创建课程
        PageResult pageResult=courseService.getTeacherCourse(pageQueryDTO);
        return Result.success(pageResult);
    }


    /*
 老师查看已创建课程列表
  */
    @PostMapping("/teacher/test")
    @ApiOperation("获取我的考试课程")
    public Result<PageResult> gGetTeacherTestCourse(@RequestBody PageQueryDTO pageQueryDTO) {//创建课程
        PageResult pageResult=courseService.getTeacherTestCourse(pageQueryDTO);
        return Result.success(pageResult);
    }
  
    /*
    删除课程
     */
    @DeleteMapping("/{courseId}")
    @ApiOperation("删除课程")
    public Result<String> deleteCourse(@PathVariable Integer courseId) {
        boolean result = courseService.deleteCourse(courseId);
        if (result) {
            return Result.success("课程删除成功");
        } else {
            return Result.error("课程删除失败");
        }
    }
    
    /*
    获取课程章节列表
     */
    @GetMapping("/{courseId}/chapters")
    @ApiOperation("获取课程章节列表")
    public Result<List<ChapterVO>> getChapterList(@PathVariable Integer courseId) {
        List<ChapterVO> chapters = courseService.getChapterList(courseId);
        return Result.success(chapters);
    }
    
    /*
    获取章节详情
     */
    @GetMapping("/chapter/{chapterId}")
    @ApiOperation("获取章节详情")
    public Result<ChapterVO> getChapterDetail(@PathVariable Integer chapterId) {
        ChapterVO chapter = courseService.getChapterDetail(chapterId);
        return Result.success(chapter);
    }
    
    /*
    更新章节
     */
    @PutMapping("/chapter")
    @ApiOperation("更新章节")
    public Result<String> updateChapter(@RequestBody UpdateChapterDTO updateChapterDTO) {
        boolean result = courseService.updateChapter(updateChapterDTO);
        if (result) {
            return Result.success("章节更新成功");
        } else {
            return Result.error("章节更新失败");
        }
    }
    
    /*
    删除章节
     */
    @DeleteMapping("/chapter/{chapterId}")
    @ApiOperation("删除章节")
    public Result<String> deleteChapter(@PathVariable Integer chapterId) {
        boolean result = courseService.deleteChapter(chapterId);
        if (result) {
            return Result.success("章节删除成功");
        } else {
            return Result.error("章节删除失败");
        }
    }
}
