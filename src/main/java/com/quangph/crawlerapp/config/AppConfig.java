package com.quangph.crawlerapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Lớp chứa các bean cấu hình dùng chung cho hệ thống crawl.
 */
@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
public class AppConfig {

    /**
     * Khởi tạo OkHttpClient dùng chung cho các luồng crawl API.
     *
     * @param crawlerProperties cấu hình timeout và HTTP
     * @return client đã được cấu hình timeout, redirect
     */
    @Bean
    public OkHttpClient okHttpClient(CrawlerProperties crawlerProperties) {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(crawlerProperties.getHttp().getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(crawlerProperties.getHttp().getReadTimeoutMs()))
                .writeTimeout(Duration.ofMillis(crawlerProperties.getHttp().getWriteTimeoutMs()))
                .callTimeout(Duration.ofMillis(crawlerProperties.getHttp().getCallTimeoutMs()))
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    /**
     * Khởi tạo ObjectMapper dùng chung cho serialize/deserialize JSON.
     *
     * @return ObjectMapper đã tự động nạp các module cần thiết
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean(name = "crawlTaskExecutor")
    public Executor crawlTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("crawl-export-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.initialize();
        return executor;
    }
}
