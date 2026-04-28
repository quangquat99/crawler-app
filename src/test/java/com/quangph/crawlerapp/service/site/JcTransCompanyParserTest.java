package com.quangph.crawlerapp.service.site;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quangph.crawlerapp.dto.response.CompanyData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cho parser JcTrans.
 */
class JcTransCompanyParserTest {

    private final JcTransCompanyParser parser = new JcTransCompanyParser(new ObjectMapper());

    /**
     * Kiem tra parser co doc duoc card company co ban tu HTML mau.
     */
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

    /**
     * Kiem tra parser co doc duoc JSON response dang data.records.
     */
    @Test
    void shouldParseCompaniesFromApiResponse() {
        String rawBody = """
                {
                  "msg": "succeed",
                  "code": 0,
                  "data": {
                    "records": [
                      {
                        "compName": "LESAM INTERNATIONAL GROUP S.r.l.",
                        "countryName": "Italy",
                        "cityName": "Rome",
                        "profile": "Lesam was established since 1995.",
                        "description": "Handling Goods With Care",
                        "advCodeList": ["FCL", "LCL", "Air Freight"],
                        "vipListVoList": [{"code": "JC Elite"}],
                        "compEncryptionId": "3159911",
                        "directoryShows": [
                          {"nameEn": "FCL"},
                          {"nameEn": "LCL"}
                        ]
                      }
                    ]
                  }
                }
                """;

        List<CompanyData> items = parser.parseApiResponse(rawBody);

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().name()).isEqualTo("LESAM INTERNATIONAL GROUP S.r.l.");
        assertThat(items.getFirst().location()).isEqualTo("Rome, Italy");
        assertThat(items.getFirst().memberType()).isEqualTo("JC Elite");
        assertThat(items.getFirst().detailUrl()).isEqualTo("https://www.jctrans.com/en/company/3159911");
        assertThat(items.getFirst().tags()).contains("FCL", "LCL", "Air Freight");
    }
}
