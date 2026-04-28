package com.quangph.crawlerapp.service.site;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quangph.crawlerapp.dto.response.CompanyData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Parser chuyen HTML cua trang JcTrans thanh danh sach CompanyData.
 */
@Component
public class JcTransCompanyParser {

    private final ObjectMapper objectMapper;

    public JcTransCompanyParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Kiem tra parser nay co xu ly duoc URL hien tai hay khong.
     *
     * @param url URL can parser
     * @return true neu URL thuoc domain/route JcTrans company
     */
    public boolean supports(String url) {
        return url != null && url.startsWith("https://www.jctrans.com/en/company/");
    }

    /**
     * Parse HTML thanh danh sach cong ty.
     *
     * @param html HTML nguon da lay duoc
     * @param baseUrl URL goc de resolve link tuong doi
     * @return danh sach company crawl duoc
     */
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

    /**
     * Parse JSON API response dang data.records thanh danh sach company.
     *
     * @param rawBody raw JSON response
     * @return danh sach company crawl duoc
     */
    public List<CompanyData> parseApiResponse(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode records = root.path("data").path("records");
            if (!records.isArray() || records.isEmpty()) {
                return List.of();
            }

            List<CompanyData> companies = new ArrayList<>();
            for (JsonNode record : records) {
                String companyName = text(record, "compName");
                if (companyName == null || companyName.isBlank()) {
                    continue;
                }

                String location = joinNonBlank(", ",
                        text(record, "cityName"),
                        text(record, "countryName")
                );
                String businessScope = joinNonBlank(" | ",
                        joinArray(record.path("advCodeList")),
                        text(record, "description"),
                        text(record, "profile")
                );
                String memberType = resolveMemberType(record);
                String detailUrl = resolveDetailUrl(record);
                List<String> tags = collectTags(record);

                companies.add(new CompanyData(
                        companyName.trim(),
                        blankToNull(location),
                        blankToNull(businessScope),
                        blankToNull(memberType),
                        blankToNull(detailUrl),
                        tags
                ));
            }

            return companies;
        } catch (Exception exception) {
            return List.of();
        }
    }

    /**
     * Tach raw records tu JSON API response de log va tra ve truoc khi map.
     *
     * @param rawBody raw JSON response
     * @return danh sach JSON record
     */
    public List<JsonNode> extractApiRecords(String rawBody) {
        try {
            JsonNode records = objectMapper.readTree(rawBody).path("data").path("records");
            if (!records.isArray() || records.isEmpty()) {
                return List.of();
            }

            List<JsonNode> items = new ArrayList<>();
            records.forEach(items::add);
            return items;
        } catch (Exception exception) {
            return List.of();
        }
    }

    /**
     * Lay text dau tien theo CSS selector tu mot node goc.
     *
     * @param root node goc
     * @param selector CSS selector can tim
     * @return text cua node tim duoc, hoac null
     */
    private String text(Element root, String selector) {
        Element element = root.selectFirst(selector);
        return element == null ? null : element.text();
    }

    /**
     * Lay gia tri attribute cua phan tu dau tien theo selector.
     *
     * @param root node goc
     * @param selector CSS selector can tim
     * @param attributeName ten attribute can lay
     * @return gia tri attribute, hoac null
     */
    private String attr(Element root, String selector, String attributeName) {
        Element element = root.selectFirst(selector);
        return element == null ? null : element.attr(attributeName);
    }

    /**
     * Resolve link chi tiet company tu card hien tai.
     *
     * @param card node card company
     * @return URL chi tiet company
     */
    private String resolveDetailUrl(Element card) {
        if ("a".equalsIgnoreCase(card.tagName()) && card.hasAttr("href")) {
            return card.attr("abs:href");
        }
        return attr(card, "a[href]", "abs:href");
    }

    /**
     * Suy ra member type tu text hoac CSS class trong card.
     *
     * @param card node card company
     * @return member type, hoac null neu khong tim thay
     */
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

    /**
     * Resolve member type tu JSON record.
     *
     * @param record JSON company record
     * @return member type, hoac null
     */
    private String resolveMemberType(JsonNode record) {
        JsonNode vipList = record.path("vipListVoList");
        if (vipList.isArray() && !vipList.isEmpty()) {
            String vipCode = text(vipList.get(0), "code");
            if (vipCode != null && !vipCode.isBlank()) {
                return vipCode;
            }
        }
        return text(record, "settleStatus");
    }

    /**
     * Resolve detail URL tu compEncryptionId hoac uid.
     *
     * @param record JSON company record
     * @return URL chi tiet company
     */
    private String resolveDetailUrl(JsonNode record) {
        String encryptedId = text(record, "compEncryptionId");
        if (encryptedId != null && !encryptedId.isBlank()) {
            return "https://www.jctrans.com/en/company/" + encryptedId;
        }

        String uid = text(record, "uid");
        if (uid != null && !uid.isBlank()) {
            return "https://www.jctrans.com/en/company/" + uid;
        }

        return null;
    }

    /**
     * Gom tag tu cac field list quen thuoc trong JSON.
     *
     * @param record JSON company record
     * @return danh sach tag duy nhat
     */
    private List<String> collectTags(JsonNode record) {
        List<String> tags = new ArrayList<>();
        appendArrayValues(tags, record.path("advCodeList"));

        JsonNode directoryShows = record.path("directoryShows");
        if (directoryShows.isArray()) {
            for (JsonNode node : directoryShows) {
                String name = firstNonBlank(text(node, "nameEn"), text(node, "relatedInfo"));
                if (name != null && !tags.contains(name)) {
                    tags.add(name);
                }
            }
        }

        return tags;
    }

    /**
     * Tra ve gia tri dau tien khong rong trong danh sach candidate.
     *
     * @param candidates danh sach gia tri can kiem tra
     * @return gia tri dau tien hop le, hoac null
     */
    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }

    /**
     * Lay text tu field JSON va doi blank thanh null.
     *
     * @param node node can doc
     * @param fieldName ten field
     * @return text hoac null
     */
    private String text(JsonNode node, String fieldName) {
        JsonNode child = node.path(fieldName);
        if (child.isMissingNode() || child.isNull()) {
            return null;
        }
        String value = child.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * Noi cac gia tri khong rong bang separator.
     *
     * @param separator chuoi ngan cach
     * @param values cac gia tri can noi
     * @return chuoi da noi, hoac null neu rong
     */
    private String joinNonBlank(String separator, String... values) {
        StringJoiner joiner = new StringJoiner(separator);
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                joiner.add(value.trim());
            }
        }
        String joined = joiner.toString();
        return joined.isBlank() ? null : joined;
    }

    /**
     * Chuyen array JSON thanh chuoi phan tach bang dau phay.
     *
     * @param array node array
     * @return chuoi da noi, hoac null
     */
    private String joinArray(JsonNode array) {
        List<String> values = new ArrayList<>();
        appendArrayValues(values, array);
        return values.isEmpty() ? null : String.join(", ", values);
    }

    /**
     * Append cac gia tri text khong rong tu JSON array vao list dich.
     *
     * @param target list dich
     * @param array node array
     */
    private void appendArrayValues(List<String> target, JsonNode array) {
        if (!array.isArray()) {
            return;
        }

        for (JsonNode item : array) {
            String value = item.isValueNode() ? item.asText() : null;
            if (value != null && !value.isBlank() && !target.contains(value.trim())) {
                target.add(value.trim());
            }
        }
    }

    /**
     * Doi blank string thanh null.
     *
     * @param value gia tri dau vao
     * @return null neu blank, nguoc lai tra ve chuoi trim
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
