package com.lesson.controller.comm;

import com.lesson.config.StorageProperties;
import com.lesson.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.lesson.mapper.CourseChapterMapper;
import com.lesson.service.StorageService;
import com.lesson.mapper.ChapterMapper;
import com.lesson.entity.Chapter;
import com.lesson.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private CourseChapterMapper courseChapterMapper;



    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload-image")
    @ApiOperation("上传图片文件")
    public Result<Map<String, Object>> uploadImage(@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            
            log.info("=== 开始处理图片上传请求 ===");
            log.info("文件对象: {}", file);
            log.info("文件名: {}", file != null ? file.getOriginalFilename() : "null");
            log.info("文件大小: {} bytes", file != null ? file.getSize() : 0);
            log.info("Content-Type: {}", file != null ? file.getContentType() : "null");
            
            // 记录请求头信息
            log.info("请求头信息:");
            log.info("  Content-Type: {}", request.getHeader("Content-Type"));
            log.info("  Content-Length: {}", request.getHeader("Content-Length"));
            log.info("  User-Agent: {}", request.getHeader("User-Agent"));
            
            // 记录所有请求参数
            log.info("所有请求参数:");
            request.getParameterMap().forEach((key, values) -> {
                log.info("  {}: {}", key, String.join(", ", values));
            });
            
            // 记录multipart请求信息
            log.info("请求Content-Type: {}", request.getContentType());
            log.info("请求Content-Length: {}", request.getContentLength());
            
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                log.error("上传的文件为空，file对象: {}", file);
                return Result.error("请选择要上传的图片文件");
            }
            
            // 检查文件类型
            if (!isImageFile(file.getOriginalFilename())) {
                log.error("不支持的文件类型: {}", file.getOriginalFilename());
                return Result.error("只支持上传图片文件（JPG、PNG、GIF等）");
            }

            // 检查文件大小
            if (file.getSize() > 2 * 1024 * 1024) { // 2MB限制
                log.error("文件大小超限: {} bytes", file.getSize());
                return Result.error("图片文件大小不能超过2MB");
            }

            // 生成唯一文件名
            String fileName = file.getOriginalFilename();
            String safeFileName = fileName != null ? fileName.replaceAll("\\s+", "_") : "unknown.png";
            String objectName = "images/" + System.currentTimeMillis() + "_" + safeFileName;
            
            log.info("开始上传文件到存储服务，objectName: {}", objectName);
            
            // 直接上传到存储服务
            storageService.uploadFile(file, objectName);

            Map<String, Object> result = new HashMap<>();
            result.put("objectName", objectName);
            result.put("fileName", safeFileName);
            result.put("fileSize", file.getSize());
            result.put("imageUrl", storageService.getDownloadUrl(objectName));
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("图片上传完成: {}, 大小: {}KB, 耗时: {}ms", 
                    fileName, file.getSize() / 1024, duration);
            log.info("=== 图片上传处理完成 ===");
                    
            return Result.success(result);
        } catch (Exception e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload-video")
    @ApiOperation("上传视频文件")
    public Result<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "duration", required = false) String duration) {
        try {
            long startTime = System.currentTimeMillis();
            
            // 检查文件类型
            if (file == null || !isVideoFile(file.getOriginalFilename())) {
                return Result.error("不支持的文件类型，请上传视频文件");
            }

            // 检查文件大小
            if (file.getSize() > 2L * 1024 * 1024 * 1024) { // 2GB限制
                return Result.error("视频文件大小不能超过2GB");
            }

            // 生成唯一文件名
            String fileName = file.getOriginalFilename();
            String safeFileName = fileName != null ? fileName.replaceAll("\\s+", "_") : "unknown.mp4";
            String objectName = "videos/" + System.currentTimeMillis() + "_" + safeFileName;
            
            // 直接上传到存储服务
            storageService.uploadFile(file, objectName);

            Map<String, Object> result = new HashMap<>();
            result.put("objectName", objectName);
            result.put("fileName", safeFileName);
            result.put("fileSize", file.getSize());
            result.put("fileSizeMB", (file.getSize() / (1024.0 * 1024.0)));
            if (duration != null && !duration.isEmpty()) {
                result.put("duration", duration);
            }
            result.put("videoUrl", storageService.getDownloadUrl(objectName));
            
            long uploadDuration = System.currentTimeMillis() - startTime;
            log.info("视频上传完成: {}, 大小: {}MB, 耗时: {}ms", 
                    fileName, file.getSize() / (1024 * 1024), uploadDuration);
                    
            return Result.success(result);
        } catch (Exception e) {
            log.error("视频上传失败: {}", e.getMessage());
            return Result.error("视频上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload-doc")
    @ApiOperation("上传并解析文档（PDF/Word/TXT）")
    public Result<?> uploadDoc(@RequestParam("file") MultipartFile file, @RequestParam("chapterId") Integer chapterId) {
        String objectName = null;
        String minioUrl = null;
        String fileName = null;
        try {
            // 1. 上传到 MinIO
            fileName = file.getOriginalFilename();
            String safeFileName = fileName != null ? fileName.replaceAll("\\s+", "_") : "unknown.doc";
            String filename = safeFileName.toLowerCase();
            objectName = "documents/" + System.currentTimeMillis() + "_" + filename;
            byte[] fileBytes = file.getBytes(); // 一次性读取，后续多次用
            storageService.uploadObject(objectName, new ByteArrayInputStream(fileBytes), file.getContentType());
            minioUrl = storageService.getDownloadUrl(objectName);

            // 2. 立即保存 objectName 到数据库（保证后续能查到文件）
            chapterMapper.updateVideoUrl(chapterId, objectName, "00:10:00");
            chapterMapper.updateVideoSize(chapterId, file.getSize());

            // 3. 解析文档内容（单独 try-catch，失败不影响主流程）
            try {
                List<Map<String, Object>> pages = new ArrayList<>();
                int totalPages = 0;
                String filenameLower = filename;
                if (filenameLower.endsWith(".pdf")) {
                    pages = parsePdf(fileBytes);
                } else if (filenameLower.endsWith(".docx")) {
                    pages = parseDocx(fileBytes);
                } else if (filenameLower.endsWith(".doc")) {
                    pages = parseDoc(fileBytes);
                } else if (filenameLower.endsWith(".txt") || filenameLower.endsWith(".md")) {
                    pages = parseTxtOrMd(fileBytes);
                } else if (filenameLower.endsWith(".xls") || filenameLower.endsWith(".xlsx")) {
                    pages = parseExcel(fileBytes, filenameLower);
                }
                totalPages = pages.size();
                // 4. 保存分页内容
                try {
                    Map<String, Object> docJson = new HashMap<>();
                    docJson.put("pages", pages);
                    docJson.put("totalPages", totalPages);
                    String jsonStr = new ObjectMapper().writeValueAsString(docJson);
                    courseChapterMapper.updateDocJson(chapterId, jsonStr);
                } catch (Exception e) {
                    // 分页内容保存失败也不影响主流程
                    log.warn("分页内容保存失败: {}", e.getMessage());
                }
            } catch (Exception e) {
                // 解析失败只记录日志，不影响主流程
                log.warn("文档解析失败: {}", e.getMessage());
            }

            return Result.success();
        } catch (Exception e) {
            return Result.error("文档上传失败: " + e.getMessage());
        }
    }

    // PDF解析
    private List<Map<String, Object>> parsePdf(byte[] fileBytes) {
        List<Map<String, Object>> pages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileBytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(document);
                Map<String, Object> page = new HashMap<>();
                page.put("page", i);
                page.put("content", text);
                pages.add(page);
            }
        } catch (Exception e) {
            log.warn("PDF解析失败: {}", e.getMessage());
        }
        return pages;
    }

    // DOCX解析
    private List<Map<String, Object>> parseDocx(byte[] fileBytes) {
        List<Map<String, Object>> pages = new ArrayList<>();
        try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
            List<XWPFParagraph> paragraphs = docx.getParagraphs();
            int paraPerPage = 10;
            int pageNum = 1;
            StringBuilder pageContent = new StringBuilder();
            for (int i = 0; i < paragraphs.size(); i++) {
                try {
                    String text = paragraphs.get(i).getText();
                    if (text != null && !text.trim().isEmpty()) {
                        pageContent.append(text).append("\n");
                    }
                } catch (Exception e) {
                    log.warn("段落解析失败: {}", e.getMessage());
                }
                if ((i + 1) % paraPerPage == 0 || i == paragraphs.size() - 1) {
                    Map<String, Object> page = new HashMap<>();
                    page.put("page", pageNum++);
                    page.put("content", pageContent.toString());
                    pages.add(page);
                    pageContent = new StringBuilder();
                }
            }
            // 解析表格内容
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : docx.getTables()) {
                try {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                        for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                            String cellText = cell.getText();
                            if (cellText != null && !cellText.trim().isEmpty()) {
                                pageContent.append(cellText).append("\t");
                            }
                        }
                        pageContent.append("\n");
                    }
                } catch (Exception e) {
                    log.warn("表格解析失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("DOCX解析失败: {}", e.getMessage());
        }
        return pages;
    }

    // DOC解析
    private List<Map<String, Object>> parseDoc(byte[] fileBytes) {
        List<Map<String, Object>> pages = new ArrayList<>();
        try (HWPFDocument doc = new HWPFDocument(new ByteArrayInputStream(fileBytes));
             WordExtractor extractor = new WordExtractor(doc)) {
            String[] paragraphs = extractor.getParagraphText();
            int paraPerPage = 10;
            int pageNum = 1;
            StringBuilder pageContent = new StringBuilder();
            for (int i = 0; i < paragraphs.length; i++) {
                try {
                    String text = paragraphs[i];
                    if (text != null && !text.trim().isEmpty()) {
                        pageContent.append(text).append("\n");
                    }
                } catch (Exception e) {
                    log.warn("段落解析失败: {}", e.getMessage());
                }
                if ((i + 1) % paraPerPage == 0 || i == paragraphs.length - 1) {
                    Map<String, Object> page = new HashMap<>();
                    page.put("page", pageNum++);
                    page.put("content", pageContent.toString());
                    pages.add(page);
                    pageContent = new StringBuilder();
                }
            }
        } catch (Exception e) {
            log.warn("DOC解析失败: {}", e.getMessage());
        }
        return pages;
    }

    // TXT/MD解析
    private List<Map<String, Object>> parseTxtOrMd(byte[] fileBytes) {
        List<Map<String, Object>> pages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8))) {
            String line;
            int lineCount = 0;
            int pageNum = 1;
            StringBuilder pageContent = new StringBuilder();
            int linesPerPage = 50;
            while ((line = reader.readLine()) != null) {
                pageContent.append(line).append("\n");
                lineCount++;
                if (lineCount % linesPerPage == 0) {
                    Map<String, Object> page = new HashMap<>();
                    page.put("page", pageNum++);
                    page.put("content", pageContent.toString());
                    pages.add(page);
                    pageContent = new StringBuilder();
                }
            }
            if (pageContent.length() > 0) {
                Map<String, Object> page = new HashMap<>();
                page.put("page", pageNum++);
                page.put("content", pageContent.toString());
                pages.add(page);
            }
        } catch (Exception e) {
            log.warn("TXT/MD解析失败: {}", e.getMessage());
        }
        return pages;
    }

    // Excel解析
    private List<Map<String, Object>> parseExcel(byte[] fileBytes, String filenameLower) {
        List<Map<String, Object>> pages = new ArrayList<>();
        try {
            Workbook workbook = filenameLower.endsWith(".xlsx") ?
                    new XSSFWorkbook(new ByteArrayInputStream(fileBytes)) :
                    new HSSFWorkbook(new ByteArrayInputStream(fileBytes));
            int pageNum = 1;
            int rowsPerPage = 20;
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                int rowCount = 0;
                StringBuilder pageContent = new StringBuilder();
                for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            pageContent.append(cell.toString()).append("\t");
                        }
                    }
                    pageContent.append("\n");
                    rowCount++;
                    if (rowCount % rowsPerPage == 0) {
                        Map<String, Object> page = new HashMap<>();
                        page.put("page", pageNum++);
                        page.put("content", "[Sheet " + (s+1) + "]\n" + pageContent.toString());
                        pages.add(page);
                        pageContent = new StringBuilder();
                    }
                }
                if (pageContent.length() > 0) {
                    Map<String, Object> page = new HashMap<>();
                    page.put("page", pageNum++);
                    page.put("content", "[Sheet " + (s+1) + "]\n" + pageContent.toString());
                    pages.add(page);
                }
            }
            workbook.close();
        } catch (Exception e) {
            log.warn("Excel解析失败: {}", e.getMessage());
        }
        return pages;
    }

    @GetMapping("/chapter/getDocPages")
    @ApiOperation("获取章节文档分页内容")
    public Result<?> getDocPages(@RequestParam Integer chapterId, @RequestParam Integer userId) {
        String downloadUrl = storageProperties.getMinio().getEndpoint() +"/"+storageProperties.getMinio().getBucketName()+"/";
        // 1. 查章节
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) return Result.error("章节不存在");
        // 2. 权限校验，和视频接口一致
        if (!userService.canPlayChapter(userId, chapter)) {
            return Result.error("无权限查看文档");
        }
        // 3. 返回内容（原有逻辑）
        String docJson = chapter.getDocJson();
        if (docJson == null || docJson.trim().isEmpty()) {
            // 如果没有解析的文档内容，返回原始文件信息供下载
            Map<String, Object> result = new HashMap<>();
            result.put("hasContent", false);
            result.put("message", "文档尚未解析，请下载原始文件查看");
            // 获取原始文件URL（如果有的话）
            String videoUrl = chapter.getVideoUrl();
            if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                result.put("downloadUrl", storageService.getDownloadUrl(videoUrl));
                result.put("fileName", videoUrl.substring(videoUrl.lastIndexOf("/") + 1));
            }
            return Result.success(result);
        }
        try {
            Map<String, Object> parsedContent = new ObjectMapper().readValue(docJson, Map.class);
            parsedContent.put("hasContent", true);
            return Result.success(parsedContent);
        } catch (Exception e) {
            // 解析失败，返回原始文件信息
            Map<String, Object> result = new HashMap<>();
            result.put("hasContent", false);
            result.put("message", "文档解析失败，请下载原始文件查看");
            result.put("error", e.getMessage());
            // 获取原始文件URL（如果有的话）
            String videoUrl = courseChapterMapper.getVideoUrl(chapterId);
            if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                result.put("downloadUrl", storageService.getDownloadUrl(videoUrl));
                result.put("fileName", videoUrl.substring(videoUrl.lastIndexOf("/") + 1));
            }
            return Result.success(result);
        }
    }

    @GetMapping("/api/doc/download")
    @ApiOperation("获取文档临时下载地址（50分钟有效）")
    public Result<String> getDocDownloadUrl(@RequestParam Integer chapterId, @RequestParam Integer userId) {
        // 1. 查章节
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) return Result.error("章节不存在");
        String fileUrl = chapter.getVideoUrl(); // 这里fileUrl存的就是objectName
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return Result.error("文档不存在");
        }
        // 2. 权限校验：只有免费，课程本人、已购学生或管理员可下载
        if (!userService.canPlayChapter(userId, chapter)) {
            return Result.error("无权限下载文档");
        }
        // 3. 生成签名URL（5分钟有效）
        try {
            String signedUrl = storageService.getPresignedDownloadUrl(fileUrl, 3000);
            String downloadUrl = storageService.getDownloadUrl(fileUrl);
            return Result.success(signedUrl);
        } catch (Exception e) {
            return Result.error("生成文档下载地址失败: " + e.getMessage());
        }
    }



    /**
     * 判断是否为视频文件
     */
    private boolean isVideoFile(String fileName) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".mp4") ||
                lowerFileName.endsWith(".avi") ||
                lowerFileName.endsWith(".mov") ||
                lowerFileName.endsWith(".wmv") ||
                lowerFileName.endsWith(".flv") ||
                lowerFileName.endsWith(".mkv") ||
                lowerFileName.endsWith(".webm");
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".jpg") ||
                lowerFileName.endsWith(".jpeg") ||
                lowerFileName.endsWith(".png") ||
                lowerFileName.endsWith(".gif");
    }
} 