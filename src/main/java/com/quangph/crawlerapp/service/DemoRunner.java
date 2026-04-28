package com.quangph.crawlerapp.service;

import com.quangph.crawlerapp.config.CrawlerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runner demo giup kick off mot request crawl mau luc startup neu duoc bat.
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
     * Chay demo crawl sau khi Spring Boot da khoi dong xong.
     *
     * @param args tham so startup cua application
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!crawlerProperties.getDemo().isEnabled()) {
            return;
        }

        String demoUrl = crawlerProperties.getDemo().getUrl();
        log.info("Chay demo crawl voi URL: {}", demoUrl);
        crawlOrchestratorService.crawl(demoUrl);
    }
}
