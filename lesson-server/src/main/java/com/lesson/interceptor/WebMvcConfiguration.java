package com.lesson.interceptor;

import com.lesson.config.LessonProperties;
import com.lesson.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import io.swagger.annotations.Api;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {


    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private LessonProperties lessonProperties;

    @Value("${swagger.enabled:true}")
    private boolean swaggerEnabled;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        List<String> excludeList = lessonProperties.getInterceptor().getExcludePaths();
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludeList);
    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket adminApi() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("在线教育系统-管理端接口文档")
                .version("1.0")
                .description("在线教育系统-管理端接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("管理端")
                .apiInfo(apiInfo)
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.admin"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket userApi() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("在线教育系统-用户端接口文档")
                .version("1.0")
                .description("在线教育系统-用户端接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端")
                .apiInfo(apiInfo)
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.user"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 全部接口文档
     */
    @Bean
    public Docket allApi() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("在线教育系统-全部接口文档")
                .version("1.0")
                .description("在线教育系统-全部接口文档")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("全部接口")
                .apiInfo(apiInfo)
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 课程分类管理分组
     */
    @Bean
    public Docket courseCategoryApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("课程分类管理")
                .apiInfo(new ApiInfoBuilder().title("课程分类管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.comm"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/course/category.*"))
                .build();
    }

    /**
     * 导航菜单管理分组
     */
    @Bean
    public Docket navMenuApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("导航菜单管理")
                .apiInfo(new ApiInfoBuilder().title("导航菜单管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.user"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/api/nav-menu.*"))
                .build();
    }

    /**
     * 订单管理分组
     */
    @Bean
    public Docket orderApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("订单管理")
                .apiInfo(new ApiInfoBuilder().title("订单管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.user"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/order.*"))
                .build();
    }

    /**
     * 购物车管理分组
     */
    @Bean
    public Docket cartApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("购物车管理")
                .apiInfo(new ApiInfoBuilder().title("购物车管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.user"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/cart.*"))
                .build();
    }

    /**
     * 考试题目管理分组
     */
    @Bean
    public Docket examQuestionApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("考试题目管理")
                .apiInfo(new ApiInfoBuilder().title("考试题目管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.student"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/teacher/exam/questions.*"))
                .build();
    }

    /**
     * 问答问题管理分组
     */
    @Bean
    public Docket qaQuestionApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("问答问题管理")
                .apiInfo(new ApiInfoBuilder().title("问答问题管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.comm"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/qa/question.*"))
                .build();
    }

    /**
     * 问答答案管理分组
     */
    @Bean
    public Docket qaAnswerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("问答答案管理")
                .apiInfo(new ApiInfoBuilder().title("问答答案管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.comm"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/qa/answer.*"))
                .build();
    }

    /**
     * 用户管理分组
     */
    @Bean
    public Docket userControllerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户管理")
                .apiInfo(new ApiInfoBuilder().title("用户管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.user"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/user.*"))
                .build();
    }

    /**
     * 我的课程分组
     */
    @Bean
    public Docket myCoursesApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("我的课程")
                .apiInfo(new ApiInfoBuilder().title("我的课程接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.user"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/my-courses.*"))
                .build();
    }

    /**
     * 收藏管理分组
     */
    @Bean
    public Docket favoriteAllApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("收藏管理")
                .apiInfo(new ApiInfoBuilder().title("收藏管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.user"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/favorite.*"))
                .build();
    }

    /**
     * 视频管理分组
     */
    @Bean
    public Docket videoApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("视频管理")
                .apiInfo(new ApiInfoBuilder().title("视频管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.student"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/video.*"))
                .build();
    }

    /**
     * 支付课程分组
     */
    @Bean
    public Docket payCourseApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("支付课程")
                .apiInfo(new ApiInfoBuilder().title("支付课程接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.student"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/pay-course.*"))
                .build();
    }

    /**
     * 学习进度分组
     */
    @Bean
    public Docket learnProcessApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("学习进度")
                .apiInfo(new ApiInfoBuilder().title("学习进度接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.student"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/learn-process.*"))
                .build();
    }

    /**
     * 考试管理分组
     */
    @Bean
    public Docket examApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("考试管理")
                .apiInfo(new ApiInfoBuilder().title("考试管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.student"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/exam.*"))
                .build();
    }

    /**
     * 审核管理分组
     */
    @Bean
    public Docket auditApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("审核管理")
                .apiInfo(new ApiInfoBuilder().title("审核管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.admin"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/audit.*"))
                .build();
    }

    /**
     * 通知管理分组
     */
    @Bean
    public Docket notificationApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("通知管理")
                .apiInfo(new ApiInfoBuilder().title("通知管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.comm"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/notification.*"))
                .build();
    }

    /**
     * 浏览课程分组
     */
    @Bean
    public Docket scanSourseApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("浏览课程")
                .apiInfo(new ApiInfoBuilder().title("浏览课程接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.comm"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/scan.*"))
                .build();
    }

    /**
     * 论坛管理分组
     */
    @Bean
    public Docket forumApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("论坛管理")
                .apiInfo(new ApiInfoBuilder().title("论坛管理接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.comm"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/forum.*"))
                .build();
    }

    /**
     * 通用接口分组
     */
    @Bean
    public Docket commonApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("通用接口")
                .apiInfo(new ApiInfoBuilder().title("通用接口").build())
                .enable(swaggerEnabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lesson.controller.comm"))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.regex(".*/common.*"))
                .build();
    }



    /**
     * 添加资源处理器，用于处理特定路径的请求  {必须配置,不然访问是404}
     * 该方法重写自父类，用于配置资源处理器以服务于Swagger文档和相关静态资源
     *
     * @param registry 资源处理器注册表，用于注册资源处理器
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 为Swagger UI的HTML文档请求配置资源处理器
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");

        // 为Webjars请求配置资源处理器，Webjars用于提供静态资源
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        //指定对象转换器，对象转换器可以Java对象序列化为JSON数据
        jackson2HttpMessageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将消息转换器对象添加到框架容器converters中
        converters.add(0,jackson2HttpMessageConverter);//优先使用
    }
}
