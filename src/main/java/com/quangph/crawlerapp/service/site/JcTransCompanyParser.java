package com.quangph.crawlerapp.service.site;

import com.quangph.crawlerapp.dto.response.CompanyData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JcTransCompanyParser {

    public boolean supports(String url) {
        return url != null && url.startsWith("https://www.jctrans.com/en/company/");
    }

    public List<CompanyData> parse(String html, String baseUrl) {
        Document document = Jsoup.parse(html, baseUrl);
        List<CompanyData> companies = new ArrayList<>();

        // Trang Jctrans co 2 kieu card chinh:
        // 1) HTML/SSR card tu server
        // 2) Card sau khi JS render trong swiper / list
        Elements cards = document.select(
                ".company-item, .company-info-card, .company-content, .person-swiper-slide"
        );
        for (Element card : cards) {
            String name = firstNonBlank(
                    card.selectFirst(".company-name") != null ? card.selectFirst(".company-name").text() : null,
                    card.selectFirst(".member-swiper-company-name") != null ? card.selectFirst(".member-swiper-company-name").text() : null,
                    card.selectFirst("h3") != null ? card.selectFirst("h3").text() : null,
                    card.selectFirst("h2") != null ? card.selectFirst("h2").text() : null
            );

            if (name == null || name.isBlank()) {
                continue;
            }

            String location = text(card, ".location, .company-location, [class*=location], .desc-box");
            String businessScope = text(card, ".business-scope, .company-scope, [class*=scope]");
            String memberType = resolveMemberType(card);
            String detailUrl = resolveDetailUrl(card);

            List<String> tags = card.select(".tag, .label, .badge, [class*=tag]")
                    .eachText()
                    .stream()
                    .filter(tag -> !tag.isBlank())
                    .distinct()
                    .toList();

            companies.add(new CompanyData(name, location, businessScope, memberType, detailUrl, tags));
        }

        return companies;
    }

    private String text(Element root, String selector) {
        Element element = root.selectFirst(selector);
        return element == null ? null : element.text();
    }

    private String attr(Element root, String selector, String attributeName) {
        Element element = root.selectFirst(selector);
        return element == null ? null : element.attr(attributeName);
    }

    private String resolveDetailUrl(Element card) {
        if ("a".equalsIgnoreCase(card.tagName()) && card.hasAttr("href")) {
            return card.attr("abs:href");
        }
        return attr(card, "a[href]", "abs:href");
    }

    private String resolveMemberType(Element card) {
        String rawMemberType = text(card, ".member-type, .company-member");
        if (rawMemberType != null && !rawMemberType.isBlank()) {
            return rawMemberType;
        }

        Element vipElement = card.selectFirst("[class*=vip-code-]");
        if (vipElement == null) {
            return null;
        }

        for (String cssClass : vipElement.classNames()) {
            if (cssClass.matches("vip-code-\\d+")) {
                return cssClass;
            }
        }
        return null;
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }
}
