package com.quangph.crawlerapp.dto.response;

import java.time.Instant;
import java.util.List;

public record CrawlResponse(
        String requestedUrl,
        String strategyUsed,
        String message,
        int totalItems,
        Instant crawledAt,
        List<CompanyData> items
) {
}
