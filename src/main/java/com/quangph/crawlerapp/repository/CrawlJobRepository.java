package com.quangph.crawlerapp.repository;

import com.quangph.crawlerapp.entity.CrawlJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository JPA de thao tac voi bang crawl_job khi can luu database.
 */
public interface CrawlJobRepository extends JpaRepository<CrawlJobEntity, Long> {
}
