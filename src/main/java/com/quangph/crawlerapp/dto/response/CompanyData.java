package com.quangph.crawlerapp.dto.response;

import java.util.List;

public record CompanyData(
        String name,
        String location,
        String businessScope,
        String memberType,
        String detailUrl,
        List<String> tags
) {
}
