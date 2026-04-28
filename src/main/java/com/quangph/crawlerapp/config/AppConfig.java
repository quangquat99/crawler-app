package com.quangph.crawlerapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
public class AppConfig {

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

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
