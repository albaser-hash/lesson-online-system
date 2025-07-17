package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.QaAnswer;
import com.lesson.vo.QaAnswerDetailVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface QaAnswerMapper extends BaseMapper<QaAnswer> {
    @Select("SELECT a.*, COALESCE(NULLIF(u.name, ''), u.user_name) as user_name, u.avatar FROM qa_answers a LEFT JOIN user u ON a.user_id = u.user_id WHERE a.question_id = #{questionId} ORDER BY a.is_best DESC, a.create_time ASC")
    List<QaAnswerDetailVO> selectByQuestionId(@Param("questionId") Integer questionId);
} 