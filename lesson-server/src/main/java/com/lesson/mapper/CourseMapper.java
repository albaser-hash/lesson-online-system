package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.entity.Course;
import com.lesson.vo.MyCourseVO;
import com.lesson.vo.ScanCourseVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface CourseMapper extends BaseMapper<Course> {
    List<ScanCourseVO> selectCourseListWithSpecificFields();

    ScanCourseVO selectCourseById(Integer id);
    //查询所有收藏课程列表
    Page<ScanCourseVO> selectCourseByIds(Page<ScanCourseVO> page, List<Integer> courseIds);
    //查询教师课程列表
    List<ScanCourseVO> selectTeacherCourses(Integer teacherId);
    //分页查询课程列表（带教师名和分类名）
    Page<ScanCourseVO> selectCoursePageWithDetails(Page<ScanCourseVO> page, @Param("keyword") String keyword, @Param("categoryId") Integer categoryId);
    
    // 查询我的课程（分页）
    Page<MyCourseVO> selectMyCoursesPage(Page<MyCourseVO> page, @Param("courseIds") List<Integer> courseIds, @Param("userId") Integer userId);
    
    // 查询我的课程（不分页）
    List<MyCourseVO> selectMyCourses(@Param("courseIds") List<Integer> courseIds, @Param("userId") Integer userId);
    
    // 查询待审核课程列表
    List<ScanCourseVO> selectPendingCourses();
    
    // 查询所有课程（用于审核页面）
    List<ScanCourseVO> selectAllCoursesForAudit();
    
    // 查询所有课程（用于审核页面）- 分页版本
    Page<ScanCourseVO> selectAllCoursesForAuditWithPage(Page<ScanCourseVO> page);
    
    // 根据审核状态查询课程
    List<ScanCourseVO> selectCoursesByStatus(@Param("status") String status);
    
    // 根据审核状态查询课程 - 分页版本
    Page<ScanCourseVO> selectCoursesByStatusWithPage(Page<ScanCourseVO> page, @Param("status") String status);

    // 统计某状态的课程数量
    long countByStatus(@Param("status") String status);

    List<ScanCourseVO> selectTeacherTestCourses(Integer currentId);
}