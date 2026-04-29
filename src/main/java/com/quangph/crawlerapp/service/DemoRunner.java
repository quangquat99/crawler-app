package com.quangph.crawlerapp.service;

import com.quangph.crawlerapp.config.CrawlerProperties;
import com.quangph.crawlerapp.dto.request.CrawlRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runner demo giúp khởi tạo một request crawl mẫu lúc startup nếu được bật.
 */
@Component
public class DemoRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final CrawlerProperties crawlerProperties;
    private final CrawlOrchestratorService crawlOrchestratorService;

    public DemoRunner(CrawlerProperties crawlerProperties, CrawlOrchestratorService crawlOrchestratorService) {
        this.crawlerProperties = crawlerProperties;
        this.crawlOrchestratorService = crawlOrchestratorService;
    }

    /**
     * Chạy demo crawl sau khi Spring Boot đã khởi động xong.
     *
     * @param args tham số startup của application
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!crawlerProperties.getDemo().isEnabled()) {
            return;
        }

//        String demoUrl = crawlerProperties.getDemo().getUrl();
        String demoUrl = "https://www.jctrans.com/en/company/";
        log.info("Chay demo crawl voi URL: {}", demoUrl);
//        crawlOrchestratorService.crawl(new CrawlRequest(demoUrl, 1, 10, 1));
    }
}
