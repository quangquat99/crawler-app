package com.quangph.crawlerapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entity mô tả job crawl để sẵn cho việc lưu database sau này.
 */
@Entity
@Table(name = "crawl_job")
public class CrawlJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String sourceUrl;

    @Column(nullable = false, length = 50)
    private String strategyUsed;

    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Lấy id của job crawl.
     *
     * @return id job
     */
    public Long getId() {
        return id;
    }

    /**
     * Lấy URL nguồn đã được crawl.
     *
     * @return URL nguồn
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Gán URL nguồn cho job crawl.
     *
     * @param sourceUrl URL nguồn
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Lấy tên chiến lược đã được sử dụng.
     *
     * @return tên strategy
     */
    public String getStrategyUsed() {
        return strategyUsed;
    }

    /**
     * Gán tên chiến lược đã được sử dụng.
     *
     * @param strategyUsed tên strategy
     */
    public void setStrategyUsed(String strategyUsed) {
        this.strategyUsed = strategyUsed;
    }

    /**
     * Lấy thời điểm tạo job crawl.
     *
     * @return thời điểm tạo
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gán thời điểm tạo job crawl.
     *
     * @param createdAt thời điểm tạo
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
