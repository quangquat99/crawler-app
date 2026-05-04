package com.quangph.crawlerapp.service.strategy;

import com.quangph.crawlerapp.dto.request.CrawlRequest;

/**
 * Contract chung cho mọi chiến lược crawl.
 */
public interface CrawlerStrategy {

    /**
     * Trả về tên chiến lược để log và response.
     *
     * @return tên strategy
     */
    String getName();

    /**
     * Kiểm tra strategy có phù hợp để xử lý URL hay không.
     *
     * @param url URL cần crawl
     * @return true nếu strategy hỗ trợ
     */
    boolean supports(String url);

    CrawlExecutionResult crawl(CrawlRequest request);
}
