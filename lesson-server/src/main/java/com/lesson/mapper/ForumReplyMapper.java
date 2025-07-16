package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.ForumReply;
import com.lesson.vo.ForumReplyDetailVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ForumReplyMapper extends BaseMapper<ForumReply> {
    /**
     * 查询帖子的回复列表（包括所有子回复）
     */
    @Select("SELECT fr.reply_id as replyId, fr.topic_id as topicId, fr.user_id as userId, " +
            "fr.reply_content as replyContent, fr.create_time as createTime, " +
            "fr.parent_reply_id as parentReplyId, fr.reply_to_user_id as replyToUserId, " +
            "fr.reply_to_user_name as replyToUserName, " +
            "COALESCE(NULLIF(u.name, ''), u.user_name) as userName, u.avatar " +
            "FROM forum_replies fr " +
            "LEFT JOIN user u ON fr.user_id = u.user_id " +
            "WHERE fr.topic_id = #{topicId} " +
            "ORDER BY fr.create_time ASC")
    List<ForumReplyDetailVO> selectRepliesByTopicId(Integer topicId);

    /**
     * 查询子回复列表
     */
    @Select("SELECT fr.reply_id as replyId, fr.topic_id as topicId, fr.user_id as userId, " +
            "fr.reply_content as replyContent, fr.create_time as createTime, " +
            "fr.parent_reply_id as parentReplyId, fr.reply_to_user_id as replyToUserId, " +
            "fr.reply_to_user_name as replyToUserName, " +
            "COALESCE(NULLIF(u.name, ''), u.user_name) as userName, u.avatar " +
            "FROM forum_replies fr " +
            "LEFT JOIN user u ON fr.user_id = u.user_id " +
            "WHERE fr.parent_reply_id = #{parentReplyId} " +
            "ORDER BY fr.create_time ASC")
    List<ForumReplyDetailVO> selectChildRepliesByParentId(Integer parentReplyId);
}
