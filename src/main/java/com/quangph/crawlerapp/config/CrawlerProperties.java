package com.quangph.crawlerapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Lớp bind toàn bộ cấu hình prefix `crawler` từ file properties/yaml.
 */
@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {

    private final Demo demo = new Demo();
    private final Http http = new Http();
    private final Playwright playwright = new Playwright();
    private final Sites sites = new Sites();

    /**
     * Trả về nhóm cấu hình demo startup.
     *
     * @return cấu hình demo
     */
    public Demo getDemo() {
        return demo;
    }

    /**
     * Trả về nhóm cấu hình HTTP.
     *
     * @return cấu hình HTTP
     */
    public Http getHttp() {
        return http;
    }

    /**
     * Trả về nhóm cấu hình Playwright.
     *
     * @return cấu hình Playwright
     */
    public Playwright getPlaywright() {
        return playwright;
    }

    /**
     * Trả về nhóm cấu hình đặc thù theo site.
     *
     * @return cấu hình site
     */
    public Sites getSites() {
        return sites;
    }

    /**
     * Cấu hình cho luồng demo khi app vừa khởi động.
     */
    public static class Demo {
        private boolean enabled;
        private String url;

        /**
         * Cho biết có bật demo startup hay không.
         *
         * @return true nếu bật demo
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Gán trạng thái bật/tắt demo startup.
         *
         * @param enabled true nếu muốn bật demo
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Lấy URL demo sẽ được crawl khi startup.
         *
         * @return URL demo
         */
        public String getUrl() {
            return url;
        }

        /**
         * Gán URL demo crawl.
         *
         * @param url URL demo
         */
        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * Cấu hình timeout và giới hạn log cho HTTP client.
     */
    public static class Http {
        private int connectTimeoutMs = 10_000;
        private int readTimeoutMs = 15_000;
        private int writeTimeoutMs = 15_000;
        private int maxBodyLogLength = 1_200;

        /**
         * Lấy timeout kết nối HTTP.
         *
         * @return số mili giây timeout kết nối
         */
        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        /**
         * Gán timeout kết nối HTTP.
         *
         * @param connectTimeoutMs số mili giây timeout kết nối
         */
        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        /**
         * Lấy timeout đọc response HTTP.
         *
         * @return số mili giây timeout đọc
         */
        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        /**
         * Gán timeout đọc response HTTP.
         *
         * @param readTimeoutMs số mili giây timeout đọc
         */
        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        /**
         * Lấy timeout ghi request HTTP.
         *
         * @return số mili giây timeout ghi
         */
        public int getWriteTimeoutMs() {
            return writeTimeoutMs;
        }

        /**
         * Gán timeout ghi request HTTP.
         *
         * @param writeTimeoutMs số mili giây timeout ghi
         */
        public void setWriteTimeoutMs(int writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
        }

        /**
         * Lấy độ dài tối đa của body được log preview.
         *
         * @return số ký tự tối đa
         */
        public int getMaxBodyLogLength() {
            return maxBodyLogLength;
        }

        /**
         * Gán độ dài tối đa của body được log preview.
         *
         * @param maxBodyLogLength số ký tự tối đa
         */
        public void setMaxBodyLogLength(int maxBodyLogLength) {
            this.maxBodyLogLength = maxBodyLogLength;
        }
    }

    /**
     * Cấu hình cho trình duyệt Playwright.
     */
    public static class Playwright {
        private boolean headless = true;
        private int timeoutMs = 30_000;

        /**
         * Cho biết Playwright có chạy headless hay không.
         *
         * @return true nếu chạy ẩn giao diện
         */
        public boolean isHeadless() {
            return headless;
        }

        /**
         * Gán chế độ headless cho Playwright.
         *
         * @param headless true nếu muốn chạy ẩn giao diện
         */
        public void setHeadless(boolean headless) {
            this.headless = headless;
        }

        /**
         * Lấy timeout mặc định cho thao tác Playwright.
         *
         * @return số mili giây timeout
         */
        public int getTimeoutMs() {
            return timeoutMs;
        }

        /**
         * Gán timeout mặc định cho thao tác Playwright.
         *
         * @param timeoutMs số mili giây timeout
         */
        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    /**
     * Cấu hình theo site để map page URL sang API URL khi cần.
     */
    public static class Sites {
        private final JcTrans jcTrans = new JcTrans();

        /**
         * Lấy cấu hình cho site JCTrans.
         *
         * @return cấu hình JCTrans
         */
        public JcTrans getJcTrans() {
            return jcTrans;
        }
    }

    /**
     * Cấu hình đặc thù cho luồng company của JCTrans.
     */
    public static class JcTrans {
        private String companyApiUrl;

        /**
         * Lấy API URL company list nếu muốn map từ page URL sang API URL.
         *
         * @return API URL hoặc null
         */
        public String getCompanyApiUrl() {
            return companyApiUrl;
        }

        /**
         * Gán API URL company list.
         *
         * @param companyApiUrl API URL company list
         */
        public void setCompanyApiUrl(String companyApiUrl) {
            this.companyApiUrl = companyApiUrl;
        }
    }
}
