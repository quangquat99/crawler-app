package com.quangph.crawlerapp.service.site;

import com.quangph.crawlerapp.dto.response.CompanyData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JcTransCompanyParserTest {

    private final JcTransCompanyParser parser = new JcTransCompanyParser();

    @Test
    void shouldParseCompanyCardsFromHtml() {
        String html = """
                <div class="company-item">
                    <div class="company-name">ABC Logistics</div>
                    <div class="location">Vietnam</div>
                    <div class="business-scope">Air Freight</div>
                    <div class="member-type">VIP</div>
                    <a href="/company/abc">Detail</a>
                    <span class="tag">Sea</span>
                    <span class="tag">Air</span>
                </div>
                """;

        List<CompanyData> items = parser.parse(html, "https://www.jctrans.com/en/company/");

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().name()).isEqualTo("ABC Logistics");
        assertThat(items.getFirst().location()).isEqualTo("Vietnam");
        assertThat(items.getFirst().detailUrl()).isEqualTo("https://www.jctrans.com/company/abc");
    }
}
