package com.quangph.crawlerapp.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * DTO nội bộ đại diện cho kết quả của một strategy crawl.
 */
public record CrawlExecutionResult(
        List<JsonNode> items,
        String message
) {
    /**
     * Tạo kết quả rỗng khi strategy không lấy được data.
     *
     * @param message thông điệp mô tả lý do
     * @return kết quả rỗng
     */
    public static CrawlExecutionResult empty(String message) {
        return new CrawlExecutionResult(List.of(), message);
    }
}
