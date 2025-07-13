package com.lesson.controller.student;

import com.lesson.dto.LearnProcessDTO;
import com.lesson.result.Result;
import com.lesson.service.CourseService;
import com.lesson.service.LearnService;
import com.lesson.service.OrderService;
import com.lesson.vo.DetailCourseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/learn")
@Api(tags = "课程进度")
@Slf4j
public class LearnProcessController {
    @Autowired
    private LearnService learnService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private OrderService orderService;
    /*
     * 课程进度
     */
    @PostMapping("/process")
    @ApiOperation("课程进度")
    public Result<String> LearnProcess(@RequestBody LearnProcessDTO learnProcessDTO) {//创建课程
            String orderId = learnService.getLearnProcess(learnProcessDTO);
            return Result.success(orderId+"%");
    }

    @PostMapping("/complete")
    @ApiOperation("章节学习完成")
    public Result<String> completeChapter(@RequestBody LearnProcessDTO learnProcessDTO) {
        Integer userId = com.lesson.context.BaseContext.getCurrentId();
        Integer courseId = learnProcessDTO.getCourseId();
        Integer chapterId = learnProcessDTO.getChapterId();
        learnService.completeChapter(userId, courseId, chapterId);
        return Result.success("章节学习进度已更新");
    }
    
    @GetMapping("/course/{courseId}/detail")
    @ApiOperation("获取已购买课程的完整详情（包含章节内容）")
    public Result<DetailCourseVO> getPurchasedCourseDetail(@PathVariable("courseId") Integer courseId) {
        Integer userId = com.lesson.context.BaseContext.getCurrentId();
        
        // 检查用户是否已购买该课程
        boolean hasPurchased = orderService.hasPurchased(userId, courseId);
        if (!hasPurchased) {
            return Result.error("您尚未购买该课程，无法查看完整内容");
        }
        
        // 获取课程完整详情
        DetailCourseVO courseDetail = courseService.getCourseDetail(courseId);
        if (courseDetail == null) {
            return Result.error("课程不存在");
        }
        
        return Result.success(courseDetail);
    }
}
