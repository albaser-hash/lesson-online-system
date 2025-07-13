package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.dto.AuditCourseDTO;
import com.lesson.dto.CreateChapterDTO;
import com.lesson.dto.CreateCourseDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.dto.UpdateCourseDTO;
import com.lesson.entity.Chapter;
import com.lesson.entity.Course;
import com.lesson.entity.Favorite;
import com.lesson.entity.Order;
import com.lesson.mapper.ChapterMapper;
import com.lesson.mapper.CourseMapper;
import com.lesson.mapper.FavoriteMapper;
import com.lesson.mapper.OrderMapper;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.CourseService;
import com.lesson.vo.CreateChapterVO;
import com.lesson.vo.CreateCourseVO;
import com.lesson.vo.DetailCourseVO;
import com.lesson.vo.ScanCourseVO;
import com.lesson.vo.UpdateCourseVO;
import com.lesson.vo.ChapterVO;
import com.lesson.vo.CourseScanDetailVO;
import com.lesson.vo.ChapterPreviewVO;
import com.lesson.dto.UpdateChapterDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.lesson.exception.ServiceException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lesson.context.BaseContext.getCurrentId;

@Service
@Slf4j
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private ChapterMapper chapterMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private OrderMapper orderMapper;

    /*
     * 查询全部课程
     */
    @Override
    public List<ScanCourseVO> getCourseList() {
        // 执行查询
        List<ScanCourseVO> courses = courseMapper.selectCourseListWithSpecificFields();
        return courses;
    }

    /**
     * 根据课程ID获取课程详细信息
     *
     * 此方法首先通过课程ID从数据库中查询课程基本信息，然后查询与该课程相关的所有章节信息，
     * 最后将这些信息封装到一个 DetailedCourseVO 对象中返回这提供了课程的详细视图，
     * 包括课程基本信息和相关章节列表
     *
     * @param id 课程ID，用于查询课程详细信息
     * @return 返回包含课程详细信息和章节列表的 DetailCourseVO 对象如果课程ID不存在，则返回null
     */
    @Override
    public DetailCourseVO getCourseDetail(Integer id) {
        // 通过课程ID查询课程基本信息
        ScanCourseVO scanCourseVO = courseMapper.selectCourseById(id);
        // 如果课程ID不存在，返回null
        if (scanCourseVO == null) {
            return null; // 或抛出异常，表示课程不存在
        }

        // 正确的 LambdaQueryWrapper 使用方式，根据 courseId 查询章节
        LambdaQueryWrapper<Chapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chapter::getCourseId, scanCourseVO.getCourseId()); // 根据 courseId 查询章节

        // 查询与当前课程相关的所有章节信息
        List<Chapter> chapters = chapterMapper.selectList(queryWrapper);
        // 创建一个 DetailedCourseVO 对象，用于封装课程详细信息
        DetailCourseVO detailCourseVO = new DetailCourseVO();
        // 将课程基本信息从 ScanCourseVO 复制到 DetailCourseVO
        BeanUtils.copyProperties(scanCourseVO, detailCourseVO);

        // 设置课程详细信息中的章节列表
        detailCourseVO.setChapteList(chapters); // 设置章节列表

        // 返回包含课程详细信息和章节列表的 DetailCourseVO 对象
        return detailCourseVO;
    }

    /**
     * 根据课程ID获取课程扫描详情
     *
     * @param id 课程ID
     * @return CourseScanDetailVO对象，包含课程扫描详情，如果课程不存在则返回null
     */
    @Override
    public CourseScanDetailVO getCourseScanDetail(Integer id) {
        // 通过课程ID查询课程信息
        ScanCourseVO scanCourseVO = courseMapper.selectCourseById(id);
        // 如果课程信息为空，则返回null
        if (scanCourseVO == null) {
            return null; // 或抛出异常，表示课程不存在
        }

        // 查询章节列表
        LambdaQueryWrapper<Chapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chapter::getCourseId, scanCourseVO.getCourseId());
        queryWrapper.orderByAsc(Chapter::getOrderNum); // 按排序序号排序

        // 执行章节查询
        List<Chapter> chapters = chapterMapper.selectList(queryWrapper);

        // 转换为ChapterPreviewVO，只包含基本信息
        List<ChapterPreviewVO> chapterPreviews = chapters.stream().map(chapter -> {
            ChapterPreviewVO preview = new ChapterPreviewVO();
            BeanUtils.copyProperties(chapter, preview);
            return preview;
        }).collect(Collectors.toList());

        // 创建课程扫描详情对象并设置属性
        CourseScanDetailVO courseScanDetailVO = new CourseScanDetailVO();
        BeanUtils.copyProperties(scanCourseVO, courseScanDetailVO);
        courseScanDetailVO.setChapteList(chapterPreviews);

        // 返回课程扫描详情对象
        return courseScanDetailVO;
    }

    /**
     * 创建课程服务接口的实现方法
     * 该方法负责处理课程创建请求，将传入的DTO对象数据转换并保存到数据库中，
     * 同时返回包含新创建课程信息的VO对象
     *
     * @param createCourseDTO 创建课程的数据传输对象，包含需要保存到数据库的课程信息
     * @return 返回一个包含新创建课程详细信息的值对象
     */
    @Override
    public CreateCourseVO CreateCourse(CreateCourseDTO createCourseDTO) {
        // 创建一个新的课程实体对象
        Course course = new Course();
        // 将DTO对象的属性复制到课程实体对象中
        BeanUtils.copyProperties(createCourseDTO, course);
        // 设置课程的类别ID
        course.setCategoryId(createCourseDTO.getCategoryId());
        // 设置课程的教师ID，通过获取当前用户ID的方法得到
        course.setTeacherId(getCurrentId());
        // 设置课程的创建时间，为当前系统时间
        course.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        // 通过Mapper接口将课程实体对象插入到数据库中
        courseMapper.insert(course);

        // 创建一个新的课程创建响应值对象
        CreateCourseVO createCourseVO = new CreateCourseVO();
        // 将DTO对象的属性复制到响应值对象中
        BeanUtils.copyProperties(createCourseDTO, createCourseVO);
        // 设置响应值对象的课程ID，来自数据库插入后的课程实体对象
        createCourseVO.setCourseId(course.getCourseId());
        // 设置响应值对象的审核状态，来自课程实体对象
        createCourseVO.setAuditStatues(course.getAuditStatus());
        // 设置响应值对象的创建时间，来自课程实体对象
        createCourseVO.setCreateTime(course.getCreateTime());
        // 设置响应值对象的封面图片URL，来自课程实体对象
        createCourseVO.setCoverImage(course.getCoverImage());
        // 返回包含新创建课程信息的响应值对象
        return createCourseVO;
    }

    @Override
    public CreateChapterVO CreateChapter(CreateChapterDTO createChapterDTO) {
        Chapter chapter = new Chapter();
        BeanUtils.copyProperties(createChapterDTO, chapter);
        chapter.setContentType(createChapterDTO.getContentType());
        Course course = courseMapper.selectById(createChapterDTO.getCourseId());
        if (course == null) throw new RuntimeException("课程不存在");
        chapter.setTeacherId(course.getTeacherId());
        chapterMapper.insert(chapter);
        CreateChapterVO createChapterVO = new CreateChapterVO();
        BeanUtils.copyProperties(createChapterDTO, createChapterVO);
        createChapterVO.setChapterId(chapter.getChapterId());
        createChapterVO.setContentType(chapter.getContentType());
        return createChapterVO;
    }

    /**
     * 更新课程信息方法
     * 该方法首先检查课程是否存在，然后验证当前教师是否有权限修改该课程，
     * 接着更新课程信息，最后返回更新后的课程视图对象
     *
     * @param updateCourseDTO 包含要更新的课程信息的数据传输对象
     * @return 更新后的课程视图对象
     * @throws RuntimeException 如果课程不存在或当前教师尝试修改非自己的课程，则抛出运行时异常
     */
    @Override
    public UpdateCourseVO updateCourse(UpdateCourseDTO updateCourseDTO) {
        // 查询课程是否存在
        Course existingCourse = courseMapper.selectById(updateCourseDTO.getCourseId());
        if (existingCourse == null) {
            throw new RuntimeException("课程不存在");
        }

        // 检查是否是当前教师的课程
        Integer currentTeacherId = getCurrentId();
        if (!existingCourse.getTeacherId().equals(currentTeacherId)) {
            throw new RuntimeException("只能修改自己的课程");
        }

        // 更新课程信息
        BeanUtils.copyProperties(updateCourseDTO, existingCourse);

        // 将List<String>转换为String存储
        if (updateCourseDTO.getCourseTags() != null) {
            existingCourse.setCourseTags(String.join(",", updateCourseDTO.getCourseTags()));
        }

        // 更新审核状态为待审核
        existingCourse.setAuditStatus("PENDING");
        existingCourse.setCategoryId(updateCourseDTO.getCategoryId());

        // 执行更新
        courseMapper.updateById(existingCourse);

        // 构建返回VO
        UpdateCourseVO updateCourseVO = new UpdateCourseVO();
        BeanUtils.copyProperties(existingCourse, updateCourseVO);
        updateCourseVO.setCourseTags(updateCourseDTO.getCourseTags());
        updateCourseVO.setUpdateMessage("课程更新成功，等待审核");

        return updateCourseVO;
    }
    /**
     * 审核课程
     *
     * @param
     */
    @Override
    public boolean AuditCourse(AuditCourseDTO auditCourseDTO) {
        Course course = courseMapper.selectById(auditCourseDTO.getCourseId());
        if (course != null) {
            // 更新审核状态
            course.setAuditStatus(auditCourseDTO.getAuditStatus());
            course.setAuditMessage(auditCourseDTO.getAuditMessage());
            // 使用MyBatis Plus的update方法
            courseMapper.updateById(course);
        }
        return true;
    }

    /**
     * 获取待审核课程列表
     */
    @Override
    public List<ScanCourseVO> getPendingCourses() {
        return courseMapper.selectPendingCourses();
    }

    /**
     * 根据审核状态获取课程列表
     */
    @Override
    public List<ScanCourseVO> getCoursesByStatus(String status) {
        if ("ALL".equalsIgnoreCase(status)) {
            // 查询所有状态的课程
            return courseMapper.selectAllCoursesForAudit();
        } else {
            // 查询指定状态的课程
            return courseMapper.selectCoursesByStatus(status);
        }
    }

    /**
     * 根据审核状态获取课程列表（分页）
     */
    @Override
    public PageResult getCoursesByStatusWithPage(String status, Integer page, Integer pageSize) {
        // 创建分页对象
        Page<ScanCourseVO> pageInfo = new Page<>(page, pageSize);
        Page<ScanCourseVO> result;

        // 根据状态查询课程
        if ("ALL".equalsIgnoreCase(status)) {
            result = courseMapper.selectAllCoursesForAuditWithPage(pageInfo);
        } else {
            result = courseMapper.selectCoursesByStatusWithPage(pageInfo, status);
        }

        // 统计各状态数量
        long pendingCount = courseMapper.countByStatus("PENDING");
        long approvedCount = courseMapper.countByStatus("APPROVED");
        long rejectedCount = courseMapper.countByStatus("REJECTED");

        // 将统计结果存入Map
        java.util.Map<String, Long> statusCounts = new java.util.HashMap<>();
        statusCounts.put("PENDING", pendingCount);
        statusCounts.put("APPROVED", approvedCount);
        statusCounts.put("REJECTED", rejectedCount);

        // 创建并返回分页结果对象
        PageResult pageResult = new PageResult(result.getTotal(), result.getRecords(), statusCounts);
        return pageResult;
    }

    /**
     * 收藏课程
     *
     * 此方法用于处理用户对课程的收藏操作如果用户已经收藏了课程，再次收藏会取消收藏状态；
     * 如果用户未收藏课程，则会创建一个新的收藏记录默认收藏状态为未取消（即已收藏）
     *
     * @param courseId 课程ID，用于标识要收藏的课程
     * @return 返回收藏操作是否成功true表示成功，false表示失败
     */
    @Override
    public boolean favoriteCourse(Integer courseId) {
        // 获取当前用户ID
        Integer currentId = getCurrentId();
        // currentId = 4;//TODO先手动加上去
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, currentId).eq(Favorite::getCourseId, courseId);

        // 查询是否已有收藏记录
        Favorite existing = favoriteMapper.selectOne(queryWrapper);

        if (existing != null) {
            // 存在记录，切换 isCancel 状态
            if (existing.getIsCancel() == 0) {
                existing.setIsCancel(1); // 取消收藏
            } else {
                existing.setIsCancel(0); // 恢复收藏
            }
            // 更新收藏记录
            return favoriteMapper.updateById(existing) > 0;
        } else {
            // 不存在记录，插入新数据，默认 isCancel = 0
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(currentId);
            newFavorite.setCourseId(courseId);
            newFavorite.setIsCancel(0); // 默认收藏状态
            // 添加新的收藏记录
            return favoriteMapper.insert(newFavorite) > 0;
        }
    }


    /**
     * 获取课程列表，根据页码、关键词和分类进行筛选
     *
     * @param pageQueryDTO 包含页码、页大小、关键词和分类ID的查询DTO
     * @return 返回包含课程列表和总记录数的PageResult对象
     */
    @Override
    public PageResult getCourseList(PageQueryDTO pageQueryDTO) {
        // 初始化分页对象
        Page<ScanCourseVO> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        // 获取关键词
        String keyword = pageQueryDTO.getKeyword();
        // 获取分类ID
        Integer categoryId = pageQueryDTO.getCategoryId();

        // 使用新的分页查询方法，支持关键词和分类筛选
        Page<ScanCourseVO> pageResult = courseMapper.selectCoursePageWithDetails(page, keyword, categoryId);

        // 返回封装了总记录数和课程记录的PageResult对象
        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
    }
    /*
        获取收藏课程
     */
    @Override
    public PageResult getfavoriteCourse(PageQueryDTO pageQueryDTO) {
        // 获取当前用户ID
        Integer currentUserId = getCurrentId();
        // 创建分页对象
        Page<ScanCourseVO> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        // 构建查询条件：只查收藏课程
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, currentUserId)
                .eq(Favorite::getIsCancel, 0); // 只查未取消的收藏
        // 查询收藏记录的 courseIds
        List<Integer> courseIds = favoriteMapper.selectList(queryWrapper)
                .stream()
                .map(Favorite::getCourseId)
                .collect(Collectors.toList());

        // 如果没有收藏课程，返回空列表
        if (courseIds.isEmpty()) {
            return new PageResult(0L, java.util.Collections.emptyList());
        }

        // 查询课程信息（现在直接返回ScanCourseVO，包含教师名和分类名）
        Page<ScanCourseVO> coursePage = courseMapper.selectCourseByIds(page, courseIds);

        // 返回分页结果
        return new PageResult(coursePage.getTotal(), coursePage.getRecords());
    }

    /**
     * 获取老师课程
     *
     * 此方法用于根据教师ID查询该教师的所有课程，并进行分页处理
     * 如果传入的教师ID为空，则使用当前登录的教师ID进行查询
     *
     * @param pageQueryDTO 包含分页信息和教师ID的查询对象
     * @return 返回分页后的课程结果
     */
    @Override
    public PageResult getTeacherCourse(PageQueryDTO pageQueryDTO) {
        // 获取当前教师ID，如果传入的教师ID为空，则使用当前登录的教师ID
        Integer currentId = pageQueryDTO.getTeacherId();
        if (currentId == null) {
            currentId = getCurrentId();
        }

        // 记录查询日志，包括当前教师ID和传入的教师ID
        log.info("查询教师课程 - 教师ID: {}, 传入的teacherId: {}", currentId, pageQueryDTO.getTeacherId());

        // 查询该老师的所有课程（包含教师姓名和分类名称）
        List<ScanCourseVO> allCourses = courseMapper.selectTeacherCourses(currentId);

        // 记录查询到的课程数量
        log.info("查询到的课程数量: {}", allCourses.size());

        // 手动分页处理
        int page = pageQueryDTO.getPage();
        int pageSize = pageQueryDTO.getPageSize();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allCourses.size());

        // 根据分页计算结果，获取当前页的课程列表
        List<ScanCourseVO> pageCourses;
        if (startIndex >= allCourses.size()) {
            // 如果起始索引大于总课程数，说明没有数据，返回空列表
            pageCourses = new ArrayList<>();
        } else {
            // 否则，获取当前页的课程列表
            pageCourses = allCourses.subList(startIndex, endIndex);
        }

        // 记录分页结果日志，包括总课程数和当前页课程数
        log.info("分页结果 - 总数: {}, 当前页数据量: {}", allCourses.size(), pageCourses.size());

        // 返回分页结果
        return new PageResult((long) allCourses.size(), pageCourses);
    }
    /*
     * 删除课程（仅在没有用户购买的情况下）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Integer courseId) {
        // 1. 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 2. 检查是否是当前教师的课程
        Integer currentTeacherId = getCurrentId();
        if (!course.getTeacherId().equals(currentTeacherId)) {
            throw new RuntimeException("只能删除自己的课程");
        }

        // 3. 检查是否有用户购买过这个课程
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getCourseId, courseId)
                .eq(Order::getOrderStatus, "PAID");
        Long purchaseCount = orderMapper.selectCount(orderWrapper);

        if (purchaseCount > 0) {
            throw new RuntimeException("该课程已有用户购买，无法删除");
        }

        // 4. 删除相关数据（按依赖关系顺序删除）

        // 删除收藏记录（如果有的话）
        LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(Favorite::getCourseId, courseId);
        favoriteMapper.delete(favoriteWrapper);

        // 删除课程章节（MyBatis Plus会自动处理外键约束）
        LambdaQueryWrapper<Chapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(Chapter::getCourseId, courseId);
        chapterMapper.delete(chapterWrapper);

        // 删除课程本身
        int result = courseMapper.deleteById(courseId);

        log.info("课程删除成功 - 课程ID: {}, 删除结果: {}", courseId, result > 0);

        return result > 0;
    }

    /*
     * 获取课程章节列表
     */
    @Override
    public List<ChapterVO> getChapterList(Integer courseId) {
        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 查询章节列表，按orderNum排序
        LambdaQueryWrapper<Chapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chapter::getCourseId, courseId)
                .orderByAsc(Chapter::getOrderNum);

        List<Chapter> chapters = chapterMapper.selectList(queryWrapper);

        // 转换为VO
        return chapters.stream().map(chapter -> {
            ChapterVO vo = new ChapterVO();
            BeanUtils.copyProperties(chapter, vo);
            vo.setContentType(chapter.getContentType());
            return vo;
        }).collect(Collectors.toList());
    }

    /*
     * 获取章节详情
     */
    @Override
    public ChapterVO getChapterDetail(Integer chapterId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new RuntimeException("章节不存在");
        }

        ChapterVO vo = new ChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        vo.setContentType(chapter.getContentType());
        return vo;
    }

    /*
     * 更新章节
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateChapter(UpdateChapterDTO updateChapterDTO) {
        // 1. 检查章节是否存在
        Chapter existingChapter = chapterMapper.selectById(updateChapterDTO.getChapterId());
        if (existingChapter == null) {
            throw new RuntimeException("章节不存在");
        }

        // 2. 检查课程是否存在
        Course course = courseMapper.selectById(updateChapterDTO.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 检查是否是当前教师的课程
        Integer currentTeacherId = getCurrentId();
        if (!course.getTeacherId().equals(currentTeacherId)) {
            throw new RuntimeException("只能修改自己课程的章节");
        }

        // 4. 更新章节信息
        BeanUtils.copyProperties(updateChapterDTO, existingChapter);
        existingChapter.setContentType(updateChapterDTO.getContentType());
        // 新增：自动补全teacherId
        existingChapter.setTeacherId(course.getTeacherId());
        int result = chapterMapper.updateById(existingChapter);

        log.info("章节更新成功 - 章节ID: {}, 更新结果: {}", updateChapterDTO.getChapterId(), result > 0);

        return result > 0;
    }

    /*
     * 删除章节
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteChapter(Integer chapterId) {
        // 1. 检查章节是否存在
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new ServiceException("章节不存在");
        }

        // 2. 检查课程是否存在
        Course course = courseMapper.selectById(chapter.getCourseId());
        if (course == null) {
            throw new ServiceException("课程不存在");
        }

        // 3. 检查是否是当前教师的课程
        Integer currentTeacherId = getCurrentId();
        if (!course.getTeacherId().equals(currentTeacherId)) {
            throw new ServiceException("只能删除自己课程的章节");
        }

        // 4. 检查课程是否有用户购买
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getCourseId, chapter.getCourseId())
                .eq(Order::getOrderStatus, "PAID");
        Long purchaseCount = orderMapper.selectCount(orderWrapper);

        if (purchaseCount > 0) {
            throw new ServiceException("该课程已有用户购买，无法删除章节");
        }

        // 5. 删除章节
        int result = chapterMapper.deleteById(chapterId);

        log.info("章节删除成功 - 章节ID: {}, 删除结果: {}", chapterId, result > 0);

        return result > 0;
    }

    @Override
    public Result<String> favoriteCourseStatus(Integer courseId, Integer userId) {
        Favorite favorite = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getCourseId, courseId)
                .eq(Favorite::getUserId, userId));
        if (favorite != null && favorite.getIsCancel() == 0) {
            return Result.success("已收藏");
        } else {
            return Result.success("未收藏");
        }
    }

    @Override
    public boolean updateCourseCategory(com.lesson.dto.UpdateCourseCategoryDTO dto) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) return false;

        // 检查课程是否已审核通过
        if (!"APPROVED".equals(course.getAuditStatus())) {
            log.warn("课程未审核通过，无法修改分类 - 课程ID: {}, 审核状态: {}", dto.getCourseId(), course.getAuditStatus());
            return false;
        }

        course.setCategoryId(dto.getCategoryId());
        courseMapper.updateById(course);
        return true;
    }

    @Override
    public boolean toggleRecommend(com.lesson.dto.ToggleRecommendDTO dto) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) return false;
        // 检查课程是否已审核通过
        if (!"APPROVED".equals(course.getAuditStatus())) {
            log.warn("课程未审核通过，无法评分 - 课程ID: {}, 审核状态: {}", dto.getCourseId(), course.getAuditStatus());
            return false;
        }
        course.setScore(dto.getScore() != null ? dto.getScore() : 0);
        courseMapper.updateById(course);
        return true;
    }

    @Override
    public PageResult getTeacherTestCourse(PageQueryDTO pageQueryDTO) {
        Integer currentId = pageQueryDTO.getTeacherId();
        if (currentId == null) {
            currentId = getCurrentId();
        }


        // 查询该老师的所有课程（包含教师姓名和分类名称）
        List<ScanCourseVO> allCourses = courseMapper.selectTeacherTestCourses(currentId);


        // 手动分页处理
        int page = pageQueryDTO.getPage();
        int pageSize = pageQueryDTO.getPageSize();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allCourses.size());

        List<ScanCourseVO> pageCourses;
        if (startIndex >= allCourses.size()) {
            pageCourses = new ArrayList<>();
        } else {
            pageCourses = allCourses.subList(startIndex, endIndex);
        }

        log.info("分页结果 - 总数: {}, 当前页数据量: {}", allCourses.size(), pageCourses.size());

        // 返回分页结果
        return new PageResult((long) allCourses.size(), pageCourses);
    }
}