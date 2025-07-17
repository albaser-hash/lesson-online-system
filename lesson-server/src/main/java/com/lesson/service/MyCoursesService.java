package com.lesson.service;

import com.lesson.dto.PageQueryDTO;
import com.lesson.result.PageResult;
import com.lesson.vo.MyCoursesStatsVO;
import com.lesson.vo.MyCourseVO;

import java.util.List;

public interface MyCoursesService {
    /**
     * 获取学习统计信息
     */
    MyCoursesStatsVO getStudyStats();
    
    /**
     * 获取我的课程列表（分页）
     */
    PageResult getMyCourses(PageQueryDTO pageQueryDTO);
    
    /**
     * 获取我的所有课程（不分页）
     */
    List<MyCourseVO> getAllMyCourses();
} 