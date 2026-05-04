package com.quangph.crawlerapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

/**
 * DTO đầu vào cho API crawl.
 */
public record CrawlRequest(

        @NotBlank(message = "URL không được để trống")
        @URL(message = "URL không hợp lệ")
        String pageUrl,

        @NotNull(message = "Trang không được để trống")
        @Min(value = 1, message = "Trang phải lớn hơn hoặc bằng 1")
        Integer page,

        @Min(value = 1, message = "Kích thước trang phải lớn hơn hoặc bằng 1")
        Integer pageSize,

        @NotNull(message = "Quốc gia không được để trống")
        @Min(value = 1, message = "Quốc gia phải lớn hơn hoặc bằng 1")
        Integer countryId,

        String token
) {
    public static final int FIXED_PAGE_SIZE = 20;

    public CrawlRequest {
        pageSize = pageSize == null ? FIXED_PAGE_SIZE : pageSize;
    }
}
