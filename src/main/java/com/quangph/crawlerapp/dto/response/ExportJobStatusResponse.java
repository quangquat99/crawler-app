package com.quangph.crawlerapp.dto.response;

public record ExportJobStatusResponse(
        String jobId,
        String status,
        int currentPage,
        int totalPages,
        long processedItems,
        long totalItems,
        int progressPercent,
        String message,
        String downloadUrl
) {
}
