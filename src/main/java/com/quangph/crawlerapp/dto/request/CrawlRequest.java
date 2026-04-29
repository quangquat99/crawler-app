package com.quangph.crawlerapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

/**
 * DTO đầu vào cho API crawl.
 */
public record CrawlRequest(

        @NotBlank(message = "url khong duoc de trong")
        @URL(message = "url khong hop le")
        String pageUrl,

        @NotNull(message = "page khong duoc de trong")
        @Min(value = 1, message = "page phai >= 1")
        Integer page,

        @NotNull(message = "pageSize khong duoc de trong")
        @Min(value = 1, message = "pageSize phai >= 1")
        Integer pageSize,

        @NotNull(message = "countryId khong duoc de trong")
        @Min(value = 1, message = "countryId phai >= 1")
        Integer countryId,

        String token
) {
}
