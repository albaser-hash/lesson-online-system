package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.entity.ForumTopics;
import com.lesson.vo.ForumTopicDetailVO;
import org.apache.ibatis.annotations.Select;


public interface ForumTopicsMapper extends BaseMapper<ForumTopics> {

    /**
     * 分页查询帖子列表，并关联用户信息
     * @param page
     * @return
     */
    @Select("SELECT ft.*, COALESCE(NULLIF(u.name, ''), u.user_name) as user_name, u.avatar, COUNT(fr.reply_id) AS replyCount " +
            "FROM forum_topics ft " +
            "LEFT JOIN user u ON ft.user_id = u.user_id " +
            "LEFT JOIN forum_replies fr ON ft.topic_id = fr.topic_id " +
            "GROUP BY ft.topic_id " +
            "ORDER BY ft.create_time DESC")
    Page<ForumTopicDetailVO> selectTopicPage(Page<ForumTopicDetailVO> page);

    /**
     * 查询单个贴子详情（带用户信息）
     */
    @Select("SELECT ft.*, COALESCE(NULLIF(u.name, ''), u.user_name) as user_name, u.avatar, COUNT(fr.reply_id) AS replyCount " +
            "FROM forum_topics ft " +
            "LEFT JOIN user u ON ft.user_id = u.user_id " +
            "LEFT JOIN forum_replies fr ON ft.topic_id = fr.topic_id " +
            "WHERE ft.topic_id = #{topicId} " +
            "GROUP BY ft.topic_id")
    ForumTopicDetailVO selectTopicDetailById(Integer topicId);
}
