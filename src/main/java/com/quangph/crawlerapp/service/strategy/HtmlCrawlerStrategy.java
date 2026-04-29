package com.quangph.crawlerapp.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.service.site.JcTransCompanyParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Strategy crawl HTML thường bằng Jsoup khi không tìm thấy API phù hợp.
 */
@Component
@Order(2)
public class HtmlCrawlerStrategy implements CrawlerStrategy {

    private final ObjectMapper objectMapper;
    private final JcTransCompanyParser jcTransCompanyParser;

    public HtmlCrawlerStrategy(ObjectMapper objectMapper, JcTransCompanyParser jcTransCompanyParser) {
        this.objectMapper = objectMapper;
        this.jcTransCompanyParser = jcTransCompanyParser;
    }

    /**
     * Trả về tên strategy.
     *
     * @return tên strategy HTML
     */
    @Override
    public String getName() {
        return "HTML";
    }

    /**
     * Tạm thời cho phép strategy HTML được thử với mọi URL.
     *
     * @param url URL cần crawl
     * @return true
     */
    @Override
    public boolean supports(String url) {
        return true;
    }

    /**
     * Crawl HTML thuần bằng Jsoup và parse thành data.
     *
     * @param url URL cần crawl
     * @return kết quả crawl bằng HTML
     */
    @Override
    public CrawlExecutionResult crawl(String url) {
        Connection connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15_000)
                .followRedirects(true);

        try {
            Document document = connection.get();
            List<JsonNode> items = jcTransCompanyParser.parse(document.html(), url)
                    .stream()
                    .<JsonNode>map(item -> objectMapper.valueToTree(item))
                    .toList();
            if (items.isEmpty()) {
                return CrawlExecutionResult.empty("HTML thuan khong co item, co the trang dung JS render");
            }

            return new CrawlExecutionResult(items, "Lay du lieu tu HTML server response");
        } catch (IOException exception) {
            return CrawlExecutionResult.empty("Loi HTML crawl: " + exception.getMessage());
        }
    }

    @Override
    public CrawlExecutionResult crawl(CrawlRequest url) {
        return null;
    }
}
