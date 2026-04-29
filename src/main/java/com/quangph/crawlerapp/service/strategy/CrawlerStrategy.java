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

    /**
     * Thực hiện crawl URL và trả về kết quả.
     *
     * @param url URL cần crawl
     * @return kết quả crawl của strategy
     */
    CrawlExecutionResult crawl(String url);


    CrawlExecutionResult crawl(CrawlRequest url);
}
