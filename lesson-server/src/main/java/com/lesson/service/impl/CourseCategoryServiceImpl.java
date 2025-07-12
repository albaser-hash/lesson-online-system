package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.CourseCategory;
import com.lesson.mapper.CourseCategoryMapper;
import com.lesson.result.PageResult;
import com.lesson.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public void add(CourseCategory category) {
        courseCategoryMapper.insert(category);
    }

    @Override
    public void update(CourseCategory category) {
        courseCategoryMapper.updateById(category);
    }

    @Override
    public void delete(Integer categoryId) {
        courseCategoryMapper.deleteById(categoryId);
    }

    @Override
    @Cacheable(value = "courseCategory", key = "'id:' + #categoryId", unless = "#result == null")
    public CourseCategory getById(Integer categoryId) {
        return courseCategoryMapper.selectById(categoryId);
    }

    /**
     * 重写课程类别列表方法以支持分页和缓存
     *
     * @param pageQueryDTO 分页查询对象，包含页码和页面大小信息
     * @return 返回一个包含课程类别数据的PageResult对象
     *
     * 此方法使用MyBatis-Plus的分页和条件构造器来查询课程类别数据
     * 它重写了基类的方法，以提供更高效的查询和更好的用户体验
     *
     * 注意：此处使用了Cacheable注解来实现方法结果的缓存
     * - value属性指定缓存的名称
     * - key属性指定缓存的键
     * - unless属性指定在什么条件下不缓存结果（此处为结果为空时不缓存）
     */
    @Override
    @Cacheable(value = "courseCategory", key = "'list'", unless = "#result == null")
    public PageResult list(PageQueryDTO pageQueryDTO) {
        // 创建分页对象
        Page<CourseCategory> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        // 创建Lambda查询条件构造器
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        // 可根据需要添加查询条件，例如：wrapper.like(CourseCategory::getCategoryName, xxx);
        // 执行分页查询
        Page<CourseCategory> result = courseCategoryMapper.selectPage(page, wrapper);
        // 返回分页结果，包括总记录数和当前页记录列表
        return new PageResult(result.getTotal(), result.getRecords());
    }
} 