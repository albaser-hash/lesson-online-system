package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesson.dto.*;
import com.lesson.entity.*;
import com.lesson.mapper.*;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.CourseService;
import com.lesson.service.ExamService;
import com.lesson.vo.*;
import com.lesson.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lesson.context.BaseContext.getCurrentId;

@Service
@Slf4j
public class ExamServiceImpl implements ExamService {
    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Autowired
    private ExamPaperMapper examPaperMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CourseMapper courseMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建考试
     * 该方法用于创建一个新的考试记录及其相关题目，并通知相关学生
     *
     * @param createExamDTO 创建考试的数据传输对象，包含考试的基本信息和题目信息
     * @return 返回创建考试的响应对象，包含考试ID和创建时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ExamVO> createExam(CreateExamDTO createExamDTO) {
        try {
            // 检查课程是否存在且属于当前教师
            Course course = courseMapper.selectById(createExamDTO.getCourseId());
            if (course == null) {
                return Result.error("课程不存在");
            }
            
            Integer currentTeacherId = getCurrentId();
            if (!course.getTeacherId().equals(currentTeacherId)) {
                return Result.error("只能为自己的课程创建考试");
            }
            
            // 创建考试
        Exam exam = new Exam();
        exam.setCourseId(createExamDTO.getCourseId());
            exam.setTeacherId(currentTeacherId);
        exam.setExamName(createExamDTO.getExamName());
            exam.setExamCount(createExamDTO.getQuestions().size());
            
            // 转换时间格式
            if (createExamDTO.getStartTime() != null) {
                try {
                    if (createExamDTO.getStartTime() instanceof Timestamp) {
                        exam.setStartTime((Timestamp) createExamDTO.getStartTime());
                    } else {
                        // 如果是其他格式，尝试转换
                        long timeInMillis = createExamDTO.getStartTime().getTime();
                        exam.setStartTime(new Timestamp(timeInMillis));
                    }
                } catch (Exception e) {
                    log.error("转换开始时间失败", e);
                    return Result.error("开始时间格式错误");
                }
            }
            if (createExamDTO.getEndTime() != null) {
                try {
                    if (createExamDTO.getEndTime() instanceof Timestamp) {
                        exam.setEndTime((Timestamp) createExamDTO.getEndTime());
                    } else {
                        // 如果是其他格式，尝试转换
                        long timeInMillis = createExamDTO.getEndTime().getTime();
                        exam.setEndTime(new Timestamp(timeInMillis));
                    }
                } catch (Exception e) {
                    log.error("转换结束时间失败", e);
                    return Result.error("结束时间格式错误");
                }
            }
            
        exam.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
            
        examMapper.insert(exam);

            // 创建题目
            for (CreateExamDTO.ExamQuestionDTO questionDTO : createExamDTO.getQuestions()) {
                ExamQuestion question = new ExamQuestion();
                question.setExamId(exam.getExamId());
                question.setQuestionType(questionDTO.getQuestionType());
                question.setQuestionContent(questionDTO.getQuestionContent());
                question.setOptions(questionDTO.getOptions());
                question.setCorrectAnswer(questionDTO.getCorrectAnswer());
                question.setScore(questionDTO.getScore());
                question.setDifficulty(questionDTO.getDifficulty());
                question.setExplanation(questionDTO.getExplanation());
                question.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
                
                examQuestionMapper.insert(question);
            }
            
            // 构建返回VO
            ExamVO examVO = new ExamVO();
            examVO.setExamId(exam.getExamId());
            examVO.setCourseId(exam.getCourseId());
            examVO.setCourseName(course.getCourseName());
            examVO.setTeacherId(exam.getTeacherId());
            examVO.setExamName(exam.getExamName());
            examVO.setExamCount(exam.getExamCount());
            examVO.setStartTime(exam.getStartTime());
            examVO.setEndTime(exam.getEndTime());
            examVO.setCreateTime(exam.getCreateTime());
            examVO.setStatus("UPCOMING");
            examVO.setHasSubmitted(false);
            
            return Result.success(examVO);
            
        } catch (Exception e) {
            log.error("创建考试失败", e);
            return Result.error("创建考试失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> publishExam(Integer examId) {
        try {
            // 检查考试是否存在
            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            // 检查权限
            Integer currentTeacherId = getCurrentId();
            if (!exam.getTeacherId().equals(currentTeacherId)) {
                return Result.error("只能发布自己的考试");
            }
            // 获取购买该课程的学生
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getCourseId, exam.getCourseId())
                       .eq(Order::getOrderStatus, "PAID");
            List<Order> orders = orderMapper.selectList(orderWrapper);


            // 查询已提交该考试的学生
            List<ExamPaper> submittedPapers = examPaperMapper.selectList(
                new LambdaQueryWrapper<ExamPaper>().eq(ExamPaper::getExamId, examId)
            );
            Set<Integer> submittedUserIds = submittedPapers.stream().map(ExamPaper::getUserId).collect(Collectors.toSet());

            // 只通知未提交的学生
            List<Integer> allUserIds = orders.stream().map(Order::getUserId).collect(Collectors.toList());
            List<Integer> toNotifyUserIds = allUserIds.stream()
                .filter(uid -> !submittedUserIds.contains(uid))
                .collect(Collectors.toList());

            // 发送通知
            for (Order order : orders) {
                if (!toNotifyUserIds.contains(order.getUserId())) continue;
                Notification notification = new Notification();
                notification.setUserId(order.getUserId());
                notification.setType("EXAM");
                Timestamp startTime = exam.getStartTime();
                LocalDateTime localDateTime = startTime.toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = localDateTime.format(formatter);

                notification.setContent("您购买的课程《" + exam.getExamName() + "》有新考试，时间为:"+formattedTime+"请及时参加");
                notification.setIsRead(false);
                notification.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
                notification.setLink("/student/exam"); // 自动跳转到考试列表
                notificationMapper.insert(notification);
            }
            // 发送WebSocket通知（按userId推送）
            WebSocketServer.sendExamNotificationToUsers(
                toNotifyUserIds,
                exam.getExamName(),
                "您购买的课程《" + exam.getExamName() + "》有新考试，请及时参加"
            );
            return Result.success("考试发布成功，已通知" + toNotifyUserIds.size() + "名学生");
        } catch (Exception e) {
            log.error("发布考试失败", e);
            return Result.error("发布考试失败");
        }
    }
    @Override
    public List<ExamVO> getStudentExams(Integer userId) {
        return examMapper.selectStudentExams(userId);
    }

    @Override
    public List<ExamVO> getTeacherExams(Integer teacherId) {
        return examMapper.selectTeacherExams(teacherId);
    }

    /**
     * 获取学生考试详情
     *
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 返回考试详情对象，如果学生未购买课程或考试不存在，则返回null
     */
    @Override
    public ExamDetailVO getExamDetailForStudent(Integer examId, Integer userId) {
        // 检查学生是否购买了该课程
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return null;
        }

        // 获取课程信息
        Course course = courseMapper.selectById(exam.getCourseId());

        // 查询学生是否已购买该课程
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, userId)
                   .eq(Order::getCourseId, exam.getCourseId())
                   .eq(Order::getOrderStatus, "PAID");
        Order order = orderMapper.selectOne(orderWrapper);
        if (order == null) {
            return null;
        }
        
