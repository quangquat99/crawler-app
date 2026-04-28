package com.quangph.crawlerapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * DTO dau vao cho API crawl.
 */
public record CrawlRequest(
        @NotBlank(message = "url khong duoc de trong")
        @URL(message = "url khong hop le")
        String url
) {
}
