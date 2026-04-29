package com.quangph.crawlerapp.repository;

import com.quangph.crawlerapp.entity.CrawlJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository JPA để thao tác với bảng crawl_job khi cần lưu database.
 */
public interface CrawlJobRepository extends JpaRepository<CrawlJobEntity, Long> {
}
