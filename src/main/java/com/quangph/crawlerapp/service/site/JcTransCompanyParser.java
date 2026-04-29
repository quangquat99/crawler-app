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
 * Parser chuyển HTML của trang JcTrans thành danh sách CompanyData.
 */
@Component
public class JcTransCompanyParser {

    private final ObjectMapper objectMapper;

    public JcTransCompanyParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Kiểm tra parser này có xử lý được URL hiện tại hay không.
     *
     * @param url URL cần parser
     * @return true nếu URL thuộc domain/route JcTrans company
     */
    public boolean supports(String url) {
        return url != null && url.startsWith("https://www.jctrans.com/en/company/");
    }

    /**
     * Parse HTML thành danh sách công ty.
     *
     * @param html HTML nguồn đã lấy được
     * @param baseUrl URL gốc để resolve link tương đối
     * @return danh sách company crawl được
     */
    public List<CompanyData> parse(String html, String baseUrl) {
        Document document = Jsoup.parse(html, baseUrl);
        List<CompanyData> companies = new ArrayList<>();

        // Trang JcTrans có 2 kiểu card chính:
        // 1) HTML/SSR card từ server
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
     * Parse JSON API response dạng data.records thành danh sách company.
     *
     * @param rawBody raw JSON response
     * @return danh sách company crawl được
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
     * Tách raw records từ JSON API response để log và trả về trước khi map.
     *
     * @param rawBody raw JSON response
     * @return danh sách JSON record
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
     * Lấy text đầu tiên theo CSS selector từ một node gốc.
     *
     * @param root node gốc
     * @param selector CSS selector cần tìm
     * @return text của node tìm được, hoặc null
     */
    private String text(Element root, String selector) {
        Element element = root.selectFirst(selector);
        return element == null ? null : element.text();
    }

    /**
     * Lấy giá trị attribute của phần tử đầu tiên theo selector.
     *
     * @param root node gốc
     * @param selector CSS selector cần tìm
     * @param attributeName tên attribute cần lấy
     * @return giá trị attribute, hoặc null
     */
    private String attr(Element root, String selector, String attributeName) {
        Element element = root.selectFirst(selector);
        return element == null ? null : element.attr(attributeName);
    }

    /**
     * Resolve link chi tiết company từ card hiện tại.
     *
     * @param card node card company
     * @return URL chi tiết company
     */
    private String resolveDetailUrl(Element card) {
        if ("a".equalsIgnoreCase(card.tagName()) && card.hasAttr("href")) {
            return card.attr("abs:href");
        }
        return attr(card, "a[href]", "abs:href");
    }

    /**
     * Suy ra member type từ text hoặc CSS class trong card.
     *
     * @param card node card company
     * @return member type, hoặc null nếu không tìm thấy
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
     * Resolve member type từ JSON record.
     *
     * @param record JSON company record
     * @return member type, hoặc null
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
     * Resolve detail URL từ compEncryptionId hoặc uid.
     *
     * @param record JSON company record
     * @return URL chi tiết company
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
     * Gom tag từ các field list quen thuộc trong JSON.
     *
     * @param record JSON company record
     * @return danh sách tag duy nhất
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
     * Trả về giá trị đầu tiên không rỗng trong danh sách candidate.
     *
     * @param candidates danh sách giá trị cần kiểm tra
     * @return giá trị đầu tiên hợp lệ, hoặc null
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
     * Lấy text từ field JSON và đổi blank thành null.
     *
     * @param node node cần đọc
     * @param fieldName tên field
     * @return text hoặc null
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
     * Nối các giá trị không rỗng bằng separator.
     *
     * @param separator chuỗi ngăn cách
     * @param values các giá trị cần nối
     * @return chuỗi đã nối, hoặc null nếu rỗng
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
     * Chuyển array JSON thành chuỗi phân tách bằng dấu phẩy.
     *
     * @param array node array
     * @return chuỗi đã nối, hoặc null
     */
    private String joinArray(JsonNode array) {
        List<String> values = new ArrayList<>();
        appendArrayValues(values, array);
        return values.isEmpty() ? null : String.join(", ", values);
    }

    /**
     * Thêm các giá trị text không rỗng từ JSON array vào list đích.
     *
     * @param target list đích
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
     * Đổi blank string thành null.
     *
     * @param value giá trị đầu vào
     * @return null nếu blank, ngược lại trả về chuỗi trim
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
