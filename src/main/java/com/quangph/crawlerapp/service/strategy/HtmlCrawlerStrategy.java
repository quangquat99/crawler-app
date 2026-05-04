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

import java.io.InterruptedIOException;
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

    @Override
    public CrawlExecutionResult crawl(CrawlRequest request) {
        String url = request.pageUrl();
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

            return CrawlExecutionResult.success(
                    items,
                    items.isEmpty()
                            ? "HTML đã tải thành công nhưng không có dữ liệu, có thể trang dùng JavaScript để render."
                            : "Đã lấy dữ liệu từ HTML trả về từ máy chủ.",
                    items.size(),
                    items.isEmpty() ? 0 : 1
            );
        } catch (InterruptedIOException exception) {
            return CrawlExecutionResult.failure("Crawl bị timeout, vui lòng thử lại sau.");
        } catch (IOException exception) {
            return CrawlExecutionResult.failure("Crawl HTML thất bại: " + exception.getMessage());
        } catch (Exception exception) {
            return CrawlExecutionResult.failure("Crawl HTML thất bại: " + exception.getMessage());
        }
    }
}
