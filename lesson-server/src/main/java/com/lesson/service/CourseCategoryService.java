package com.lesson.service;

import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.CourseCategory;
import com.lesson.result.PageResult;

public interface CourseCategoryService {
    void add(CourseCategory category);
    void update(CourseCategory category);
    void delete(Integer categoryId);
    CourseCategory getById(Integer categoryId);
    PageResult list(PageQueryDTO pageQueryDTO);
} 