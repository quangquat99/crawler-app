package com.quangph.crawlerapp.service.export;

import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.request.ExportSelectedPagesRequest;
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
    public void processAllPages(String jobId, CrawlRequest request) {
        CrawlRequest normalizedRequest = new CrawlRequest(
                request.pageUrl(),
                1,
                CrawlRequest.FIXED_PAGE_SIZE,
                request.countryId(),
                request.token()
        );
        processExportJob(jobId, normalizedRequest, null, "Đang xuất tất cả các trang...");
    }

    @Async("crawlTaskExecutor")
    public void processSelectedPages(String jobId, ExportSelectedPagesRequest request) {
        CrawlRequest baseRequest = new CrawlRequest(
                request.pageUrl(),
                1,
                CrawlRequest.FIXED_PAGE_SIZE,
                request.countryId(),
                request.token()
        );
        processExportJob(jobId, baseRequest, request.pages(), "Đang xuất các trang đã chọn...");
    }

    private void processExportJob(
            String jobId,
            CrawlRequest baseRequest,
            List<Integer> selectedPages,
            String startMessage
    ) {
        ExportJobState jobState = exportJobRegistry.find(jobId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tác vụ xuất dữ liệu: " + jobId));

        jobState.markRunning(startMessage);

        Path exportDirectory = Path.of(System.getProperty("java.io.tmpdir"), "crawler-exports");
        Path outputFile = exportDirectory.resolve(jobId + ".xlsx");

        SXSSFWorkbook workbook = excelExportService.createStreamingWorkbook();
        try {
            Files.createDirectories(exportDirectory);
            Sheet sheet = excelExportService.createCompanySheet(workbook);

            Instant metadataStartedAt = Instant.now();
            CrawlResponse metadataResponse = crawlOrchestratorService.crawl(baseRequest);
            if (!metadataResponse.success()) {
                throw new IllegalStateException(metadataResponse.message());
            }
            logPageProgress(jobId, 1, CrawlRequest.FIXED_PAGE_SIZE, 0, metadataStartedAt, Instant.now());

            long totalItems = metadataResponse.totalItems() > 0 ? metadataResponse.totalItems() : metadataResponse.items().size();
            int discoveredTotalPages = metadataResponse.totalPages() > 0
                    ? metadataResponse.totalPages()
                    : Math.max(1, (int) Math.ceil((double) totalItems / CrawlRequest.FIXED_PAGE_SIZE));

            List<Integer> pagesToExport = selectedPages == null || selectedPages.isEmpty()
                    ? buildSequentialPages(discoveredTotalPages)
                    : validateSelectedPages(selectedPages, discoveredTotalPages);

            int rowIndex = 1;
            long processedItems = 0;
            int processedPageCount = 0;
            int totalSelectedPages = pagesToExport.size();

            for (Integer pageNumber : pagesToExport) {
                Instant pageStartedAt = Instant.now();
                CrawlRequest pageRequest = new CrawlRequest(
                        baseRequest.pageUrl(),
                        pageNumber,
                        CrawlRequest.FIXED_PAGE_SIZE,
                        baseRequest.countryId(),
                        baseRequest.token()
                );

                CrawlResponse pageResponse = pageNumber == 1
                        ? metadataResponse
                        : crawlOrchestratorService.crawl(pageRequest);

                if (!pageResponse.success()) {
                    throw new IllegalStateException(pageResponse.message());
                }

                rowIndex = excelExportService.appendCompanyRows(
                        sheet,
                        mapToExcelRows(pageResponse.items()),
                        rowIndex
                );

                processedItems += pageResponse.items().size();
                processedPageCount += 1;

                int progressPercent = Math.min(100, (int) Math.round((processedPageCount * 100.0) / totalSelectedPages));
                jobState.updateProgress(
                        pageNumber,
                        totalSelectedPages,
                        processedItems,
                        totalItems,
                        progressPercent,
                        "Đang xử lý trang " + pageNumber + " (" + processedPageCount + "/" + totalSelectedPages + ")"
                );

                logPageProgress(jobId, pageNumber, CrawlRequest.FIXED_PAGE_SIZE, progressPercent, pageStartedAt, Instant.now());
            }

            try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
                excelExportService.writeWorkbook(workbook, outputStream);
            }

            jobState.markDone(
                    pagesToExport.get(pagesToExport.size() - 1),
                    totalSelectedPages,
                    processedItems,
                    totalItems,
                    outputFile,
                    "Đã xuất file Excel thành công."
            );
        } catch (IOException exception) {
            log.warn("export_job_failed jobId={} message={}", jobId, exception.getMessage());
            jobState.markFailed("Xuất file thất bại do lỗi IO: " + exception.getMessage());
        } catch (Exception exception) {
            log.warn("export_job_failed jobId={} message={}", jobId, exception.getMessage());
            jobState.markFailed(exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Xuất file thất bại."
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

    private List<Integer> validateSelectedPages(List<Integer> selectedPages, int totalPages) {
        List<Integer> validPages = selectedPages.stream()
                .distinct()
                .sorted()
                .toList();

        boolean hasOutOfRangePage = validPages.stream().anyMatch(page -> page < 1 || page > totalPages);
        if (hasOutOfRangePage) {
            throw new IllegalStateException("Danh sách trang đã chọn vượt quá tổng số trang hiện có.");
        }

        return validPages;
    }

    private List<Integer> buildSequentialPages(int totalPages) {
        return java.util.stream.IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
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
        log.info("export_job_progress jobId={} page={} pageSize={} progress={} durationMs={}",
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
