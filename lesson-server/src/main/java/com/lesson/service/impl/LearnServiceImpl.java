package com.lesson.service.impl;

import com.lesson.dto.LearnProcessDTO;
import com.lesson.mapper.LearnMapper;
import com.lesson.service.LearnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LearnServiceImpl implements LearnService {
    @Autowired
    private LearnMapper learnMapper;
    /*
     * 查询进度
     */
    @Override
    public String getLearnProcess(LearnProcessDTO learnProcessDTO) {
        Integer LearnedNum = learnMapper.getLearnedNum(learnProcessDTO);
        Integer ChapterNum = learnMapper.getChapterNum(learnProcessDTO);
        float result = (float) LearnedNum / ChapterNum;
        return String.format("%.2f%%", result * 100);//没有百分号的形式
    }

    @Override
    public void completeChapter(Integer userId, Integer courseId, Integer chapterId) {
        learnMapper.upsertProgress(userId, courseId, chapterId, 1);
    }
}