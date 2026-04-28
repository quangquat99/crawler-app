package com.quangph.crawlerapp.controller;

import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.response.CrawlResponse;
import com.quangph.crawlerapp.service.CrawlOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crawls")
public class CrawlController {

    private final CrawlOrchestratorService crawlOrchestratorService;

    public CrawlController(CrawlOrchestratorService crawlOrchestratorService) {
        this.crawlOrchestratorService = crawlOrchestratorService;
    }

    @PostMapping
    public ResponseEntity<CrawlResponse> crawl(@Valid @RequestBody CrawlRequest request) {
        return ResponseEntity.ok(crawlOrchestratorService.crawl(request.url()));
    }
}
