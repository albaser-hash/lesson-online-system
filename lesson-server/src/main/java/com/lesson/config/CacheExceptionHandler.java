package com.lesson.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheExceptionHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("缓存获取失败，将降级到数据库查询 - Cache: {}, Key: {}, Error: {}", 
                cache.getName(), key, exception.getMessage());
        // 不抛出异常，让方法继续执行数据库查询
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("缓存写入失败 - Cache: {}, Key: {}, Error: {}", 
                cache.getName(), key, exception.getMessage());
        // 不抛出异常，缓存写入失败不影响业务
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("缓存清除失败 - Cache: {}, Key: {}, Error: {}", 
                cache.getName(), key, exception.getMessage());
        // 不抛出异常，缓存清除失败不影响业务
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("缓存清空失败 - Cache: {}, Error: {}", 
                cache.getName(), exception.getMessage());
        // 不抛出异常，缓存清空失败不影响业务
    }
} 