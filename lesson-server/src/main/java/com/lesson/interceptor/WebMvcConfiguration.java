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
