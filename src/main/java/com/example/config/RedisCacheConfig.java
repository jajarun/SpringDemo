package com.example.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置
 * 将Spring Cache存储到Redis中
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 配置Redis作为缓存管理器
     * 所有@Cacheable注解的缓存都会存储到Redis
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager() {
        // 配置JSON序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.registerModule(new JavaTimeModule()); // 支持LocalDateTime等时间类型
        
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues() // 不缓存null值
                .prefixCacheNameWith("cache::"); // 缓存key前缀

        // 为不同的缓存名称配置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户邮箱缓存 - 长期缓存（您repository中使用的）
        cacheConfigurations.put("findUsersByEmail", defaultConfig
                .entryTtl(Duration.ofHours(2))
                .prefixCacheNameWith("user::email::"));
        
        // 用户列表缓存 - 短期缓存
        cacheConfigurations.put("userList", defaultConfig
                .entryTtl(Duration.ofMinutes(15))
                .prefixCacheNameWith("user::list::"));
        
        // 用户详情缓存 - 中期缓存
        cacheConfigurations.put("userDetails", defaultConfig
                .entryTtl(Duration.ofMinutes(45))
                .prefixCacheNameWith("user::detail::"));
        
        // 搜索结果缓存 - 短期缓存
        cacheConfigurations.put("userSearch", defaultConfig
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("search::"));
        
        // 分页缓存 - 短期缓存
        cacheConfigurations.put("userPage", defaultConfig
                .entryTtl(Duration.ofMinutes(8))
                .prefixCacheNameWith("page::"));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 支持事务
                .build();
    }
}
