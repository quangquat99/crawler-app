package com.quangph.crawlerapp.service.export;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ExportJobRegistry {

    private final ConcurrentMap<String, ExportJobState> jobs = new ConcurrentHashMap<>();

    public ExportJobState create(String jobId) {
        ExportJobState state = new ExportJobState(jobId);
        jobs.put(jobId, state);
        return state;
    }

    public Optional<ExportJobState> find(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }
}
