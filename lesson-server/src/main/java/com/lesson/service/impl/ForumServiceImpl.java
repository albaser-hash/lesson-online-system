package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.dto.*;
import com.lesson.entity.*;
import com.lesson.mapper.*;
import com.lesson.result.PageResult;
import com.lesson.service.CourseService;
import com.lesson.service.ForumService;
import com.lesson.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.lesson.context.BaseContext.getCurrentId;

@Service
@Slf4j
public class ForumServiceImpl implements ForumService {
    @Autowired
    private ForumTopicsMapper forumTopicsMapper;
    @Autowired
    private ForumReplyMapper forumReplyMapper;
/*
创建
 */
    /**
     * 创建新的论坛主题
     *
     * 该方法接收一个论坛主题的数据传输对象（DTO），将其属性复制到一个新的论坛主题实体对象中，
     * 然后将该实体对象插入到数据库中同时返回包含新主题信息的值对象（VO）
     *
     * @param forumTopicsDTO 论坛主题的数据传输对象，包含要创建的新主题的信息
     * @return 返回一个论坛主题的值对象，包含新创建主题的ID和创建时间
     */
    @Override
    public ForumTopicsVO topics(ForumTopicsDTO forumTopicsDTO) {
        // 创建一个新的论坛主题实体对象
        ForumTopics  forumTopics = new ForumTopics();
        // 将DTO中的属性复制到实体对象中
        BeanUtils.copyProperties(forumTopicsDTO,forumTopics);
        // 设置回复数量为0，因为是新创建的主题
        forumTopics.setReplyCount(0);//初始数量为0
        // 设置创建时间为当前时间戳
        forumTopics.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        // 设置用户ID为当前用户的ID
        forumTopics.setUserId(getCurrentId());
        // 将论坛主题实体对象插入到数据库中
        forumTopicsMapper.insert(forumTopics);
        // 创建一个新的论坛主题值对象
        ForumTopicsVO forumTopicsVO = new ForumTopicsVO();
        // 设置值对象的主題ID为新创建主题的ID
        forumTopicsVO.setTopicId(forumTopics.getTopicId());
        // 设置值对象的发布时间为新创建主题的创建时间
        forumTopicsVO.setPublishTime(forumTopics.getCreateTime());
        // 返回包含新主题信息的值对象
        return forumTopicsVO;
    }

    /**
     * 处理论坛回复的函数
     *
     * 该函数负责将用户提交的回复内容保存到数据库中，并更新相应话题的回复数量
     * 它支持多级回复和@用户功能，通过记录父回复ID和被回复用户ID实现
     *
     * @param forumReplyDTO 包含用户提交的回复信息的数据传输对象
     * @return 返回一个布尔值，表示回复是否成功插入数据库
     */
    @Override
    public boolean reply(ForumReplyDTO forumReplyDTO) {
        ForumReply forumReply = new ForumReply();
        forumReply.setUserId(getCurrentId());
        BeanUtils.copyProperties(forumReplyDTO, forumReply);
        // 支持多级回复和@用户
        forumReply.setParentReplyId(forumReplyDTO.getParentReplyId());
        forumReply.setReplyToUserId(forumReplyDTO.getReplyToUserId());
        forumReply.setReplyToUserName(forumReplyDTO.getReplyToUserName());
        forumReply.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        Integer insert = forumReplyMapper.insert(forumReply);//插入回复
        ForumTopics forumTopics = forumTopicsMapper.selectById(forumReply.getTopicId());//更新回复数
        if (forumTopics != null) {
            forumTopics.setReplyCount(forumTopics.getReplyCount() + 1);
            forumTopicsMapper.updateById(forumTopics);
        }
        return insert != 0;
    }


