package com.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private long authenticatedRequestsPerMinute = 60;
    private long anonymousRequestsPerMinute = 20;
    private long authRequestsPerMinute = 5;
}
