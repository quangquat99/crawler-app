package com.quangph.crawlerapp.service.export;

import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.response.CrawlResponse;
import com.quangph.crawlerapp.dto.response.CrawledCompanyExcelRow;
import com.quangph.crawlerapp.dto.response.CrawledCompanyRow;
import com.quangph.crawlerapp.service.CrawlOrchestratorService;
import com.quangph.crawlerapp.service.ExcelExportService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
public class ExportAllJobWorker {

    private static final Logger log = LoggerFactory.getLogger(ExportAllJobWorker.class);

    private final ExportJobRegistry exportJobRegistry;
    private final CrawlOrchestratorService crawlOrchestratorService;
    private final ExcelExportService excelExportService;

    public ExportAllJobWorker(
            ExportJobRegistry exportJobRegistry,
            CrawlOrchestratorService crawlOrchestratorService,
            ExcelExportService excelExportService
    ) {
        this.exportJobRegistry = exportJobRegistry;
        this.crawlOrchestratorService = crawlOrchestratorService;
        this.excelExportService = excelExportService;
    }

    @Async("crawlTaskExecutor")
    public void process(String jobId, CrawlRequest request) {
        ExportJobState jobState = exportJobRegistry.find(jobId)
                .orElseThrow(() -> new IllegalStateException("Export job not found: " + jobId));

        jobState.markRunning("Export job started");

        Path exportDirectory = Path.of(System.getProperty("java.io.tmpdir"), "crawler-exports");
        Path outputFile = exportDirectory.resolve(jobId + ".xlsx");

        SXSSFWorkbook workbook = excelExportService.createStreamingWorkbook();
        try {
            Files.createDirectories(exportDirectory);

            Sheet sheet = excelExportService.createCompanySheet(workbook);
            int rowIndex = 1;
            CrawlRequest firstPageRequest = new CrawlRequest(
                    request.pageUrl(),
                    1,
                    request.pageSize(),
                    request.countryId(),
                    request.token()
            );

            Instant firstPageStartedAt = Instant.now();
            CrawlResponse firstPageResponse = crawlOrchestratorService.crawl(firstPageRequest);
            if (!firstPageResponse.success()) {
                throw new IllegalStateException(firstPageResponse.message());
            }

            long totalItems = firstPageResponse.totalItems() > 0 ? firstPageResponse.totalItems() : firstPageResponse.items().size();
            int totalPages = firstPageResponse.totalPages() > 0
                    ? firstPageResponse.totalPages()
                    : Math.max(1, (int) Math.ceil((double) totalItems / request.pageSize()));

            rowIndex = excelExportService.appendCompanyRows(
                    sheet,
                    mapToExcelRows(firstPageResponse.items()),
                    rowIndex
            );

            long processedItems = firstPageResponse.items().size();
            updateJobProgress(jobState, 1, totalPages, processedItems, totalItems);
            logPageProgress(jobId, 1, request.pageSize(), jobState.getProgressPercent(), firstPageStartedAt, Instant.now());

            for (int page = 2; page <= totalPages; page++) {
                Instant pageStartedAt = Instant.now();
                CrawlRequest pageRequest = new CrawlRequest(
                        request.pageUrl(),
                        page,
                        request.pageSize(),
                        request.countryId(),
                        request.token()
                );

                CrawlResponse pageResponse = crawlOrchestratorService.crawl(pageRequest);
                if (!pageResponse.success()) {
                    throw new IllegalStateException(pageResponse.message());
                }

                rowIndex = excelExportService.appendCompanyRows(
                        sheet,
                        mapToExcelRows(pageResponse.items()),
                        rowIndex
                );

                processedItems += pageResponse.items().size();
                updateJobProgress(jobState, page, totalPages, processedItems, totalItems);
                logPageProgress(jobId, page, request.pageSize(), jobState.getProgressPercent(), pageStartedAt, Instant.now());
            }

            try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
                excelExportService.writeWorkbook(workbook, outputStream);
            }

            jobState.markDone(
                    totalPages,
                    totalPages,
                    processedItems,
                    totalItems,
                    outputFile,
                    "Export all completed"
            );
        } catch (IOException exception) {
            log.warn("export_all_failed jobId={} message={}", jobId, exception.getMessage());
            jobState.markFailed("Export failed due to IO error: " + exception.getMessage());
        } catch (Exception exception) {
            log.warn("export_all_failed jobId={} message={}", jobId, exception.getMessage());
            jobState.markFailed(exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Export all failed"
                    : exception.getMessage());
        } finally {
            workbook.dispose();
            try {
                workbook.close();
            } catch (IOException ignored) {
                // no-op
            }
        }
    }

    private void updateJobProgress(
            ExportJobState jobState,
            int currentPage,
            int totalPages,
            long processedItems,
            long totalItems
    ) {
        int progressPercent = totalPages <= 0 ? 0 : Math.min(100, (int) Math.round((currentPage * 100.0) / totalPages));
        jobState.updateProgress(
                currentPage,
                totalPages,
                processedItems,
                totalItems,
                progressPercent,
                "Processed page " + currentPage + "/" + totalPages
        );
    }

    private void logPageProgress(
            String jobId,
            int page,
            int pageSize,
            int progressPercent,
            Instant startedAt,
            Instant endedAt
    ) {
        long durationMs = endedAt.toEpochMilli() - startedAt.toEpochMilli();
        log.info("export_all_progress jobId={} page={} pageSize={} progress={} durationMs={}",
                jobId,
                page,
                pageSize,
                progressPercent,
                durationMs);
    }

    private List<CrawledCompanyExcelRow> mapToExcelRows(List<CrawledCompanyRow> rows) {
        return rows.stream()
                .map(this::mapToExcelRow)
                .toList();
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
}