    @Override
    public PageResult list(PageQueryDTO pageQueryDTO) {
        Page<ForumTopicDetailVO> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<ForumTopicDetailVO> pageResult = forumTopicsMapper.selectTopicPage(page);
        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public ForumTopicDetailVO detail(Integer topicId) {
        return forumTopicsMapper.selectTopicDetailById(topicId);
    }

    /**
     * 根据主题ID获取主题详情包括回复
     * 此方法首先从数据库中获取主题详情和所有相关回复，然后将回复组装成树形结构，最后返回包含主题和回复的主题详情对象
     *
     * @param topicId 主题ID，用于查询特定主题的详情和回复
     * @return 返回一个ForumTopicWithRepliesVO对象，其中包含主题详情和组装好的回复树
     */
    @Override
    public ForumTopicWithRepliesVO detailWithReplies(Integer topicId) {
        // 查询主题详情
        ForumTopicDetailVO topic = forumTopicsMapper.selectTopicDetailById(topicId);
        // 查询所有回复
        List<ForumReplyDetailVO> flatReplies = forumReplyMapper.selectRepliesByTopicId(topicId);

        // 组装多级children结构
        java.util.Map<Integer, ForumReplyDetailVO> idMap = new java.util.HashMap<>();
        java.util.List<ForumReplyDetailVO> rootReplies = new java.util.ArrayList<>();

        // 初始化所有回复的children列表
        for (ForumReplyDetailVO reply : flatReplies) {
            idMap.put(reply.getReplyId(), reply);
            reply.setChildren(new java.util.ArrayList<>());
        }

        // 按parentReplyId组装父子关系
        for (ForumReplyDetailVO reply : flatReplies) {
            if (reply.getParentReplyId() == null) {
                // parentReplyId为NULL的作为一级回复（兼容历史数据）
                rootReplies.add(reply);
            } else {
                // 有parentReplyId的作为子回复
                ForumReplyDetailVO parent = idMap.get(reply.getParentReplyId());
                if (parent != null) {
                    parent.getChildren().add(reply);
                } else {
                    // 容错：找不到父级也作为一级回复
                    rootReplies.add(reply);
                }
            }
        }

        // 创建并组装返回值
        ForumTopicWithRepliesVO vo = new ForumTopicWithRepliesVO();
        vo.setTopic(topic);
        vo.setReplies(rootReplies);
        return vo;
    }
    @Override
    public void updateTopic(ForumTopicsDTO forumTopicsDTO) {
        ForumTopics forumTopics = new ForumTopics();
        org.springframework.beans.BeanUtils.copyProperties(forumTopicsDTO, forumTopics);
        forumTopicsMapper.updateById(forumTopics);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Integer id) {
        // 先删除该帖子下的所有回复
        LambdaQueryWrapper<ForumReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(ForumReply::getTopicId, id);
        forumReplyMapper.delete(replyWrapper);
        
        // 再删除帖子本身
        forumTopicsMapper.deleteById(id);
    }

    @Override
    public void updateReply(ForumReplyDTO forumReplyDTO) {
        ForumReply forumReply = new ForumReply();
        org.springframework.beans.BeanUtils.copyProperties(forumReplyDTO, forumReply);
        forumReplyMapper.updateById(forumReply);
    }

    @Override
    public void deleteReply(Integer id) {
        // 递归删除所有子回复
        List<Integer> allIds = getAllReplyIdsToDelete(id);
        forumReplyMapper.deleteBatchIds(allIds);
        // 更新帖子的回复数（可选：可统计减少allIds.size()，此处保持原逻辑）
    }

    /**
     * 递归查找所有要删除的回复ID（含自身及所有子孙）
     * 此方法用于收集论坛中某个回复及其所有子回复的ID，以便进行批量删除操作
     * 它采用递归的方式，首先添加当前回复的ID，然后查询并递归处理所有子回复
     *
     * @param id 当前要处理的回复ID，从这个ID开始递归查找
     * @return 返回包含当前回复及其所有子回复ID的列表
     */
    private List<Integer> getAllReplyIdsToDelete(Integer id) {
        // 初始化一个列表，用于存储当前回复及其所有子回复的ID
        List<Integer> ids = new java.util.ArrayList<>();
        // 将当前回复的ID添加到列表中
        ids.add(id);
        // 查询所有子回复
        List<ForumReply> children = forumReplyMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ForumReply>().eq("parent_reply_id", id)
        );
        // 遍历所有子回复，并递归收集它们的ID
        for (ForumReply child : children) {
            ids.addAll(getAllReplyIdsToDelete(child.getReplyId()));
        }
        // 返回收集到的所有回复ID列表
        return ids;
    }

    @Override
    public ForumReplyDTO getReplyById(Integer id) {
        ForumReply forumReply = forumReplyMapper.selectById(id);
        ForumReplyDTO dto = new ForumReplyDTO();
        org.springframework.beans.BeanUtils.copyProperties(forumReply, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteTopics(List<Integer> ids) {
        // 先删除所有帖子下的回复
        LambdaQueryWrapper<ForumReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.in(ForumReply::getTopicId, ids);
        forumReplyMapper.delete(replyWrapper);
        
        // 再批量删除帖子
        forumTopicsMapper.deleteBatchIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteReplies(List<Integer> ids) {
        // 获取要删除的回复信息，用于更新帖子的回复数
        List<ForumReply> replies = forumReplyMapper.selectBatchIds(ids);
        
        // 删除回复
        forumReplyMapper.deleteBatchIds(ids);
        
        // 更新相关帖子的回复数
        for (ForumReply reply : replies) {
            ForumTopics topic = forumTopicsMapper.selectById(reply.getTopicId());
            if (topic != null && topic.getReplyCount() > 0) {
                topic.setReplyCount(topic.getReplyCount() - 1);
                forumTopicsMapper.updateById(topic);
            }
        }
    }
}