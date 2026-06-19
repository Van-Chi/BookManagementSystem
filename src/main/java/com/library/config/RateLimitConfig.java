package com.library.config;

import io.github.bucket4j.Bucket;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    /**
     * Kho lưu trữ các bucket rate limit dùng chung cho toàn ứng dụng.
     * Được tách thành bean riêng để có thể inject vào test và xoá trạng thái giữa các test case.
     */
    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimitBucketStore() {
        return new ConcurrentHashMap<>();
    }
}
