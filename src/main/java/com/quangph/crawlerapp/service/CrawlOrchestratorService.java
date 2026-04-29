package com.quangph.crawlerapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.response.CrawlResponse;
import com.quangph.crawlerapp.dto.response.CrawledCompanyExcelRow;
import com.quangph.crawlerapp.service.strategy.CrawlExecutionResult;
import com.quangph.crawlerapp.service.strategy.CrawlerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service điều phối thứ tự các chiến lược crawl theo rule:
 * API -> HTML -> PLAYWRIGHT.
 */
@Service
public class CrawlOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(CrawlOrchestratorService.class);

    private final List<CrawlerStrategy> crawlerStrategies;
    private final ExcelExportService excelExportService;

    public CrawlOrchestratorService(List<CrawlerStrategy> crawlerStrategies, ExcelExportService excelExportService) {
        this.crawlerStrategies = crawlerStrategies;
        this.excelExportService = excelExportService;
    }

    /**
     * Chạy lần lượt các strategy cho tới khi lấy được data.
     *
     * @return kết quả crawl cuối cùng
     */
    public CrawlResponse crawl(CrawlRequest request) {
        for (CrawlerStrategy crawlerStrategy : crawlerStrategies) {
            if (!crawlerStrategy.supports(request.pageUrl())) {
                continue;
            }

            log.info("Bat dau crawl voi strategy={}", crawlerStrategy.getName());
            CrawlExecutionResult result = crawlerStrategy.crawl(request);
            if (!result.items().isEmpty()) {
                logResult(result);
                return new CrawlResponse(
                        request.pageUrl(),
                        crawlerStrategy.getName(),
                        result.message(),
                        result.items().size(),
                        Instant.now(),
                        result.items()
                );
            }

            log.info("Strategy {} khong lay duoc data, ly do: {}", crawlerStrategy.getName(), result.message());
        }

        return new CrawlResponse(
                request.pageUrl(),
                "NONE",
                "Khong crawl duoc du lieu tu URL nay",
                0,
                Instant.now(),
                List.of()
        );
    }

    public byte[] crawlAndExportExcel(CrawlRequest request) {
        CrawlResponse crawlResponse = crawl(request);
        List<CrawledCompanyExcelRow> rows = crawlResponse.items().stream()
                .map(this::mapToExcelRow)
                .toList();

        return excelExportService.exportCompanyRows(rows);
    }

    private CrawledCompanyExcelRow mapToExcelRow(JsonNode item) {
        JsonNode list = item.path("list");
        JsonNode detail = item.path("detail");

        JsonNode mainUser = null;
        for (JsonNode user : detail.path("userInfoVoList")) {
            if ("1".equals(user.path("isMain").asText())) {
                mainUser = user;
            }
        }

        return new CrawledCompanyExcelRow(
                firstNonBlank(detail.path("nameEn").asText(null), list.path("compName").asText(null)),
                firstNonBlank(detail.path("status").asText(null), list.path("status").asText(null)),
                firstNonBlank(detail.path("countryNameEn").asText(null), list.path("countryName").asText(null)),
                detail.path("registeredAddressEn").asText(""),
                mainUser.path("email").asText(""),
                mainUser.path("wechat").asText(""),
                mainUser.path("whatsapp").asText(""),
                mainUser.path("skype").asText(""),
                mainUser.path("mobile").asText(""),
                detail.path("companySize").asText(""),
                detail.path("note").asText("")
        );
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second == null ? "" : second;
    }


    /**
     * Ghi log từng item crawl được để phục vụ debug tạm thời.
     *
     * @param result kết quả crawl chứa danh sách item
     */
    private void logResult(CrawlExecutionResult result) {
        result.items().forEach(item -> log.info("Raw crawl item: {}", item.toPrettyString()));
    }
}
