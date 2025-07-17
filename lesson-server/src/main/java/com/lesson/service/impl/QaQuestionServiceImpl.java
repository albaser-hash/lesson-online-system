package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.QaAnswer;
import com.lesson.entity.QaQuestion;
import com.lesson.mapper.QaAnswerMapper;
import com.lesson.mapper.QaQuestionMapper;
import com.lesson.result.PageResult;
import com.lesson.service.QaQuestionService;
import com.lesson.vo.QaAnswerDetailVO;
import com.lesson.vo.QaQuestionDetailVO;
import com.lesson.vo.QaQuestionWithAnswersVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QaQuestionServiceImpl implements QaQuestionService {
    @Autowired
    private QaQuestionMapper qaQuestionMapper;

    @Autowired
    private QaAnswerMapper qaAnswerMapper;

    @Override
    public void add(QaQuestion qaQuestion) {
        qaQuestionMapper.insert(qaQuestion);
    }

    @Override
    public void update(QaQuestion qaQuestion) {
        qaQuestionMapper.updateById(qaQuestion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer questionId) {
        // 先删除该问题下的所有回答
        LambdaQueryWrapper<QaAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(QaAnswer::getQuestionId, questionId);
        qaAnswerMapper.delete(answerWrapper);
        
        // 再删除问题本身
        qaQuestionMapper.deleteById(questionId);
    }

    @Override
    public QaQuestion getById(Integer questionId) {
        return qaQuestionMapper.selectById(questionId);
    }

    @Override
    public PageResult list(PageQueryDTO pageQueryDTO) {
        Page<QaQuestion> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<QaQuestion> wrapper = new LambdaQueryWrapper<>();
        // 可加条件 wrapper.eq(QaQuestion::getUserId, xxx);
        Page<QaQuestion> result = qaQuestionMapper.selectPage(page, wrapper);
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public PageResult listWithUserInfo(PageQueryDTO pageQueryDTO) {
        Page<QaQuestionDetailVO> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<QaQuestionDetailVO> result = qaQuestionMapper.selectPageWithUserInfo(page, pageQueryDTO.getKeyword());
        return new PageResult(result.getTotal(), result.getRecords());
    }

    @Override
    public QaQuestionDetailVO getDetailById(Integer questionId) {
        return qaQuestionMapper.selectDetailById(questionId);
    }

    @Override
    public QaQuestionWithAnswersVO getFullDetailById(Integer questionId) {
        QaQuestionDetailVO question = qaQuestionMapper.selectDetailById(questionId);
        List<QaAnswerDetailVO> answers = qaAnswerMapper.selectByQuestionId(questionId);
        QaQuestionWithAnswersVO vo = new QaQuestionWithAnswersVO();
        BeanUtils.copyProperties(question, vo);
        vo.setAnswers(answers);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setBestAnswer(Integer questionId, Integer answerId) {
        // 参数校验
        if (questionId == null || answerId == null) {
            throw new IllegalArgumentException("问题ID和回答ID不能为空");
        }
        
        // 检查问题是否存在
        QaQuestion question = qaQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        
        // 检查回答是否存在且属于该问题
        QaAnswer targetAnswer = qaAnswerMapper.selectById(answerId);
        if (targetAnswer == null || !targetAnswer.getQuestionId().equals(questionId)) {
            throw new IllegalArgumentException("回答不存在或不属于该问题");
        }
        
        // 更新问题表
        question.setBestAnswerId(answerId);
        qaQuestionMapper.updateById(question);
        
        // 更新所有回答的isBest
        List<QaAnswer> answers = qaAnswerMapper.selectList(new LambdaQueryWrapper<QaAnswer>().eq(QaAnswer::getQuestionId, questionId));
        for (QaAnswer answer : answers) {
            answer.setIsBest(answer.getAnswerId().equals(answerId) ? 1 : 0);
            qaAnswerMapper.updateById(answer);
        }
    }
} 