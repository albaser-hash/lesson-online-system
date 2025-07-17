package com.lesson.service;

import com.lesson.dto.AuditCourseDTO;
import com.lesson.dto.CreateChapterDTO;
import com.lesson.dto.CreateCourseDTO;
import com.lesson.dto.LearnProcessDTO;
import com.lesson.vo.CreateChapterVO;
import com.lesson.vo.CreateCourseVO;
import com.lesson.vo.DetailCourseVO;
import com.lesson.vo.ScanCourseVO;

import java.util.List;

public interface LearnService {
    //获取进度
    String getLearnProcess(LearnProcessDTO learnProcessDTO);
    void completeChapter(Integer userId, Integer courseId, Integer chapterId);
}
