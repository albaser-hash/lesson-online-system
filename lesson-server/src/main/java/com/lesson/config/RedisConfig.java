package com.lesson.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 设置value的序列化方式
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置hash key的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置hash value的序列化方式
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis缓存管理器（主要缓存）
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.host")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2)) // 设置缓存过期时间为2小时
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 本地缓存管理器（降级缓存）
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.host", havingValue = "false", matchIfMissing = true)
    public CacheManager localCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();
        
        // 添加本地缓存
        caches.add(new ConcurrentMapCache("navMenu"));
        caches.add(new ConcurrentMapCache("courseCategory"));
        
        cacheManager.setCaches(caches);
        return cacheManager;
    }
} 