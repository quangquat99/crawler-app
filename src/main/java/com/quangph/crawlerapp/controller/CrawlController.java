package com.quangph.crawlerapp.controller;

import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.response.CrawlResponse;
import com.quangph.crawlerapp.dto.response.ExportJobStartResponse;
import com.quangph.crawlerapp.service.ExportAllJobService;
import com.quangph.crawlerapp.service.CrawlOrchestratorService;
import com.quangph.crawlerapp.service.export.ExportJobState;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller nhận request crawl từ bên ngoài.
 */
@RestController
@RequestMapping("/api/v1/crawls")
public class CrawlController {

    private final CrawlOrchestratorService crawlOrchestratorService;
    private final ExportAllJobService exportAllJobService;

    public CrawlController(
            CrawlOrchestratorService crawlOrchestratorService,
            ExportAllJobService exportAllJobService
    ) {
        this.crawlOrchestratorService = crawlOrchestratorService;
        this.exportAllJobService = exportAllJobService;
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

    @PostMapping("/export-all")
    public ResponseEntity<ExportJobStartResponse> exportAll(@Valid @RequestBody CrawlRequest request) {
        return ResponseEntity.ok(exportAllJobService.createJob(request));
    }

    @GetMapping("/export-jobs/{jobId}")
    public ResponseEntity<?> getExportJobStatus(@PathVariable String jobId) {
        return exportAllJobService.getJobStatus(jobId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "Export job not found"
                )));
    }

    @GetMapping("/export-jobs/{jobId}/download")
    public ResponseEntity<?> downloadExportJob(@PathVariable String jobId) {
        ExportJobState jobState = exportAllJobService.getJob(jobId).orElse(null);
        if (jobState == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Export job not found"
            ));
        }

        if (!exportAllJobService.isDownloadable(jobState)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "Export job is not ready for download"
            ));
        }

        Resource resource = new FileSystemResource(exportAllJobService.getDownloadPath(jobState));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export-all-" + jobId + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(resource);
    }
}
