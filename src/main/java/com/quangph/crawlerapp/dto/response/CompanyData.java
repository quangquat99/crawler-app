package com.quangph.crawlerapp.dto.response;

import java.util.List;

/**
 * DTO dai dien cho mot ban ghi company crawl duoc.
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
