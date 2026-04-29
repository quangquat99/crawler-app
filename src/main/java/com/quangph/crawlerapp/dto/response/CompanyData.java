package com.quangph.crawlerapp.dto.response;

import java.util.List;

/**
 * DTO đại diện cho một bản ghi company crawl được.
 */
public record CompanyData(
        String name,
        String location,
        String businessScope,
        String memberType,
        String detailUrl,
        List<String> tags
) {
}