        // 检查是否已提交
        LambdaQueryWrapper<ExamPaper> paperWrapper = new LambdaQueryWrapper<>();
        paperWrapper.eq(ExamPaper::getUserId, userId)
                   .eq(ExamPaper::getExamId, examId);
        ExamPaper paper = examPaperMapper.selectOne(paperWrapper);
        
        // 构建考试详情
        ExamDetailVO examDetail = new ExamDetailVO();
        examDetail.setExamId(exam.getExamId());
        examDetail.setExamName(exam.getExamName());
        examDetail.setCourseName(course != null ? course.getCourseName() : "未知课程");
        User user = userMapper.selectById(exam.getTeacherId());
        examDetail.setTeacherName(user.getName() != null ? user.getName() : user.getUserName());
        examDetail.setExamCount(exam.getExamCount());
        examDetail.setStartTime(exam.getStartTime());
        examDetail.setEndTime(exam.getEndTime());
        examDetail.setCreateTime(exam.getCreateTime());
        examDetail.setIsSubmitted(paper != null);
        examDetail.setScore(paper != null ? paper.getScore() : null);
        examDetail.setSubmitTime(paper != null ? paper.getSubmitTime() : null);
        
        // 获取题目（不包含答案）
        List<ExamDetailVO.ExamQuestionVO> questions = examQuestionMapper.selectExamQuestionsForStudent(examId);
        examDetail.setQuestions(questions);
        
        return examDetail;
    }

    /**
     * 获取教师视角的考试详情
     *
     * 此方法首先根据考试ID获取考试信息，然后检查当前教师是否有权限查看该考试
     * 如果有权限，则进一步获取课程信息，并构建考试详情对象返回
     *
     * @param examId 考试ID
     * @return ExamDetailVO 教师视角的考试详情，如果考试不存在或当前教师无权查看，则返回null
     */
    @Override
    public ExamDetailVO getExamDetailForTeacher(Integer examId) {
        // 根据考试ID获取考试信息
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return null;
        }
        
        // 检查权限
        Integer currentTeacherId = getCurrentId();
        if (!exam.getTeacherId().equals(currentTeacherId)) {
            return null;
        }
        
        // 获取课程信息
        Course course = courseMapper.selectById(exam.getCourseId());
        
        // 构建考试详情
        ExamDetailVO examDetail = new ExamDetailVO();
        examDetail.setExamId(exam.getExamId());
        examDetail.setExamName(exam.getExamName());
        examDetail.setCourseName(course != null ? course.getCourseName() : "未知课程");
        examDetail.setTeacherName(getTeacherName(currentTeacherId)); // 教师查看自己的考试
        examDetail.setExamCount(exam.getExamCount());
        examDetail.setStartTime(exam.getStartTime());
        examDetail.setEndTime(exam.getEndTime());
        examDetail.setCreateTime(exam.getCreateTime());
        
        // 获取题目（包含答案）
        List<ExamDetailVO.ExamQuestionVO> questions = examQuestionMapper.selectExamQuestions(examId);
        examDetail.setQuestions(questions);
        
        return examDetail;
    }

