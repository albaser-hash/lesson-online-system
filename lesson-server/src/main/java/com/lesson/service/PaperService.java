package com.lesson.service;

import com.lesson.dto.CreateExamDTO;
import com.lesson.dto.SubmitPaperDTO;
import com.lesson.vo.AttendExamVO;
import com.lesson.vo.CreateExamVO;
import com.lesson.vo.ScoreVO;

public interface PaperService {
    //提交试卷
    boolean SubmitPaper(SubmitPaperDTO submitPaperDTO);
    //分数
    ScoreVO ScorePaper(Integer examId);
}
