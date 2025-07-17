package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.Chapter;
import com.lesson.entity.Paper;


public interface PaperMapper extends BaseMapper<Paper> {
    Paper selectByExamIdAndUserId(Integer currentId, Integer examId);

}
