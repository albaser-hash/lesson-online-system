package com.lesson.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "lesson")
public class LessonProperties {
    private Interceptor interceptor;

    public Interceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public static class Interceptor {
        private List<String> excludePaths;

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }
} 