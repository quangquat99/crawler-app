package com.quangph.crawlerapp.service.strategy;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.quangph.crawlerapp.config.CrawlerProperties;
import com.quangph.crawlerapp.dto.response.CompanyData;
import com.quangph.crawlerapp.service.site.JcTransCompanyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
@Order(3)
public class PlaywrightCrawlerStrategy implements CrawlerStrategy {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightCrawlerStrategy.class);

    private final CrawlerProperties crawlerProperties;
    private final JcTransCompanyParser jcTransCompanyParser;

    public PlaywrightCrawlerStrategy(CrawlerProperties crawlerProperties, JcTransCompanyParser jcTransCompanyParser) {
        this.crawlerProperties = crawlerProperties;
        this.jcTransCompanyParser = jcTransCompanyParser;
    }

    @Override
    public String getName() {
        return "PLAYWRIGHT";
    }

    @Override
    public boolean supports(String url) {
        return true;
    }

    @Override
    public CrawlExecutionResult crawl(String url) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(crawlerProperties.getPlaywright().isHeadless());

            try (Browser browser = playwright.chromium().launch(launchOptions)) {
                Page page = browser.newPage();
                page.setDefaultTimeout(crawlerProperties.getPlaywright().getTimeoutMs());
                page.navigate(url);
                page.waitForLoadState();
                page.waitForTimeout(3_000);

                String html = page.content();
                List<CompanyData> items = jcTransCompanyParser.parse(html, url);
                if (items.isEmpty()) {
                    dumpRenderedHtml(html);
                    return CrawlExecutionResult.empty("Playwright da render nhung van chua bat duoc selector");
                }

                return new CrawlExecutionResult(items, "Lay du lieu tu trang sau khi JS render");
            }
        } catch (Exception exception) {
            return CrawlExecutionResult.empty("Loi Playwright crawl: " + exception.getMessage());
        }
    }

    private void dumpRenderedHtml(String html) {
        try {
            Path outputPath = Path.of("target", "playwright-rendered.html");
            Files.writeString(outputPath, html);
            log.info("Da dump HTML render tai {}", outputPath.toAbsolutePath());
        } catch (Exception exception) {
            log.warn("Khong the dump HTML render: {}", exception.getMessage());
        }
    }
}
