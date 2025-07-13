package com.lesson.service;

import com.lesson.dto.LearnProcessDTO;

public interface LearnService {
    //获取进度
    String getLearnProcess(LearnProcessDTO learnProcessDTO);
    void completeChapter(Integer userId, Integer courseId, Integer chapterId);
}
