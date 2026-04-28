package com.quangph.crawlerapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Class chua cac bean cau hinh dung chung cho he thong crawl.
 */
@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
public class AppConfig {

    /**
     * Khoi tao OkHttpClient dung chung cho cac luong crawl API.
     *
     * @param crawlerProperties cau hinh timeout va HTTP
     * @return client da duoc cau hinh timeout, redirect
     */
    @Bean
    public OkHttpClient okHttpClient(CrawlerProperties crawlerProperties) {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(crawlerProperties.getHttp().getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(crawlerProperties.getHttp().getReadTimeoutMs()))
                .writeTimeout(Duration.ofMillis(crawlerProperties.getHttp().getWriteTimeoutMs()))
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    /**
     * Khoi tao ObjectMapper dung chung cho serialize/deserialize JSON.
     *
     * @return ObjectMapper da tu dong nap cac module can thiet
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