@Override
@Transactional(rollbackFor = Exception.class)
/**
 * 提交考试方法
 *
 * @param submitExamDTO 考试提交信息，包含考试ID和答案
 * @param userId 用户ID，用于关联考试和用户
 * @return 返回结果，包含提交状态和得分信息
 */
public Result<String> submitExam(SubmitExamDTO submitExamDTO, Integer userId) {
    try {
        // 检查考试是否存在且在有效时间内
        Exam exam = examMapper.selectById(submitExamDTO.getExamId());
        if (exam == null) {
            return Result.error("考试不存在");
        }
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (now.before(exam.getStartTime()) || now.after(exam.getEndTime())) {
            return Result.error("考试时间已过或未开始");
        }
        // 检查是否已提交
        LambdaQueryWrapper<ExamPaper> paperWrapper = new LambdaQueryWrapper<>();
        paperWrapper.eq(ExamPaper::getUserId, userId)
                   .eq(ExamPaper::getExamId, submitExamDTO.getExamId());
        ExamPaper existingPaper = examPaperMapper.selectOne(paperWrapper);
        if (existingPaper != null) {
            return Result.error("您已经提交过该考试");
        }
        // 自动判分（只判客观题）
        int autoScore = 0;
        Map<Integer, String> answers = submitExamDTO.getAnswers();
        for (Map.Entry<Integer, String> entry : answers.entrySet()) {
            Integer questionId = entry.getKey();
            String userAnswer = entry.getValue();
            ExamQuestion question = examQuestionMapper.selectById(questionId);
            if (question != null && question.getExamId().equals(submitExamDTO.getExamId())) {
                String type = question.getQuestionType();
                if ("SINGLE_CHOICE".equals(type) || "MULTIPLE_CHOICE".equals(type) || "JUDGE".equals(type)) {
                    boolean isCorrect = checkAnswer(question, userAnswer);
                    if (isCorrect) {
                        autoScore += question.getScore();
                    }
                }
                // TEXT题型不判分，留给老师批改
            }
        }
        // 保存答卷
        ExamPaper paper = new ExamPaper();
        paper.setUserId(userId);
        paper.setExamId(submitExamDTO.getExamId());
        paper.setAutoScore(autoScore);
        paper.setScore(autoScore); // 兼容旧代码
        paper.setIsReviewed(false);
        paper.setFinalScore(null);
        paper.setReviewTime(null);
        paper.setSubmitTime(now);
        paper.setMarkTime(now);
        // 将答案转换为JSON
        try {
            paper.setAnswer(objectMapper.writeValueAsString(answers));
        } catch (JsonProcessingException e) {
            log.error("答案序列化失败", e);
            return Result.error("提交失败");
        }
        // 判断是否有主观题
        List<ExamQuestion> questions = examQuestionMapper.selectList(
            new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, submitExamDTO.getExamId())
        );
        boolean hasText = questions.stream().anyMatch(q -> "TEXT".equals(q.getQuestionType()));
        if (!hasText) {
            paper.setIsReviewed(true);
            paper.setFinalScore(autoScore);
            paper.setReviewTime(now);
        }
        examPaperMapper.insert(paper);

        // 获取考试信息用于通知
        String examName = exam.getExamName().isEmpty() ?  "考试" : exam.getExamName();

        // 发送批改完成通知（仅无主观题时）
        if (!hasText) {
            Notification notification = new Notification();
            notification.setUserId(paper.getUserId());
            notification.setType("EXAM");
            notification.setContent("您的考试《" + examName + "》已批改完成，最终得分：" + (paper.getFinalScore() != null ? paper.getFinalScore() : paper.getAutoScore()) + "分");
            notification.setIsRead(false);
            notification.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
            notification.setLink("/student/exam"); // 跳转到考试列表查看详情
            notificationMapper.insert(notification);
        } else {
            // 有主观题，推送批改通知给老师
            Integer teacherId = exam.getTeacherId();
            if (teacherId != null) {
                // 1. 保存数据库通知（老师可以在通知列表看到）
                Notification teacherNotification = new Notification();
                teacherNotification.setUserId(teacherId);
                teacherNotification.setType("EXAM_REVIEW");
                teacherNotification.setContent("学生提交了考试《" + examName + "》，包含主观题需要您批改");
                teacherNotification.setIsRead(false);
                teacherNotification.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
                teacherNotification.setLink("/teacher/exam/review");
                notificationMapper.insert(teacherNotification);
                
                List<Integer> teacherIds = java.util.Collections.singletonList(teacherId);
                String reviewMsg = "有新的主观题需要您批改，请及时处理。";
                com.lesson.websocket.WebSocketServer.sendReviewNotificationToUsers(teacherIds, examName, reviewMsg);
            }
        }

        return Result.success("考试提交成功，客观题得分：" + autoScore + (hasText ? "，主观题待老师批改" : ""));
    } catch (Exception e) {
        log.error("提交考试失败", e);
        return Result.error("提交考试失败");
    }
}


