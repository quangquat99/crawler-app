package com.quangph.crawlerapp.service.strategy;

public interface CrawlerStrategy {

    String getName();

    boolean supports(String url);

    CrawlExecutionResult crawl(String url);
}
