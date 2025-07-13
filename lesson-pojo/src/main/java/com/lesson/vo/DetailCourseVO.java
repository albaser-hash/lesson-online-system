package com.lesson.vo;

import com.lesson.entity.Chapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class DetailCourseVO extends  ScanCourseVO implements Serializable {

    private List<Chapter> ChapteList;
}
