package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.Course;
import com.lesson.entity.Order;
import com.lesson.mapper.CourseMapper;
import com.lesson.mapper.OrderMapper;
import com.lesson.result.PageResult;
import com.lesson.service.MyCoursesService;
import com.lesson.vo.MyCoursesStatsVO;
import com.lesson.vo.MyCourseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.lesson.context.BaseContext.getCurrentId;

@Service
@Slf4j
public class MyCoursesServiceImpl implements MyCoursesService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 获取用户学习统计信息
     * <p>
     * 此方法首先获取当前用户已购买课程的数量，然后计算用户学习的章节总数、学习总时长和完成的课程数
     * 如果用户没有购买任何课程，则直接返回默认的学习统计信息
     *
     * @return MyCoursesStatsVO 包含用户学习统计信息的对象
     */
    @Override
    public MyCoursesStatsVO getStudyStats() {
        // 获取当前用户ID
        Integer currentUserId = getCurrentId();

        // 获取用户购买的课程数量
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, currentUserId)
                .eq(Order::getOrderStatus, "PAID");
        Long totalCourses = orderMapper.selectCount(orderWrapper);

        // 从learning_progress表计算实际的学习统计信息
        Integer totalChapters = 0;
        Integer totalStudyTime = 0;
        Integer completedCourses = 0;

        // 如果有购买的课程，计算统计信息
        if (totalCourses > 0) {
            // 获取用户购买的课程列表
            List<Order> orders = orderMapper.selectList(orderWrapper);
            // 提取并去重课程ID
            List<Integer> courseIds = orders.stream()
                    .map(Order::getCourseId)
                    .distinct()
                    .collect(Collectors.toList());

            // 获取课程统计信息
            List<MyCourseVO> courses = courseMapper.selectMyCourses(courseIds, currentUserId);

            // 计算总章节数
            totalChapters = courses.stream()
                    .mapToInt(MyCourseVO::getTotalChapters)
                    .sum();

            // 计算总学习时长
            totalStudyTime = courses.stream()
                    .mapToInt(MyCourseVO::getStudyTime)
                    .sum();

            // 计算完成的课程数
            completedCourses = (int) courses.stream()
                    .filter(course -> "completed".equals(course.getStatus()))
                    .count();
        }

        // 构建并返回用户学习统计信息对象
        return MyCoursesStatsVO.builder()
                .totalCourses(totalCourses.intValue())
                .totalChapters(totalChapters)
                .totalStudyTime(totalStudyTime)
                .completedCourses(completedCourses)
                .build();
    }

    @Override
    public PageResult getMyCourses(PageQueryDTO pageQueryDTO) {
        // 获取当前用户的ID
        Integer currentUserId = getCurrentId();

        // 1. 获取用户购买的课程ID列表
        // 创建一个查询条件构建器，用于查询用户已支付的订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, currentUserId)
                .eq(Order::getOrderStatus, "PAID");
        // 执行查询，获取订单列表
        List<Order> orders = orderMapper.selectList(orderWrapper);

        // 如果没有找到任何订单，直接返回空的结果集
        if (orders.isEmpty()) {
            return new PageResult(0L, Collections.emptyList());
        }

        // 从订单中提取课程ID，去重后放入列表
        List<Integer> courseIds = orders.stream()
                .map(Order::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        // 2. 分页查询课程信息
        // 创建一个分页对象，用于查询用户购买的课程信息
        Page<MyCourseVO> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        // 执行分页查询，获取课程信息
        Page<MyCourseVO> coursePage = courseMapper.selectMyCoursesPage(page, courseIds, currentUserId);

        // 将查询结果封装到PageResult对象中并返回
        return new PageResult(coursePage.getTotal(), coursePage.getRecords());
    }

    /**
     * 获取当前用户购买的所有课程信息
     * <p>
     * 此方法首先查询当前用户购买的课程ID列表，然后根据这些ID查询详细的课程信息
     * 如果用户没有购买任何课程，则返回空列表
     *
     * @return 包含当前用户购买的所有课程信息的列表
     */
    @Override
    public List<MyCourseVO> getAllMyCourses() {
        // 获取当前用户的ID
        Integer currentUserId = getCurrentId();

        // 1. 获取用户购买的课程ID列表
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, currentUserId)
                .eq(Order::getOrderStatus, "PAID");
        List<Order> orders = orderMapper.selectList(orderWrapper);

        // 如果订单列表为空，直接返回空列表
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        // 从订单中提取课程ID，去重后组成列表
        List<Integer> courseIds = orders.stream()
                .map(Order::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        // 2. 查询所有课程信息
        // 根据课程ID列表和当前用户ID查询详细的课程信息
        return courseMapper.selectMyCourses(courseIds, currentUserId);
    }
}