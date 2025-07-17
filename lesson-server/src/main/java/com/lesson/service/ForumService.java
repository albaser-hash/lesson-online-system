package com.lesson.service;

import com.lesson.dto.ForumReplyDTO;
import com.lesson.dto.ForumTopicsDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.User;
import com.lesson.result.PageResult;
import com.lesson.vo.ForumTopicsVO;
import com.lesson.vo.ForumTopicDetailVO;
import com.lesson.vo.ForumTopicWithRepliesVO;
import java.util.List;

public interface ForumService {

    //创建
    ForumTopicsVO topics(ForumTopicsDTO forumTopicsDTO);
    //回复
    boolean reply(ForumReplyDTO forumReplyDTO);

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    PageResult list(PageQueryDTO pageQueryDTO);

    /**
     * 查询单个贴子详情
     */
    ForumTopicDetailVO detail(Integer topicId);

    /**
     * 查询贴子详情及所有回复
     */
    ForumTopicWithRepliesVO detailWithReplies(Integer topicId);

    void updateTopic(ForumTopicsDTO forumTopicsDTO);
    void deleteTopic(Integer id);
    void updateReply(ForumReplyDTO forumReplyDTO);
    void deleteReply(Integer id);
    ForumReplyDTO getReplyById(Integer id);
    void batchDeleteTopics(List<Integer> ids);
    void batchDeleteReplies(List<Integer> ids);
}
