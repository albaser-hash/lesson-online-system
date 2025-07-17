package com.lesson.vo;

import com.lesson.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data

@NoArgsConstructor
@AllArgsConstructor

public class ScanCourseVO extends Course implements Serializable {

    private String teacherName;
    private String categoryName;
}
