package com.quangph.crawlerapp.repository;

import com.quangph.crawlerapp.entity.CrawlJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlJobRepository extends JpaRepository<CrawlJobEntity, Long> {
}
