package com.quangph.crawlerapp.service.strategy;

import com.quangph.crawlerapp.dto.response.CompanyData;
import com.quangph.crawlerapp.service.site.JcTransCompanyParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Order(2)
public class HtmlCrawlerStrategy implements CrawlerStrategy {

    private final JcTransCompanyParser jcTransCompanyParser;

    public HtmlCrawlerStrategy(JcTransCompanyParser jcTransCompanyParser) {
        this.jcTransCompanyParser = jcTransCompanyParser;
    }

    @Override
    public String getName() {
        return "HTML";
    }

    @Override
    public boolean supports(String url) {
        return true;
    }

    @Override
    public CrawlExecutionResult crawl(String url) {
        Connection connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15_000)
                .followRedirects(true);

        try {
            Document document = connection.get();
            List<CompanyData> items = jcTransCompanyParser.parse(document.html(), url);
            if (items.isEmpty()) {
                return CrawlExecutionResult.empty("HTML thuan khong co item, co the trang dung JS render");
            }

            return new CrawlExecutionResult(items, "Lay du lieu tu HTML server response");
        } catch (IOException exception) {
            return CrawlExecutionResult.empty("Loi HTML crawl: " + exception.getMessage());
        }
    }
}
