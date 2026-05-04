package com.quangph.crawlerapp.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * DTO nội bộ đại diện cho kết quả của một strategy crawl.
 */
public record CrawlExecutionResult(
        boolean success,
        List<JsonNode> items,
        String message,
        long totalItems,
        int totalPages
) {
    public static CrawlExecutionResult success(
            List<JsonNode> items,
            String message,
            long totalItems,
            int totalPages
    ) {
        return new CrawlExecutionResult(true, items, message, totalItems, totalPages);
    }

    public static CrawlExecutionResult failure(String message) {
        return new CrawlExecutionResult(false, List.of(), message, 0, 0);
    }
}
