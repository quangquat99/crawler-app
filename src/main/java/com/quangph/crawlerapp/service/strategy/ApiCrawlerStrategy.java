package com.quangph.crawlerapp.service.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quangph.crawlerapp.config.CrawlerProperties;
import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.service.site.JcTransCompanyParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public String getName() {
        return "API";
    }

    @Override
    public boolean supports(String url) {
        return true;
    }

    @Override
    public CrawlExecutionResult crawl(CrawlRequest crawlRequest) {
        String pageUrl = crawlRequest.pageUrl();
        Integer countryId = crawlRequest.countryId();
        Integer pageSize = crawlRequest.pageSize();
        Integer page = crawlRequest.page();
        String token = crawlRequest.token();

        Optional<String> apiUrlOptional = resolveApiUrl(pageUrl);
        if (apiUrlOptional.isEmpty()) {
            return CrawlExecutionResult.failure("Không tìm thấy API public phù hợp để crawl dữ liệu.");
        }

        String apiUrl = apiUrlOptional.get();
        Request request = buildJcTransRequest(apiUrl, page, pageSize, countryId, token);

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return CrawlExecutionResult.failure("API trả về mã trạng thái " + response.code() + ".");
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return CrawlExecutionResult.failure("API không trả về nội dung dữ liệu.");
            }

            String rawBody = responseBody.string();
            if (log.isDebugEnabled()) {
                log.debug("API body preview: {}", limit(rawBody));
            }

            if (looksLikeJson(rawBody)) {
                JsonNode root = objectMapper.readTree(rawBody);
                if (jcTransCompanyParser.supports(pageUrl) || looksLikeJcTransCompanyApi(root)) {
                    List<JsonNode> listItems = jcTransCompanyParser.extractApiRecords(root);
                    List<JsonNode> enrichedItems = enrichCompanyDetails(listItems, token);
                    long totalItems = resolveTotalItems(root, enrichedItems.size());
                    int totalPages = resolveTotalPages(root, pageSize, totalItems);

                    return CrawlExecutionResult.success(
                            enrichedItems,
                            enrichedItems.isEmpty()
                                    ? "Crawl thành công nhưng không có dữ liệu ở trang hiện tại."
                                    : "Đã lấy dữ liệu từ API danh sách và chi tiết công ty.",
                            totalItems,
                            totalPages
                    );
                }

                return CrawlExecutionResult.failure("API trả về JSON nhưng không parse được thành dữ liệu.");
            }

            return CrawlExecutionResult.failure("Phản hồi từ API không parse được thành dữ liệu.");
        } catch (InterruptedIOException exception) {
            log.warn("Crawl timeout via strategy={} url={} page={} pageSize={}",
                    getName(), pageUrl, page, pageSize, exception);
            return CrawlExecutionResult.failure("Crawl bị timeout, vui lòng thử lại sau.");
        } catch (IOException exception) {
            log.warn("Crawl IO error via strategy={} url={} page={} pageSize={}",
                    getName(), pageUrl, page, pageSize, exception);
            return CrawlExecutionResult.failure("Crawl thất bại do lỗi kết nối, vui lòng thử lại.");
        }
    }

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

    private boolean looksLikeApiUrl(String url) {
        String normalized = url.toLowerCase();
        return normalized.contains("/api")
                || normalized.contains("cloudapi.")
                || normalized.contains("sapi.")
                || normalized.contains("base-api");
    }

    private boolean looksLikeJcTransCompanyApi(JsonNode root) {
        return root.path("data")
                .path("records")
                .path(0)
                .hasNonNull("compName");
    }

    private boolean looksLikeJson(String rawBody) {
        String trimmed = rawBody == null ? "" : rawBody.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    private String limit(String rawBody) {
        if (rawBody == null) {
            return "";
        }
        int maxLength = crawlerProperties.getHttp().getMaxBodyLogLength();
        return rawBody.length() <= maxLength ? rawBody : rawBody.substring(0, maxLength) + "...";
    }

    private long resolveTotalItems(JsonNode root, int fallbackItemCount) {
        JsonNode data = root.path("data");
        long totalItems = firstPositiveLong(data, "total", "totalItems", "totalCount", "count");
        return totalItems > 0 ? totalItems : fallbackItemCount;
    }

    private int resolveTotalPages(JsonNode root, int pageSize, long totalItems) {
        JsonNode data = root.path("data");
        int totalPages = firstPositiveInt(data, "pages", "totalPages", "pageCount");
        if (totalPages > 0) {
            return totalPages;
        }
        if (totalItems <= 0 || pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    private long firstPositiveLong(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode child = node.path(fieldName);
            long value = child.asLong(0);
            if (value > 0) {
                return value;
            }
        }
        return 0;
    }

    private int firstPositiveInt(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode child = node.path(fieldName);
            int value = child.asInt(0);
            if (value > 0) {
                return value;
            }
        }
        return 0;
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
            merged.set("detail", detail != null && !detail.isMissingNode() && !detail.isNull()
                    ? detail
                    : objectMapper.createObjectNode());
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
        } catch (Exception exception) {
            log.warn("Không lấy được chi tiết companyId={}", companyId, exception);
            return null;
        }
    }
}
