package com.quangph.crawlerapp.service.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quangph.crawlerapp.config.CrawlerProperties;
import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.service.site.JcTransCompanyParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Strategy ưu tiên tìm và gọi API public trước khi fallback sang HTML/Playwright.
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
     * Trả về tên strategy.
     *
     * @return tên strategy API
     */
    @Override
    public String getName() {
        return "API";
    }

    /**
     * Tạm thời cho phép strategy API được thử với mọi URL.
     *
     * @param url URL cần crawl
     * @return true
     */
    @Override
    public boolean supports(String url) {
        return true;
    }

    @Override
    public CrawlExecutionResult crawl(String url) {
        return null;
    }

    /**
     * Thử tìm API mapping và gọi API để lấy data.
     *
     * @return kết quả crawl bằng API
     */
    @Override
    public CrawlExecutionResult crawl(CrawlRequest crawlRequest) {

        String pageUrl = crawlRequest.pageUrl();
        Integer countryId = crawlRequest.countryId();
        Integer pageSize = crawlRequest.pageSize();
        Integer page = crawlRequest.page();
        String token = crawlRequest.token();

        Optional<String> apiUrlOptional = resolveApiUrl(pageUrl);
        if (apiUrlOptional.isEmpty()) {
            return CrawlExecutionResult.empty("Khong tim thay API mapping public phu hop");
        }

        String apiUrl = apiUrlOptional.get();
        Request request = buildJcTransRequest(apiUrl, page, pageSize, countryId, token);

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

            // Định nghĩa chuẩn: nếu là JSON thì parse bằng Jackson.
            if (looksLikeJson(rawBody)) {
                objectMapper.readTree(rawBody);
                if (jcTransCompanyParser.supports(pageUrl) || looksLikeJcTransCompanyApi(rawBody)) {
                    List<JsonNode> listItems  = jcTransCompanyParser.extractApiRecords(rawBody);
                    if (!listItems.isEmpty()) {
                        List<JsonNode> enrichedItems = enrichCompanyDetails(listItems, token);

                        return new CrawlExecutionResult(
                                enrichedItems,
                                "Lấy data từ list và detail company JSON API Response"
                        );
                    }
                }

                return CrawlExecutionResult.empty("API tra JSON nhung khong parse duoc thanh data");
            }

//            if (jcTransCompanyParser.supports(pageUrl)) {
//                List<JsonNode> items = jcTransCompanyParser.parse(rawBody, pageUrl)
//                        .stream()
//                        .<JsonNode>map(item -> objectMapper.valueToTree(item))
//                        .toList();
//                if (!items.isEmpty()) {
//                    return new CrawlExecutionResult(items, "Lay du lieu raw tu API/HTML response");
//                }
//            }

            return CrawlExecutionResult.empty("API response khong parse duoc thanh data");
        } catch (IOException exception) {
            return CrawlExecutionResult.empty("Loi goi API: " + exception.getMessage());
        }
    }

    /**
     * Resolve API URL từ URL đầu vào.
     * Hiện tại để hardcode cho việc mở rộng sau này, JcTrans chưa map sẵn API công khai ổn định.
     *
     * @param url URL gốc
     * @return API URL nếu tìm thấy
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
     * Đoán URL có dạng API endpoint hay không.
     *
     * @param url URL đầu vào
     * @return true nếu URL có dấu hiệu là API
     */
    private boolean looksLikeApiUrl(String url) {
        String normalized = url.toLowerCase();
        return normalized.contains("/api")
                || normalized.contains("cloudapi.")
                || normalized.contains("sapi.")
                || normalized.contains("base-api");
    }

    /**
     * Phát hiện body có schema giống JcTrans company search API hay không.
     *
     * @param rawBody raw JSON response
     * @return true nếu body có data.records và compName
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
     * Kiểm tra body có dạng JSON hay không.
     *
     * @param rawBody body thuần dạng string
     * @return true nếu body có dạng JSON object/array
     */
    private boolean looksLikeJson(String rawBody) {
        String trimmed = rawBody == null ? "" : rawBody.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    /**
     * Cắt ngắn body để log preview, tránh log quá dài.
     *
     * @param rawBody body thuần dạng string
     * @return body đã được cắt ngắn nếu cần
     */
    private String limit(String rawBody) {
        if (rawBody == null) {
            return "";
        }
        int maxLength = crawlerProperties.getHttp().getMaxBodyLogLength();
        return rawBody.length() <= maxLength ? rawBody : rawBody.substring(0, maxLength) + "...";
    }

    private Request buildJcTransRequest(String apiUrl, int current, int size, int countryId, String token) {

        try {
            Map<String, Object> payload = new HashMap<>();

            payload.put("current", current);
            payload.put("size", size);
            payload.put("countryId", countryId);
            if (StringUtils.hasText(token)) {
                payload.put("authorization", "Bearer " + token);
            }

            String jsonBody = objectMapper.writeValueAsString(payload);

            RequestBody body = RequestBody.create(
                    jsonBody,
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request.Builder builder = new Request.Builder()
                    .url(apiUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("Origin", "https://www.jctrans.com")
                    .header("Referer", "https://www.jctrans.com/en/company/")
                    .post(body);

            // add Authorization nếu có token
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            return builder.build();
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    private List<JsonNode> enrichCompanyDetails(List<JsonNode> listItems, String token) {
        List<JsonNode> result = new ArrayList<>();

        for (JsonNode listItem : listItems) {
            String compUid = listItem.path("compUid").asText(null);

            if (compUid == null || compUid.isBlank()) {
                result.add(listItem);
                continue;
            }

            JsonNode detail = callCompanyDetailApi(compUid, token);

            ObjectNode merged = objectMapper.createObjectNode();
            merged.set("list", listItem);

            if (detail != null && !detail.isMissingNode() && !detail.isNull()) {
                merged.set("detail", detail);
            } else {
                merged.set("detail", objectMapper.createObjectNode());
            }

            result.add(merged);
        }

        return result;
    }

    private JsonNode callCompanyDetailApi(String companyId, String token) {
        String detailApiUrl = "https://cloudapi.jctrans.com/era/fr/shop/getEraShopInfoDetail";

        try {
            ObjectNode bodyJson = objectMapper.createObjectNode();
            bodyJson.put("compUid", companyId);

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(bodyJson),
                    MediaType.parse("application/json;charset=UTF-8")
            );

            Request.Builder builder = new Request.Builder()
                    .url(detailApiUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("Origin", "https://www.jctrans.com")
                    .header("Referer", "https://www.jctrans.com/en/company/")
                    .post(body);

            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }

            try (Response response = okHttpClient.newCall(builder.build()).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return null;
                }

                String rawBody = response.body().string();
                JsonNode root = objectMapper.readTree(rawBody);

                return root.path("data");
            }

        } catch (Exception e) {
            log.warn("Khong lay duoc detail companyId={}", companyId, e);
            return null;
        }
    }
}