/**
 * 获取考试结果详情
 *
 * @param examId 考试ID
 * @param userId 用户ID
 * @return 考试结果的详细信息，如果没有找到则返回null
 */
@Override
public ExamDetailVO getExamResult(Integer examId, Integer userId) {
    // 检查是否已提交
    LambdaQueryWrapper<ExamPaper> paperWrapper = new LambdaQueryWrapper<>();
    paperWrapper.eq(ExamPaper::getUserId, userId)
               .eq(ExamPaper::getExamId, examId);
    ExamPaper paper = examPaperMapper.selectOne(paperWrapper);

    if (paper == null) {
        return null;
    }

    // 获取考试信息
    Exam exam = examMapper.selectById(examId);
    if (exam == null) {
        return null;
    }

    // 获取课程信息
    Course course = courseMapper.selectById(exam.getCourseId());

    // 构建考试结果
    ExamDetailVO examDetail = new ExamDetailVO();
    examDetail.setExamId(exam.getExamId());
    examDetail.setExamName(exam.getExamName());
    examDetail.setCourseName(course != null ? course.getCourseName() : "未知课程");

    examDetail.setTeacherName(getTeacherName(exam.getTeacherId()));
    examDetail.setExamCount(exam.getExamCount());
    examDetail.setStartTime(exam.getStartTime());
    examDetail.setEndTime(exam.getEndTime());
    examDetail.setCreateTime(exam.getCreateTime());
    examDetail.setIsSubmitted(true);
    // 新增：返回isReviewed、finalScore、autoScore
    examDetail.setIsReviewed(paper.getIsReviewed());
    examDetail.setFinalScore(paper.getFinalScore());
    examDetail.setAutoScore(paper.getAutoScore());
    // 分数逻辑：优先用finalScore
    if (paper.getIsReviewed() != null && paper.getIsReviewed()) {
        examDetail.setScore(paper.getFinalScore());
    } else {
        examDetail.setScore(paper.getAutoScore());
    }
    examDetail.setSubmitTime(paper.getSubmitTime());

    // 解析主观题得分
    Map<String, Integer> subjectiveScoreMap = new HashMap<>();
    if (paper.getSubjectiveScores() != null) {
        try {
            subjectiveScoreMap = objectMapper.readValue(paper.getSubjectiveScores(), Map.class);
        } catch (Exception e) {
            log.error("解析主观题得分失败", e);
        }
    }

    // 获取题目和答案
    List<ExamDetailVO.ExamQuestionVO> questions = examQuestionMapper.selectExamQuestions(examId);

    // 解析学生答案
    try {
        Map<Integer, String> userAnswers = objectMapper.readValue(paper.getAnswer(), Map.class);
        int totalScore = 0;
        int allScore = 0;
        int objectiveCorrectCount = 0; // 客观题正确数量
        int objectiveTotalCount = 0;   // 客观题总数量

        for (ExamDetailVO.ExamQuestionVO question : questions) {
            String userAnswer = userAnswers.get(question.getQuestionId());
            if (userAnswer == null) {
                userAnswer = userAnswers.get(String.valueOf(question.getQuestionId()));
            }
            question.setUserAnswer(userAnswer);

            // 检查答案是否正确
            ExamQuestion examQuestion = examQuestionMapper.selectById(question.getQuestionId());
            if (examQuestion != null) {
                // 处理简答题（主观题）
                if ("TEXT".equals(examQuestion.getQuestionType())) {
                    // 简答题完全依赖老师批改结果
                    Integer subjScore = subjectiveScoreMap.get(String.valueOf(question.getQuestionId()));
                    question.setObtainedScore(subjScore != null ? subjScore : 0);
                    // 简答题的正确性基于是否得分
                    question.setIsCorrect(subjScore != null && subjScore > 0);
                    // 简答题不参与客观题统计
                } else {
                    // 客观题（单选题、多选题、判断题）
                    boolean isCorrect = checkAnswer(examQuestion, userAnswer);
                    question.setIsCorrect(isCorrect);
                    question.setObtainedScore(isCorrect ? question.getScore() : 0);
                    // 客观题参与统计
                    objectiveTotalCount++;
                    if (isCorrect) {
                        objectiveCorrectCount++;
                        totalScore += question.getScore() != null ? question.getScore() : 0;
                    }
                }
                // 所有题目都计入总分
                allScore += question.getScore() != null ? question.getScore() : 0;
            }
        }

        examDetail.setTotalScore(allScore);
        // 统计所有题目 isCorrect==true 的 correctCount
        int correctCount = 0;
        for (ExamDetailVO.ExamQuestionVO question : questions) {
            if (Boolean.TRUE.equals(question.getIsCorrect())) {
                correctCount++;
            }
        }
        examDetail.setCorrectCount(correctCount);
        double accuracy = objectiveTotalCount > 0 ? (double) objectiveCorrectCount / objectiveTotalCount * 100 : 0.0;
        examDetail.setAccuracy(Math.round(accuracy * 100.0) / 100.0);

    } catch (Exception e) {
        log.error("解析答案失败", e);
    }

    examDetail.setQuestions(questions);

    return examDetail;
}

    @Override
    public Result<Object> getExamStatistics(Integer examId) {
        try {
            // 检查权限
            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            Integer currentTeacherId = getCurrentId();
            if (!exam.getTeacherId().equals(currentTeacherId)) {
                return Result.error("无权限查看");
            }
            
            // 获取考试所有答卷
            LambdaQueryWrapper<ExamPaper> paperWrapper = new LambdaQueryWrapper<>();
            paperWrapper.eq(ExamPaper::getExamId, examId);
            List<ExamPaper> papers = examPaperMapper.selectList(paperWrapper);
            int totalStudents = papers.size();
            
            // 获取所有题目及分值
            List<ExamQuestion> questions = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, examId));
            int totalQuestions = questions.size();
            int totalPossibleScore = questions.stream().mapToInt(q -> q.getScore() == null ? 0 : q.getScore()).sum();
            
            // 及格线为总分的60%
            int passLine = (int)Math.round(totalPossibleScore * 0.6);
            int totalScore = 0;
            int maxScore = 0;
            int minScore = totalPossibleScore;
            int totalCorrect = 0;
            int objectiveTotalCount = 0; // 客观题总数量
            
            for (ExamPaper paper : papers) {
                // 使用最终分数而不是自动分数
                int finalScore;
                if (paper.getIsReviewed() != null && paper.getIsReviewed() && paper.getFinalScore() != null) {
                    finalScore = paper.getFinalScore();
                } else {
                    finalScore = paper.getAutoScore() != null ? paper.getAutoScore() : 0;
                }
                
                // 添加调试日志
                log.info("学生ID: {}, autoScore: {}, finalScore: {}, isReviewed: {}, 使用的分数: {}", 
                    paper.getUserId(), paper.getAutoScore(), paper.getFinalScore(), paper.getIsReviewed(), finalScore);
                
                totalScore += finalScore;
                if (finalScore > maxScore) maxScore = finalScore;
                if (finalScore < minScore) minScore = finalScore;
                
                // 统计该学生答对题数（只统计客观题）
                int correctCount = 0;
                try {
                    Map<Integer, String> userAnswers = objectMapper.readValue(paper.getAnswer(), Map.class);
                    for (ExamQuestion q : questions) {
                        // 只统计客观题
                        if (!"TEXT".equals(q.getQuestionType())) {
                            String userAnswer = userAnswers.get(q.getQuestionId());
                            if (userAnswer == null) userAnswer = userAnswers.get(String.valueOf(q.getQuestionId()));
                            if (checkAnswer(q, userAnswer)) {
                                correctCount++;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析学生答案失败", e);
                }
                totalCorrect += correctCount;
            }
            
            // 计算客观题总数量
            objectiveTotalCount = (int) questions.stream().filter(q -> !"TEXT".equals(q.getQuestionType())).count();
            
            double averageScore = totalStudents > 0 ? (double) totalScore / totalStudents : 0;
            
            // 及格人数（基于最终分数）
            long passCount = papers.stream().filter(p -> {
                int finalScore;
                if (p.getIsReviewed() != null && p.getIsReviewed() && p.getFinalScore() != null) {
                    finalScore = p.getFinalScore();
                } else {
                    finalScore = p.getAutoScore() != null ? p.getAutoScore() : 0;
                }
                return finalScore >= passLine;
            }).count();
            
            double passRate = totalStudents > 0 ? (double) passCount / totalStudents * 100 : 0;
            
            // 正确率 = 所有学生客观题答对题数 / (客观题总数量 * 总学生数) * 100
            double correctRate = (totalStudents > 0 && objectiveTotalCount > 0) 
                ? (double) totalCorrect / (objectiveTotalCount * totalStudents) * 100 : 0;
            
            // 构建统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalStudents", totalStudents);
            statistics.put("totalScore", totalScore);
            statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
            statistics.put("maxScore", maxScore);
            statistics.put("minScore", minScore);
            statistics.put("highestScore", maxScore);
            statistics.put("lowestScore", minScore);
            statistics.put("passRate", Math.round(passRate * 100.0) / 100.0);
            statistics.put("correctRate", Math.round(correctRate * 100.0) / 100.0);
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取考试统计失败", e);
            return Result.error("获取统计信息失败");
        }
    }
    // 检查答案是否正确
    /**
     * 根据题目类型和用户答案检查答案是否正确。
     *
     * @param question 考试题对象，包含正确答案和题目类型。
     * @param userAnswer 用户提供的答案字符串。
     * @return 如果答案正确则返回true，否则返回false。
     */
    private boolean checkAnswer(ExamQuestion question, String userAnswer) {
        // 空答案检查，如果用户答案为空或只包含空白字符，则返回false。
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        // 获取正确答案和用户答案（去除前后空格）。
        String correctAnswer = question.getCorrectAnswer();
        String userAnswerTrim = userAnswer.trim();

        // 根据题目类型检查答案。
        switch (question.getQuestionType()) {
            case "SINGLE_CHOICE":
            case "JUDGE":
                // 对于单选题和判断题，直接比较用户答案和正确答案是否一致（不区分大小写）。
                return userAnswerTrim.equalsIgnoreCase(correctAnswer);
            case "MULTIPLE_CHOICE":
                // 多选题答案可能是逗号分隔的多个选项。
                // 需要将用户答案和正确答案都按逗号分割并进行比较。
                String[] correctOptions = correctAnswer.split(",");
                String[] userOptions = userAnswerTrim.split(",");
                // 如果选项数量不一致，则返回false。
                if (correctOptions.length != userOptions.length) {
                    return false;
                }
                // 遍历每个正确选项，检查是否在用户答案中找到匹配项。
                for (String option : correctOptions) {
                    boolean found = false;
                    for (String userOption : userOptions) {
                        if (option.trim().equalsIgnoreCase(userOption.trim())) {
                            found = true;
                            break;
                        }
                    }
                    // 如果某个正确选项在用户答案中未找到，则返回false。
                    if (!found) {
                        return false;
                    }
                }
                // 所有选项都匹配成功，则返回true。
                return true;
            case "TEXT":
                // 简答题不参与自动判断，需要老师手动批改。
                return false;
            default:
                // 默认情况下返回false，表示答案不正确。
                return false;
        }
    }
    /**
     * 更新考试状态（定时任务调用）
     */
    public void updateExamStatus() {
        try {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            // 获取所有考试
            List<Exam> exams = examMapper.selectList(null);
            for (Exam exam : exams) {
                String currentStatus = getExamStatus(exam, now);
                // 如果状态发生变化，发送通知
                if (!currentStatus.equals(getCurrentExamStatus(exam))) {
                    // 查找所有已购该课程的学生
                    LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
                    orderWrapper.eq(Order::getCourseId, exam.getCourseId())
                                .eq(Order::getOrderStatus, "PAID");
                    List<Order> orders = orderMapper.selectList(orderWrapper);
                    List<Integer> userIds = orders.stream().map(Order::getUserId).collect(Collectors.toList());
                    
                    // 插入状态变更通知
                    for (Order order : orders) {
                        Notification notification = new Notification();
                        notification.setUserId(order.getUserId());
                        notification.setType("EXAM");
                        notification.setContent("考试《" + exam.getExamName() + "》状态已更新为：" + getStatusText(currentStatus));
                        notification.setIsRead(false);
                        notification.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
                        notification.setLink("/student/exam"); // 跳转到考试列表
                        notificationMapper.insert(notification);
                    }
                    
                    WebSocketServer.sendExamStatusUpdateToUsers(
                        userIds,
                        exam.getExamName(),
                        currentStatus
                    );
                }
            }
        } catch (Exception e) {
            log.error("更新考试状态失败", e);
        }
    }
    /**
     * 获取考试状态
     */
    private String getExamStatus(Exam exam, Timestamp now) {
        if (now.before(exam.getStartTime())) {
            return "UPCOMING";
        } else if (now.after(exam.getEndTime())) {
            return "FINISHED";
        } else {
            return "ONGOING";
        }
    }

    /**
     * 获取当前考试状态（根据数据库时间判断）
     */
    private String getCurrentExamStatus(Exam exam) {
        Date now = new Date();
        if (now.before(exam.getStartTime())) {
            return "UPCOMING";
        } else if (now.after(exam.getEndTime())) {
            return "FINISHED";
        } else {
            return "ONGOING";
        }
    }

    @Override
    public void setAllExamsToNowByTeacherId(Long teacherId) {
        Date now = new Date();
        Date end = new Date(System.currentTimeMillis() + 3600_000); // 1小时后
        examMapper.updateExamsTimeByTeacherId(teacherId, now, end);
    }

    /**
     * 根据教师ID删除该教师创建的所有考试及相关数据
     * 这包括考试本身、与考试相关的所有答卷、题目、通知以及成绩统计（如果存在单独的成绩统计表）
     *
     * @param teacherId 教师的唯一标识符
     * @return 删除的考试数量，如果没有找到任何考试则返回0
     */
    @Override
    public int deleteAllExamsByTeacherId(Long teacherId) {
        // 查出该老师所有考试ID
        List<Exam> exams = examMapper.selectList(new QueryWrapper<Exam>().eq("teacher_id", teacherId));
        if (exams.isEmpty()) return 0;
        List<Integer> examIds = exams.stream().map(Exam::getExamId).collect(java.util.stream.Collectors.toList());
        // 删除所有答卷
        examPaperMapper.delete(new QueryWrapper<com.lesson.entity.ExamPaper>().in("exam_id", examIds));
        // 删除所有题目
        examQuestionMapper.delete(new QueryWrapper<com.lesson.entity.ExamQuestion>().in("exam_id", examIds));
        // 删除所有相关通知（type='EXAM'且content包含考试名）
        for (Exam exam : exams) {
            notificationMapper.delete(new QueryWrapper<com.lesson.entity.Notification>()
                .eq("type", "EXAM")
                .like("content", exam.getExamName()));
        }
        // 删除考试
        return examMapper.delete(new QueryWrapper<com.lesson.entity.Exam>().eq("teacher_id", teacherId));
    }

    /**
     * 老师批改主观题接口
     */
    @Override
    public Result<?> reviewPaper(ReviewDTO reviewDTO) {
        // 获取试卷ID和主观题得分
        Integer paperId = reviewDTO.getPaperId();
        Map<Integer, Integer> subjectiveScores = reviewDTO.getSubjectiveScores();

        // 根据ID查询试卷信息
        ExamPaper paper = examPaperMapper.selectById(paperId);
        // 如果试卷不存在，返回错误信息
        if (paper == null) {
            return Result.error("试卷不存在");
        }
        // 如果试卷已批改，返回错误信息
        if (Boolean.TRUE.equals(paper.getIsReviewed())) {
            return Result.error("该试卷已批改");
        }

        // 计算主观题总分
        int subjectiveScore = subjectiveScores.values().stream().mapToInt(Integer::intValue).sum();
        // 合并总分
        int finalScore = (paper.getAutoScore() == null ? 0 : paper.getAutoScore()) + subjectiveScore;

        // 更新试卷信息
        paper.setFinalScore(finalScore);
        paper.setIsReviewed(true);
        paper.setReviewTime(new Timestamp(System.currentTimeMillis()));

        // 保存主观题得分
        try {
            paper.setSubjectiveScores(objectMapper.writeValueAsString(subjectiveScores));
        } catch (Exception e) {
            log.error("保存主观题得分失败", e);
        }

        // 更新试卷信息到数据库
        examPaperMapper.updateById(paper);

        // 获取考试信息用于通知
        Exam exam = examMapper.selectById(paper.getExamId());
        String examName = exam != null ? exam.getExamName() : "考试";

        // 发送批改完成通知
        Notification notification = new Notification();
        notification.setUserId(paper.getUserId());
        notification.setType("EXAM");
        notification.setContent("您的考试《" + examName + "》已批改完成，最终得分：" + finalScore + "分");
        notification.setIsRead(false);
        notification.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        notification.setLink("/student/exam"); // 跳转到考试列表查看详情

        // 插入通知到数据库
        notificationMapper.insert(notification);

        // 返回批改结果
        return Result.success("批改成功，总分：" + finalScore);
    }
    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "UPCOMING": return "未开始";
            case "ONGOING": return "进行中";
            case "FINISHED": return "已结束";
            default: return status;
        }
    }

