package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.dto.LearnProcessDTO;
import com.lesson.entity.Learn;

public interface LearnMapper extends BaseMapper<Learn> {
    Integer getLearnedNum(LearnProcessDTO dto);
    Integer getChapterNum(LearnProcessDTO dto);
    void upsertProgress(Integer userId, Integer courseId, Integer chapterId, Integer status);
}