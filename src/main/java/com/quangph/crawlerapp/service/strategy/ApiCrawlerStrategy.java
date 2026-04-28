package com.quangph.crawlerapp.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quangph.crawlerapp.config.CrawlerProperties;
import com.quangph.crawlerapp.dto.response.CompanyData;
import com.quangph.crawlerapp.service.site.JcTransCompanyParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Strategy uu tien tim va goi API public truoc khi fallback sang HTML/Playwright.
 */
@Component
@Order(1)
public class ApiCrawlerStrategy implements CrawlerStrategy {

    private static final Logger log = LoggerFactory.getLogger(ApiCrawlerStrategy.class);

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final CrawlerProperties crawlerProperties;
    private final JcTransCompanyParser jcTransCompanyParser;

    public ApiCrawlerStrategy(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper,
            CrawlerProperties crawlerProperties,
            JcTransCompanyParser jcTransCompanyParser
    ) {
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
        this.crawlerProperties = crawlerProperties;
        this.jcTransCompanyParser = jcTransCompanyParser;
    }

    /**
     * Tra ve ten strategy.
     *
     * @return ten strategy API
     */
    @Override
    public String getName() {
        return "API";
    }

    /**
     * Tam thoi cho phep strategy API duoc thu voi moi URL.
     *
     * @param url URL can crawl
     * @return true
     */
    @Override
    public boolean supports(String url) {
        return true;
    }

    /**
     * Thu tim API mapping va goi API de lay data.
     *
     * @param url URL can crawl
     * @return ket qua crawl bang API
     */
    @Override
    public CrawlExecutionResult crawl(String url) {
        Optional<String> apiUrlOptional = resolveApiUrl(url);
        if (apiUrlOptional.isEmpty()) {
            return CrawlExecutionResult.empty("Khong tim thay API mapping public phu hop");
        }

        String apiUrl = apiUrlOptional.get();
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("User-Agent", "Mozilla/5.0")
                .get()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return CrawlExecutionResult.empty("API tra ve status " + response.code());
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return CrawlExecutionResult.empty("API khong co response body");
            }

            String rawBody = responseBody.string();
            log.info("API body preview: {}", limit(rawBody));

            // Dinh nghia chuan: neu la JSON thi parse bang Jackson.
            if (looksLikeJson(rawBody)) {
                objectMapper.readTree(rawBody);
                if (jcTransCompanyParser.supports(url) || looksLikeJcTransCompanyApi(rawBody)) {
                    List<CompanyData> items = jcTransCompanyParser.parseApiResponse(rawBody);
                    if (!items.isEmpty()) {
                        return new CrawlExecutionResult(items, "Lay du lieu tu JSON API response");
                    }
                }

                return CrawlExecutionResult.empty("API tra JSON nhung khong parse duoc thanh data");
            }

            if (jcTransCompanyParser.supports(url)) {
                List<CompanyData> items = jcTransCompanyParser.parse(rawBody, url);
                if (!items.isEmpty()) {
                    return new CrawlExecutionResult(items, "Lay du lieu bang API/HTML response");
                }
            }

            return CrawlExecutionResult.empty("API response khong parse duoc thanh data");
        } catch (IOException exception) {
            return CrawlExecutionResult.empty("Loi goi API: " + exception.getMessage());
        }
    }

    /**
     * Resolve API URL tu URL dau vao.
     * Hien tai de hardcode cho viec mo rong sau nay, JcTrans chua map san API cong khai on dinh.
     *
     * @param url URL goc
     * @return API URL neu tim thay
     */
    private Optional<String> resolveApiUrl(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }

        if (looksLikeApiUrl(url)) {
            return Optional.of(url);
        }

        String jcTransCompanyApiUrl = crawlerProperties.getSites().getJcTrans().getCompanyApiUrl();
        if (jcTransCompanyParser.supports(url) && jcTransCompanyApiUrl != null && !jcTransCompanyApiUrl.isBlank()) {
            return Optional.of(jcTransCompanyApiUrl.trim());
        }

        return Optional.empty();
    }

    /**
     * Doan URL co dang API endpoint hay khong.
     *
     * @param url URL dau vao
     * @return true neu URL co dau hieu la API
     */
    private boolean looksLikeApiUrl(String url) {
        String normalized = url.toLowerCase();
        return normalized.contains("/api")
                || normalized.contains("cloudapi.")
                || normalized.contains("sapi.")
                || normalized.contains("base-api");
    }

    /**
     * Phat hien body co schema giong JcTrans company search API hay khong.
     *
     * @param rawBody raw JSON response
     * @return true neu body co data.records va compName
     */
    private boolean looksLikeJcTransCompanyApi(String rawBody) {
        try {
            return objectMapper.readTree(rawBody)
                    .path("data")
                    .path("records")
                    .path(0)
                    .hasNonNull("compName");
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Kiem tra body co dang JSON hay khong.
     *
     * @param rawBody body thuan dang string
     * @return true neu body co dang JSON object/array
     */
    private boolean looksLikeJson(String rawBody) {
        String trimmed = rawBody == null ? "" : rawBody.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    /**
     * Cat ngan body de log preview, tranh log qua dai.
     *
     * @param rawBody body thuan dang string
     * @return body da duoc cat ngan neu can
     */
    private String limit(String rawBody) {
        if (rawBody == null) {
            return "";
        }
        int maxLength = crawlerProperties.getHttp().getMaxBodyLogLength();
        return rawBody.length() <= maxLength ? rawBody : rawBody.substring(0, maxLength) + "...";
    }
}
