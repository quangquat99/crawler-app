package com.quangph.crawlerapp.dto.response;

public record ExportJobStartResponse(
        String jobId,
        String status,
        String message
) {
}
