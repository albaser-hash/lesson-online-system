package com.lesson.service;


import com.lesson.dto.*;
import com.lesson.entity.Course;
import com.lesson.result.PageResult;
import com.lesson.result.Result;

import com.lesson.vo.*;

import java.util.List;

public interface CourseService {
    //获取课程
    List<ScanCourseVO> getCourseList();

    /**
     * 分页查询课程列表
     */
    PageResult getCourseList(PageQueryDTO pageQueryDTO);
    //获取详细课程
    DetailCourseVO getCourseDetail(Integer id);

    /**
     * 获取课程扫描详情（用于浏览，不包含敏感内容）
     */
    CourseScanDetailVO getCourseScanDetail(Integer id);

    //创建课程
    CreateCourseVO CreateCourse(CreateCourseDTO createCourseDTO);
    //更新课程
    UpdateCourseVO updateCourse(UpdateCourseDTO updateCourseDTO);
    //创建章节
    CreateChapterVO CreateChapter(CreateChapterDTO createChapterDTO);

    boolean AuditCourse(AuditCourseDTO auditCourseDTO);
    //收藏课程
    boolean favoriteCourse(Integer courseId);

    //获取收藏课程
    PageResult getfavoriteCourse(PageQueryDTO pageQueryDTO);
    //获取教师课程
    PageResult getTeacherCourse(PageQueryDTO pageQueryDTO);

    /**
     * 删除课程（仅在没有用户购买的情况下）
     * @param courseId 课程ID
     * @return 删除结果
     */
    boolean deleteCourse(Integer courseId);

    /**
     * 获取课程章节列表
     * @param courseId 课程ID
     * @return 章节列表
     */
    List<ChapterVO> getChapterList(Integer courseId);

    /**
     * 获取章节详情
     * @param chapterId 章节ID
     * @return 章节详情
     */
    ChapterVO getChapterDetail(Integer chapterId);

    /**
     * 更新章节
     * @param updateChapterDTO 更新章节DTO
     * @return 更新结果
     */
    boolean updateChapter(UpdateChapterDTO updateChapterDTO);

    /**
     * 删除章节
     * @param chapterId 章节ID
     * @return 删除结果
     */
    boolean deleteChapter(Integer chapterId);

    Result<String> favoriteCourseStatus(Integer courseId, Integer userId);

    /**
     * 获取待审核课程列表
     * @return 待审核课程列表
     */
    List<ScanCourseVO> getPendingCourses();

    /**
     * 根据审核状态获取课程列表
     */
    List<ScanCourseVO> getCoursesByStatus(String status);

    /**
     * 根据审核状态获取课程列表（分页）
     */
    PageResult getCoursesByStatusWithPage(String status, Integer page, Integer pageSize);

    /**
     * 修改课程分类
     */
    boolean updateCourseCategory(UpdateCourseCategoryDTO dto);

    /**
     * 推荐/取消推荐课程（加/减50分）
     */
    boolean toggleRecommend(ToggleRecommendDTO dto);

    PageResult getTeacherTestCourse(PageQueryDTO pageQueryDTO);
}
