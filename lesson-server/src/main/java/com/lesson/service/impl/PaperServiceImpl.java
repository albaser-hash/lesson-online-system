package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lesson.dto.AuditCourseDTO;
import com.lesson.dto.CreateChapterDTO;
import com.lesson.dto.CreateCourseDTO;
import com.lesson.dto.SubmitPaperDTO;
import com.lesson.entity.*;
import com.lesson.mapper.*;
import com.lesson.service.CourseService;
import com.lesson.service.PaperService;
import com.lesson.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lesson.context.BaseContext.getCurrentId;

@Service
@Slf4j
public class PaperServiceImpl implements PaperService {
    @Autowired
    private PaperMapper paperMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Autowired
    private ExamMapper examMapper;


/**
 * 提交试卷
 *
 * 该方法将用户填写的试卷信息提交到系统中，创建一个Paper对象，设置必要的属性，
 * 并将其插入到数据库中
 *
 * @param submitPaperDTO 包含用户提交试卷的信息，如用户ID、试卷ID等
 * @return boolean 表示试卷是否成功提交，true表示成功，false表示失败
 */
@Override
public boolean SubmitPaper(SubmitPaperDTO submitPaperDTO) {
    // 创建一个新的Paper对象
    Paper paper = new Paper();

    // 将SubmitPaperDTO中的属性复制到Paper对象中
    BeanUtils.copyProperties(submitPaperDTO, paper);

    // 设置当前用户的ID
    paper.setUserId(getCurrentId());

    // 设置试卷ID，此处硬编码为4，应根据实际情况动态获取
    paper.setPaperId(4);

    // 设置提交时间，为当前系统时间
    paper.setSubmitTime(Timestamp.valueOf(LocalDateTime.now()));

    // 插入Paper对象到数据库，返回影响的行数
    Integer insertnum=paperMapper.insert(paper);

    // 根据插入结果判断是否成功
    if(insertnum>0){
        return true;
    }
    return false;
}

/*
 * 评分试卷
 */
    @Override
    public ScoreVO ScorePaper(Integer examId) {
        //查询试卷名
        Exam exam=examMapper.selectById(examId);
        // 1. 查询本次考试的所有题目信息
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId, examId)
        );
        // 2. 创建 Map 便于快速查找正确答案
        Map<Integer, ExamQuestion> questionMap = examQuestions.stream()
                .collect(Collectors.toMap(ExamQuestion::getQuestionId, q -> q));

        // 3. 初始化总分
        int totalScore = 0;
        List<ScoreVO.QuestionAnalysis> analysisList = new ArrayList<>();
        Integer currentId = getCurrentId();
        currentId=4;//TODO
        //查到用户答案
        Paper paper=paperMapper.selectByExamIdAndUserId(currentId,examId);
        List<Paper.AnswerItem> studentanswer = paper.getAnswer();
        // 4. 遍历用户作答，得到题号和提交答案
        for (Paper.AnswerItem answeri : studentanswer) {
            Integer questionId = answeri.getQuestionId();
            String userAnswer = answeri.getUserAnswer();
            ExamQuestion examQuestion = questionMap.get(questionId);
            String questionType = examQuestion.getQuestionType();
            String correctAnswer = examQuestion.getCorrectAnswer();
            Integer score = examQuestion.getScore();
            boolean isCorrect = false;//true为正确
            // 5. 判分逻辑
            switch (questionType) {
                case "SIGNLE_CHOICE": // 单选
                case "MULTIPLE_CHOICE": // 多选
                case "JUDGE": // 判断
                    if (userAnswer != null && userAnswer.equalsIgnoreCase(correctAnswer)) {
                        totalScore += score;
                        isCorrect=true;
                    }
                    break;
                case "TEXT": // 简答题，默认给分
                    if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                        totalScore += score;
                        isCorrect = true; // 认为填写即满分
                    } else {
                        isCorrect = false; // 空答案不得分
                    }
                    break;
                default:
                    break;
            }
            // 组装分析对象
            ScoreVO.QuestionAnalysis analysis = new ScoreVO.QuestionAnalysis();
            analysis.setQuestionContent(examQuestion.getQuestionContent());
            analysis.setUserAnswer(userAnswer);
            analysis.setCorrectAnswer(correctAnswer);
            analysis.setCorrect(isCorrect);
            analysisList.add(analysis);
        }
        ScoreVO scoreVO=new ScoreVO();
        scoreVO.setScore(totalScore);
        scoreVO.setExamName(exam.getExamName());
        scoreVO.setSubmitTime(paper.getSubmitTime());
        scoreVO.setAnalysis(analysisList);
        return scoreVO;
    }
}