package com.quangph.crawlerapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.response.CrawlResponse;
import com.quangph.crawlerapp.dto.response.CrawledCompanyExcelRow;
import com.quangph.crawlerapp.dto.response.CrawledCompanyRow;
import com.quangph.crawlerapp.service.strategy.CrawlExecutionResult;
import com.quangph.crawlerapp.service.strategy.CrawlerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Service điều phối thứ tự các chiến lược crawl theo rule:
 * API -> HTML -> PLAYWRIGHT.
 */
@Service
public class CrawlOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(CrawlOrchestratorService.class);
    private static final Set<Integer> SUPPORTED_PAGE_SIZES = Set.of(10, 20, 50);

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
        CrawlResponse validationFailure = validateRequest(request);
        if (validationFailure != null) {
            return validationFailure;
        }

        Instant startedAt = Instant.now();
        String requestedUrl = request.pageUrl();
        String lastMessage = "Khong crawl duoc du lieu tu URL nay";
        Set<String> attemptedStrategies = new LinkedHashSet<>();

        for (CrawlerStrategy crawlerStrategy : crawlerStrategies) {
            if (!crawlerStrategy.supports(requestedUrl)) {
                continue;
            }

            attemptedStrategies.add(crawlerStrategy.getName());
            CrawlExecutionResult result = crawlerStrategy.crawl(request);
            if (result.message() != null && !result.message().isBlank()) {
                lastMessage = result.message();
            }

            if (result.success()) {
                List<CrawledCompanyRow> rows = result.items().stream()
                        .map(this::mapToCompanyRow)
                        .toList();

                long durationMs = Instant.now().toEpochMilli() - startedAt.toEpochMilli();
                log.info("crawl_completed strategy={} url={} page={} pageSize={} totalItems={} durationMs={}",
                        crawlerStrategy.getName(),
                        requestedUrl,
                        request.page(),
                        request.pageSize(),
                        result.totalItems(),
                        durationMs);
                logItemsDebug(result);

                return new CrawlResponse(
                        true,
                        requestedUrl,
                        crawlerStrategy.getName(),
                        result.message(),
                        request.page(),
                        request.pageSize(),
                        result.totalItems(),
                        result.totalPages(),
                        Instant.now(),
                        rows
                );
            }

            log.warn("crawl_failed strategy={} url={} page={} pageSize={} message={}",
                    crawlerStrategy.getName(),
                    requestedUrl,
                    request.page(),
                    request.pageSize(),
                    result.message());
        }

        return new CrawlResponse(
                false,
                requestedUrl,
                attemptedStrategies.isEmpty() ? "NONE" : String.join(" -> ", attemptedStrategies),
                lastMessage,
                request.page(),
                request.pageSize(),
                0,
                0,
                Instant.now(),
                List.of()
        );
    }

    public byte[] crawlAndExportExcel(CrawlRequest request) {
        CrawlResponse crawlResponse = crawl(request);
        if (!crawlResponse.success()) {
            throw new IllegalStateException(crawlResponse.message());
        }

        List<CrawledCompanyExcelRow> rows = crawlResponse.items().stream()
                .map(this::mapToExcelRow)
                .toList();

        return excelExportService.exportCompanyRows(rows);
    }

    private CrawledCompanyExcelRow mapToExcelRow(CrawledCompanyRow item) {
        return new CrawledCompanyExcelRow(
                item.companyName(),
                item.status(),
                item.country(),
                item.address(),
                item.email(),
                item.weChat(),
                item.whatsapp(),
                item.skype(),
                item.phone(),
                item.companySize(),
                item.note()
        );
    }

    private CrawledCompanyRow mapToCompanyRow(JsonNode item) {
        JsonNode list = item.path("list");
        JsonNode detail = item.path("detail");

        JsonNode mainUser = null;
        for (JsonNode user : detail.path("userInfoVoList")) {
            if ("1".equals(user.path("isMain").asText())) {
                mainUser = user;
            }
        }

        return new CrawledCompanyRow(
                firstNonBlank(detail.path("nameEn").asText(""), list.path("compName").asText("")),
                firstNonBlank(detail.path("status").asText(""), list.path("status").asText("")),
                firstNonBlank(detail.path("countryNameEn").asText(""), list.path("countryName").asText("")),
                firstNonBlank(detail.path("registeredAddressEn").asText(""), detail.path("registeredAddressCn").asText("")),
                mainUser.path("email").asText(""),
                mainUser.path("wechat").asText(""),
                mainUser.path("whatsapp").asText(""),
                mainUser.path("skype").asText(""),
                mainUser.path("mobile").asText(""),
                detail.path("companySize").asText(""),
                detail.path("note").asText("")


        );
    }

    private CrawlResponse validateRequest(CrawlRequest request) {
        if (!SUPPORTED_PAGE_SIZES.contains(request.pageSize())) {
            return new CrawlResponse(
                    false,
                    request.pageUrl(),
                    "NONE",
                    "pageSize chi duoc phep la 10, 20 hoac 50",
                    request.page(),
                    request.pageSize(),
                    0,
                    0,
                    Instant.now(),
                    List.of()
            );
        }
        return null;
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
    private void logItemsDebug(CrawlExecutionResult result) {
        if (!log.isDebugEnabled()) {
            return;
        }
        result.items().forEach(item -> log.debug("Raw crawl item: {}", item.toPrettyString()));
    }
}
