package com.lesson.controller.comm;

import com.lesson.annotation.CheckPermission;
import com.lesson.dto.ForumReplyDTO;
import com.lesson.dto.ForumTopicsDTO;
import com.lesson.dto.PageQueryDTO;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.ForumService;
import com.lesson.vo.ForumTopicsVO;
import com.lesson.vo.ForumTopicWithRepliesVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/forum")
@Api(tags = "论坛")
@Slf4j

public class ForumController {
    @Autowired
    private ForumService forumService;

    /*
        发布论坛
     */
    @PostMapping("/topics")
    @ApiOperation("发布论坛")
    public Result<ForumTopicsVO> topics(@RequestBody ForumTopicsDTO forumTopicsDTO){//json格式，加注解
        ForumTopicsVO forumTopicsVO = forumService.topics(forumTopicsDTO);
        return Result.success(forumTopicsVO);
    }
    /*
        回复论坛
     */
    @PostMapping("/reply")
    @ApiOperation("回复论坛")
    public Result reply(@RequestBody ForumReplyDTO forumReplyDTO){//json格式，加注解
        if(forumService.reply(forumReplyDTO)){
            return Result.success("回复成功");
        }
        return Result.error("回复失败");
    }

    /**
     * 分页查询帖子
     * @param pageQueryDTO
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("分页查询帖子")
    public Result<PageResult> list(PageQueryDTO pageQueryDTO){
        PageResult pageResult = forumService.list(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查看贴子详情（含回复）
     */
    @GetMapping("/detail")
    @ApiOperation("查看贴子详情（含回复）")
    public Result<ForumTopicWithRepliesVO> detail(Integer topicId) {
        ForumTopicWithRepliesVO detail = forumService.detailWithReplies(topicId);
        return Result.success(detail);
    }

    @PutMapping("/topics")
    @ApiOperation("修改帖子")
    @CheckPermission(resourceType = "forum_topic", operation = "update")
    public Result<String> updateTopic(@RequestBody ForumTopicsDTO forumTopicsDTO) {
        forumService.updateTopic(forumTopicsDTO);
        return Result.success("修改成功");
    }

    @DeleteMapping("/topics/{id}")
    @ApiOperation("删除帖子")
    @CheckPermission(resourceType = "forum_topic", operation = "delete")
    public Result<String> deleteTopic(@PathVariable Integer id) {
        forumService.deleteTopic(id);
        return Result.success("删除成功");
    }

    @PutMapping("/reply")
    @ApiOperation("修改回复")
    @CheckPermission(resourceType = "forum_reply", operation = "update")
    public Result<String> updateReply(@RequestBody ForumReplyDTO forumReplyDTO) {
        forumService.updateReply(forumReplyDTO);
        return Result.success("修改成功");
    }

    @DeleteMapping("/reply/{id}")
    @ApiOperation("删除回复")
    @CheckPermission(resourceType = "forum_reply", operation = "delete")
    public Result<String> deleteReply(@PathVariable Integer id) {
        forumService.deleteReply(id);
        return Result.success("删除成功");
    }

    @GetMapping("/reply/{id}")
    @ApiOperation("查询单条回复")
    public Result<ForumReplyDTO> getReplyById(@PathVariable Integer id) {
        return Result.success(forumService.getReplyById(id));
    }

    @DeleteMapping("/topics/batch")
    @ApiOperation("批量删除帖子")
    public Result<String> batchDeleteTopics(@RequestBody List<Integer> ids) {
        forumService.batchDeleteTopics(ids);
        return Result.success("批量删除成功");
    }

    @DeleteMapping("/reply/batch")
    @ApiOperation("批量删除回复")
    public Result<String> batchDeleteReplies(@RequestBody List<Integer> ids) {
        forumService.batchDeleteReplies(ids);
        return Result.success("批量删除成功");
    }
}
