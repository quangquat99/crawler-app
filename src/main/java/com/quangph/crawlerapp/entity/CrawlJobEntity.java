package com.quangph.crawlerapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entity mo ta job crawl de san cho viec luu database sau nay.
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
     * Lay id cua job crawl.
     *
     * @return id job
     */
    public Long getId() {
        return id;
    }

    /**
     * Lay URL nguon da duoc crawl.
     *
     * @return URL nguon
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Gan URL nguon cho job crawl.
     *
     * @param sourceUrl URL nguon
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Lay ten chien luoc da duoc su dung.
     *
     * @return ten strategy
     */
    public String getStrategyUsed() {
        return strategyUsed;
    }

    /**
     * Gan ten chien luoc da duoc su dung.
     *
     * @param strategyUsed ten strategy
     */
    public void setStrategyUsed(String strategyUsed) {
        this.strategyUsed = strategyUsed;
    }

    /**
     * Lay thoi diem tao job crawl.
     *
     * @return thoi diem tao
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gan thoi diem tao job crawl.
     *
     * @param createdAt thoi diem tao
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
