package com.lesson.service;

import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.QaAnswer;
import com.lesson.result.PageResult;

public interface QaAnswerService {
    void add(QaAnswer qaAnswer);
    void update(QaAnswer qaAnswer);
    void delete(Integer answerId);
    QaAnswer getById(Integer answerId);
    PageResult list(PageQueryDTO pageQueryDTO);
} 