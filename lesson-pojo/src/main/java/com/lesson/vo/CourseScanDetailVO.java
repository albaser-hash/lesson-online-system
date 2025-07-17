package com.lesson.vo;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseScanDetailVO extends ScanCourseVO implements Serializable {

    private List<ChapterPreviewVO> chapteList;
} 