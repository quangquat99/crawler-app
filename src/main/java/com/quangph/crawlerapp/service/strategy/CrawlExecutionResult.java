package com.quangph.crawlerapp.service.strategy;

import com.quangph.crawlerapp.dto.response.CompanyData;

import java.util.List;

/**
 * DTO noi bo dai dien cho ket qua cua mot strategy crawl.
 */
public record CrawlExecutionResult(
        List<CompanyData> items,
        String message
) {
    /**
     * Tao ket qua rong khi strategy khong lay duoc data.
     *
     * @param message thong diep mo ta ly do
     * @return ket qua rong
     */
    public static CrawlExecutionResult empty(String message) {
        return new CrawlExecutionResult(List.of(), message);
    }
}
