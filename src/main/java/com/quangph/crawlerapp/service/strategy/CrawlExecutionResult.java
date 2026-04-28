package com.quangph.crawlerapp.service.strategy;

import com.quangph.crawlerapp.dto.response.CompanyData;

import java.util.List;

public record CrawlExecutionResult(
        List<CompanyData> items,
        String message
) {
    public static CrawlExecutionResult empty(String message) {
        return new CrawlExecutionResult(List.of(), message);
    }
}
