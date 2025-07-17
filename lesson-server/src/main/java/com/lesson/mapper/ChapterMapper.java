package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.Chapter;
import com.lesson.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChapterMapper extends BaseMapper<Chapter> {
    // 继承BaseMapper即可，insert/update/selectById/selectList等方法自动支持contentType
    // 通过章节ID更新video_url字段
    void updateVideoUrl(@Param("chapterId") Integer chapterId, @Param("videoUrl") String videoUrl, @Param("videoDuration") String videoDuration);
    void updateVideoSize(@Param("chapterId") Integer chapterId, @Param("videoSize") Long videoSize);

    void updateVideoDuration(@Param("chapterId") Integer chapterId, @Param("videoDuration") String videoDuration);
}
