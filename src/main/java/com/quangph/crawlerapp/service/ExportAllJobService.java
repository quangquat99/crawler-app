package com.quangph.crawlerapp.service;

import com.quangph.crawlerapp.dto.request.CrawlRequest;
import com.quangph.crawlerapp.dto.response.ExportJobStartResponse;
import com.quangph.crawlerapp.dto.response.ExportJobStatusResponse;
import com.quangph.crawlerapp.service.export.ExportAllJobWorker;
import com.quangph.crawlerapp.service.export.ExportJobRegistry;
import com.quangph.crawlerapp.service.export.ExportJobState;
import com.quangph.crawlerapp.service.export.ExportJobStatus;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExportAllJobService {

    private final ExportJobRegistry exportJobRegistry;
    private final ExportAllJobWorker exportAllJobWorker;

    public ExportAllJobService(ExportJobRegistry exportJobRegistry, ExportAllJobWorker exportAllJobWorker) {
        this.exportJobRegistry = exportJobRegistry;
        this.exportAllJobWorker = exportAllJobWorker;
    }

    public ExportJobStartResponse createJob(CrawlRequest request) {
        String jobId = UUID.randomUUID().toString();
        exportJobRegistry.create(jobId);
        exportAllJobWorker.process(jobId, request);
        return new ExportJobStartResponse(jobId, ExportJobStatus.PENDING.name(), "Export job created");
    }

    public Optional<ExportJobStatusResponse> getJobStatus(String jobId) {
        return exportJobRegistry.find(jobId)
                .map(state -> toStatusResponse(state, buildDownloadUrl(jobId)));
    }

    public Optional<ExportJobState> getJob(String jobId) {
        return exportJobRegistry.find(jobId);
    }

    public boolean isDownloadable(ExportJobState state) {
        return state.getStatus() == ExportJobStatus.DONE
                && state.getFilePath() != null
                && Files.exists(state.getFilePath());
    }

    public Path getDownloadPath(ExportJobState state) {
        return state.getFilePath();
    }

    private ExportJobStatusResponse toStatusResponse(ExportJobState state, String downloadUrl) {
        return new ExportJobStatusResponse(
                state.getJobId(),
                state.getStatus().name(),
                state.getCurrentPage(),
                state.getTotalPages(),
                state.getProcessedItems(),
                state.getTotalItems(),
                state.getProgressPercent(),
                state.getMessage(),
                downloadUrl
        );
    }

    private String buildDownloadUrl(String jobId) {
        return "/api/v1/crawls/export-jobs/" + jobId + "/download";
    }
}
