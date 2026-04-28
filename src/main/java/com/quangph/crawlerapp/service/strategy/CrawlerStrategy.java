package com.quangph.crawlerapp.service.strategy;

/**
 * Contract chung cho moi chien luoc crawl.
 */
public interface CrawlerStrategy {

    /**
     * Tra ve ten chien luoc de log va response.
     *
     * @return ten strategy
     */
    String getName();

    /**
     * Kiem tra strategy co phu hop de xu ly URL hay khong.
     *
     * @param url URL can crawl
     * @return true neu strategy ho tro
     */
    boolean supports(String url);

    /**
     * Thuc hien crawl URL va tra ve ket qua.
     *
     * @param url URL can crawl
     * @return ket qua crawl cua strategy
     */
    CrawlExecutionResult crawl(String url);
}
