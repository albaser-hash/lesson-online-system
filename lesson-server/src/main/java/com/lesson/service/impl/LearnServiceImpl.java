package com.lesson.service.impl;

import com.lesson.dto.AuditCourseDTO;
import com.lesson.dto.CreateChapterDTO;
import com.lesson.dto.CreateCourseDTO;
import com.lesson.dto.LearnProcessDTO;
import com.lesson.entity.Chapter;
import com.lesson.entity.Course;
import com.lesson.mapper.ChapterMapper;
import com.lesson.mapper.CourseMapper;
import com.lesson.mapper.LearnMapper;
import com.lesson.service.CourseService;
import com.lesson.service.LearnService;
import com.lesson.vo.CreateChapterVO;
import com.lesson.vo.CreateCourseVO;
import com.lesson.vo.DetailCourseVO;
import com.lesson.vo.ScanCourseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.lesson.context.BaseContext.getCurrentId;

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