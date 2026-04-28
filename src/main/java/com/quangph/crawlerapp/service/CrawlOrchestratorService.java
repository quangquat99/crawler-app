package com.quangph.crawlerapp.service;

import com.quangph.crawlerapp.dto.response.CrawlResponse;
import com.quangph.crawlerapp.service.strategy.CrawlExecutionResult;
import com.quangph.crawlerapp.service.strategy.CrawlerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CrawlOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(CrawlOrchestratorService.class);

    private final List<CrawlerStrategy> crawlerStrategies;

    public CrawlOrchestratorService(List<CrawlerStrategy> crawlerStrategies) {
        this.crawlerStrategies = crawlerStrategies;
    }

    public CrawlResponse crawl(String url) {
        for (CrawlerStrategy crawlerStrategy : crawlerStrategies) {
            if (!crawlerStrategy.supports(url)) {
                continue;
            }

            log.info("Bat dau crawl voi strategy={}", crawlerStrategy.getName());
            CrawlExecutionResult result = crawlerStrategy.crawl(url);
            if (!result.items().isEmpty()) {
                logResult(result);
                return new CrawlResponse(
                        url,
                        crawlerStrategy.getName(),
                        result.message(),
                        result.items().size(),
                        Instant.now(),
                        result.items()
                );
            }

            log.info("Strategy {} khong lay duoc data, ly do: {}", crawlerStrategy.getName(), result.message());
        }

        return new CrawlResponse(
                url,
                "NONE",
                "Khong crawl duoc du lieu tu URL nay",
                0,
                Instant.now(),
                List.of()
        );
    }

    private void logResult(CrawlExecutionResult result) {
        result.items().forEach(item -> log.info("Crawl item: {}", item));
    }
}
