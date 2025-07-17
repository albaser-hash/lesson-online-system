package com.lesson.vo;

import com.lesson.entity.Chapter;
import com.lesson.entity.Paper;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class DetailCourseVO extends  ScanCourseVO implements Serializable {

    private List<Chapter> ChapteList;
}
