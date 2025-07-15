package com.lesson.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CourseChapterMapper {
    @Update("UPDATE course_chapters SET doc_json=#{docJson} WHERE chapter_id=#{chapterId}")
    void updateDocJson(@Param("chapterId") Integer chapterId, @Param("docJson") String docJson);

    @Select("SELECT doc_json FROM course_chapters WHERE chapter_id=#{chapterId}")
    String getDocJson(@Param("chapterId") Integer chapterId);
    
    @Select("SELECT video_url FROM course_chapters WHERE chapter_id=#{chapterId}")
    String getVideoUrl(@Param("chapterId") Integer chapterId);

    @Update("UPDATE course_chapters SET video_url=#{videoUrl} WHERE chapter_id=#{chapterId}")
    void updateVideoUrl(@Param("chapterId") Integer chapterId, @Param("videoUrl") String videoUrl);
} 