private String getTeacherName(Integer teacherId) {
    User teacher = userMapper.selectById(teacherId);

    if (teacher == null) {
        return "未知教师";
    }
   String name =  teacher.getName() == null ? teacher.getUserName() : teacher.getName();
    return name.isEmpty()  ? teacher.getUserName() : name;
}

    /**
     * 更新考试信息
     *
     * 此方法用于处理考试信息的更新请求，包括考试名称、时间、题目等信息
     * 它首先检查考试是否存在，然后验证当前用户是否有权限进行编辑，
     * 接着检查考试是否已经开始或是否有学生已经提交答卷，以确定是否可以编辑，
     * 最后，它更新考试的基本信息，删除旧的题目并添加新的题目
     *
     * @param updateExamDTO 包含要更新的考试信息的数据传输对象
     * @return 返回一个Result对象，包含更新后的考试信息或错误消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ExamVO> updateExam(UpdateExamDTO updateExamDTO) {
        try {
            // 检查考试是否存在
            Exam exam = examMapper.selectById(updateExamDTO.getExamId());
            if (exam == null) {
                return Result.error("考试不存在");
            }

            // 检查权限
            Integer currentTeacherId = getCurrentId();
            if (!exam.getTeacherId().equals(currentTeacherId)) {
                return Result.error("只能编辑自己的考试");
            }

            // 检查考试状态，只能编辑未开始的考试
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            if (now.after(exam.getStartTime())) {
                return Result.error("考试已开始，无法编辑");
            }

            // 检查是否有学生已提交答卷
            LambdaQueryWrapper<ExamPaper> paperWrapper = new LambdaQueryWrapper<>();
            paperWrapper.eq(ExamPaper::getExamId, updateExamDTO.getExamId());
            long submittedCount = examPaperMapper.selectCount(paperWrapper);
            if (submittedCount > 0) {
                return Result.error("已有学生提交答卷，无法编辑");
            }

            // 更新考试基本信息
            exam.setExamName(updateExamDTO.getExamName());
            if (updateExamDTO.getStartTime() != null) {
                exam.setStartTime(new Timestamp(updateExamDTO.getStartTime().getTime()));
            }
            if (updateExamDTO.getEndTime() != null) {
                exam.setEndTime(new Timestamp(updateExamDTO.getEndTime().getTime()));
            }
            exam.setExamCount(updateExamDTO.getQuestions().size());

            examMapper.updateById(exam);

            // 删除原有题目
            examQuestionMapper.deleteByExamId(updateExamDTO.getExamId());

            // 添加新题目
            for (UpdateExamDTO.ExamQuestionDTO questionDTO : updateExamDTO.getQuestions()) {
                ExamQuestion question = new ExamQuestion();
                question.setExamId(exam.getExamId());
                question.setQuestionType(questionDTO.getQuestionType());
                question.setQuestionContent(questionDTO.getQuestionContent());
                question.setOptions(questionDTO.getOptions());
                question.setCorrectAnswer(questionDTO.getCorrectAnswer());
                question.setScore(questionDTO.getScore());
                question.setDifficulty(questionDTO.getDifficulty());
                question.setExplanation(questionDTO.getExplanation());
                question.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));

                examQuestionMapper.insert(question);
            }

            // 构建返回VO
            Course course = courseMapper.selectById(exam.getCourseId());
            ExamVO examVO = new ExamVO();
            examVO.setExamId(exam.getExamId());
            examVO.setCourseId(exam.getCourseId());
            examVO.setCourseName(course != null ? course.getCourseName() : "未知课程");
            examVO.setTeacherId(exam.getTeacherId());
            examVO.setExamName(exam.getExamName());
            examVO.setExamCount(exam.getExamCount());
            examVO.setStartTime(exam.getStartTime());
            examVO.setEndTime(exam.getEndTime());
            examVO.setCreateTime(exam.getCreateTime());
            examVO.setStatus("UPCOMING");
            examVO.setHasSubmitted(false);

            return Result.success(examVO);

        } catch (Exception e) {
            log.error("编辑考试失败", e);
            return Result.error("编辑考试失败");
        }
    }
    /**
     * 删除考试
     *
     * 此方法用于删除一个考试，包括检查考试是否存在、检查操作权限、考试状态，
     * 以及是否有学生已提交答卷在满足所有条件后，将删除与考试相关的所有数据
     *
     * @param examId 考试ID
     * @return 删除结果，包括成功和错误信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteExam(Integer examId) {
        try {
            // 检查考试是否存在
            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                return Result.error("考试不存在");
            }
            
            // 检查权限
            Integer currentTeacherId = getCurrentId();
            if (!exam.getTeacherId().equals(currentTeacherId)) {
                return Result.error("只能删除自己的考试");
            }
            
            // 检查考试状态，只能删除未开始的考试
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            if (now.after(exam.getStartTime())) {
                return Result.error("考试已开始，无法删除");
            }
            
            // 检查是否有学生已提交答卷
            LambdaQueryWrapper<ExamPaper> paperWrapper = new LambdaQueryWrapper<>();
            paperWrapper.eq(ExamPaper::getExamId, examId);
            long submittedCount = examPaperMapper.selectCount(paperWrapper);
            if (submittedCount > 0) {
                return Result.error("已有学生提交答卷，无法删除");
            }
            
            // 删除相关数据
            // 1. 删除题目
            examQuestionMapper.deleteByExamId(examId);
            
            // 2. 删除相关通知
            notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getType, "EXAM")
                .like(Notification::getContent, exam.getExamName()));
            
            // 3. 删除考试
            examMapper.deleteById(examId);
            
            return Result.success("考试删除成功");
            
        } catch (Exception e) {
            log.error("删除考试失败", e);
            return Result.error("删除考试失败");
        }
    }
}

