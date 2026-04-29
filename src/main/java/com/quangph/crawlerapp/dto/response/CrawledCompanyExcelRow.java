package com.quangph.crawlerapp.dto.response;

public record CrawledCompanyExcelRow(
        String companyName,
        String status,
        String country,
        String address,
        String email,
        String weChat,
        String whatsapp,
        String skype,
        String phone,
        String companySize,
        String note
) {}