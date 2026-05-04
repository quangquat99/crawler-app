package com.quangph.crawlerapp.service.export;

import java.nio.file.Path;
import java.time.Instant;

public class ExportJobState {

    private final String jobId;
    private final Instant createdAt;

    private ExportJobStatus status;
    private int currentPage;
    private int totalPages;
    private long processedItems;
    private long totalItems;
    private int progressPercent;
    private String message;
    private Path filePath;
    private Instant updatedAt;

    public ExportJobState(String jobId) {
        this.jobId = jobId;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.status = ExportJobStatus.PENDING;
        this.message = "Export job created";
    }

    public synchronized String getJobId() {
        return jobId;
    }

    public synchronized ExportJobStatus getStatus() {
        return status;
    }

    public synchronized int getCurrentPage() {
        return currentPage;
    }

    public synchronized int getTotalPages() {
        return totalPages;
    }

    public synchronized long getProcessedItems() {
        return processedItems;
    }

    public synchronized long getTotalItems() {
        return totalItems;
    }

    public synchronized int getProgressPercent() {
        return progressPercent;
    }

    public synchronized String getMessage() {
        return message;
    }

    public synchronized Path getFilePath() {
        return filePath;
    }

    public synchronized Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized Instant getUpdatedAt() {
        return updatedAt;
    }

    public synchronized void markRunning(String message) {
        this.status = ExportJobStatus.RUNNING;
        this.message = message;
        this.updatedAt = Instant.now();
    }

    public synchronized void updateProgress(
            int currentPage,
            int totalPages,
            long processedItems,
            long totalItems,
            int progressPercent,
            String message
    ) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.processedItems = processedItems;
        this.totalItems = totalItems;
        this.progressPercent = progressPercent;
        this.message = message;
        this.updatedAt = Instant.now();
    }

    public synchronized void markDone(
            int currentPage,
            int totalPages,
            long processedItems,
            long totalItems,
            Path filePath,
            String message
    ) {
        this.status = ExportJobStatus.DONE;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.processedItems = processedItems;
        this.totalItems = totalItems;
        this.progressPercent = 100;
        this.filePath = filePath;
        this.message = message;
        this.updatedAt = Instant.now();
    }

    public synchronized void markFailed(String message) {
        this.status = ExportJobStatus.FAILED;
        this.message = message;
        this.updatedAt = Instant.now();
    }
}
