package com.quangph.crawlerapp.controller;

import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.response.CrawlResponse;
import com.quangph.crawlerapp.service.CrawlOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller nhận request crawl từ bên ngoài.
 */
@RestController
@RequestMapping("/api/v1/crawls")
public class CrawlController {

    private final CrawlOrchestratorService crawlOrchestratorService;

    public CrawlController(CrawlOrchestratorService crawlOrchestratorService) {
        this.crawlOrchestratorService = crawlOrchestratorService;
    }

    /**
     * Endpoint crawl một URL và trả kết quả data crawl được.
     *
     * @param request request chứa URL cần crawl
     * @return response chứa chiến lược được dùng và danh sách item
     */
    @PostMapping
    public ResponseEntity<CrawlResponse> crawl(@Valid @RequestBody CrawlRequest request) {
        return ResponseEntity.ok(crawlOrchestratorService.crawl(request));
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> crawlAndExport(@Valid @RequestBody CrawlRequest request) {
        byte[] excelBytes = crawlOrchestratorService.crawlAndExportExcel(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=crawl-result.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(excelBytes);
    }
}
