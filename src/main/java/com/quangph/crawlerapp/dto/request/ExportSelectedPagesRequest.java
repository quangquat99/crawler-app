package com.quangph.crawlerapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.Comparator;
import java.util.List;

public record ExportSelectedPagesRequest(
        @NotBlank(message = "URL không được để trống")
        @URL(message = "URL không hợp lệ")
        String pageUrl,

        @Min(value = 1, message = "Kích thước trang phải lớn hơn hoặc bằng 1")
        Integer pageSize,

        @NotNull(message = "Quốc gia không được để trống")
        @Min(value = 1, message = "Quốc gia phải lớn hơn hoặc bằng 1")
        Integer countryId,

        @NotEmpty(message = "Danh sách trang không được để trống")
        List<@NotNull(message = "Trang không hợp lệ") @Min(value = 1, message = "Trang phải lớn hơn hoặc bằng 1") Integer> pages,

        String token
) {
    public ExportSelectedPagesRequest {
        pageSize = pageSize == null ? CrawlRequest.FIXED_PAGE_SIZE : pageSize;
        pages = pages == null
                ? List.of()
                : pages.stream()
                .filter(page -> page != null && page >= 1)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
