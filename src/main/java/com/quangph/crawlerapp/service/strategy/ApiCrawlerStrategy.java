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
            // Demo Jctrans hien tai chua co public API mapping on dinh, nen block nay de san cho site khac.
            if (looksLikeJson(rawBody)) {
                objectMapper.readTree(rawBody);
                return CrawlExecutionResult.empty("Da tim thay API nhung chua khai bao JSON mapper cho site nay");
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

    private Optional<String> resolveApiUrl(String url) {
        // Demo hardcode cho Jctrans: trang list cong ty hien khong co public API endpoint on dinh
        // duoc map san trong project. Strategy nay van duoc chay dau tien de giu dung flow senior.
        return Optional.empty();
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
}
