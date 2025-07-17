package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.QaAnswer;
import com.lesson.mapper.QaAnswerMapper;
import com.lesson.result.PageResult;
import com.lesson.service.QaAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QaAnswerServiceImpl implements QaAnswerService {
    @Autowired
    private QaAnswerMapper qaAnswerMapper;

    @Override
    public void add(QaAnswer qaAnswer) {
        qaAnswerMapper.insert(qaAnswer);
    }

    @Override
    public void update(QaAnswer qaAnswer) {
        qaAnswerMapper.updateById(qaAnswer);
    }

    @Override
    public void delete(Integer answerId) {
        qaAnswerMapper.deleteById(answerId);
    }

    @Override
    public QaAnswer getById(Integer answerId) {
        return qaAnswerMapper.selectById(answerId);
    }

    @Override
    public PageResult list(PageQueryDTO pageQueryDTO) {
        Page<QaAnswer> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<QaAnswer> wrapper = new LambdaQueryWrapper<>();
        // 可加条件 wrapper.eq(QaAnswer::getQuestionId, xxx);
        Page<QaAnswer> result = qaAnswerMapper.selectPage(page, wrapper);
        return new PageResult(result.getTotal(), result.getRecords());
    }
} 