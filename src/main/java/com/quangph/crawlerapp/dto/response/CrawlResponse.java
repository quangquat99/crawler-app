package com.quangph.crawlerapp.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

/**
 * DTO đầu ra cho kết quả crawl URL.
 */
public record CrawlResponse(
        String requestedUrl,
        String strategyUsed,
        String message,
        int totalItems,
        Instant crawledAt,
        List<CrawledCompanyRow> items
) {
}
