package com.lesson.controller.user;

import com.lesson.dto.PageQueryDTO;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(tags = "收藏和查看收藏")
@Slf4j

public class FavoriteAllController {
    @Autowired
    private CourseService  courseService;
    /*
    收藏课程
     */
    @GetMapping("/favorite/{courseId}")
    @ApiOperation("收藏课程")
    public Result<String> favoriteCourse(@PathVariable("courseId") Integer courseId) {//创建课程
        if(courseService.favoriteCourse(courseId))
            return Result.success("收藏成功");
        else
            return Result.error("收藏失败");
    }

    /*
   收藏课程
    */
    @GetMapping("/favorite/{courseId}/{userId}")
    @ApiOperation("收藏课程状态")
    public Result<String> favoriteCourseStatus(@PathVariable("courseId") Integer courseId,@PathVariable("userId") Integer userId) {//创建课程
     return  courseService.favoriteCourseStatus(courseId,userId);

    }



    /*
    获取收藏课程
     */
    @PostMapping("/favorite/list")
    @ApiOperation("获取收藏课程")
    public Result<PageResult> GetFavoriteCourse(@RequestBody PageQueryDTO pageQueryDTO) {//创建课程
        PageResult pageResult=courseService.getfavoriteCourse(pageQueryDTO);
        return Result.success(pageResult);
    }

}
