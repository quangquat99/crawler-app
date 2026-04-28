package com.quangph.crawlerapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class bind toan bo cau hinh prefix `crawler` tu file properties/yaml.
 */
@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {

    private final Demo demo = new Demo();
    private final Http http = new Http();
    private final Playwright playwright = new Playwright();
    private final Sites sites = new Sites();

    /**
     * Tra ve nhom cau hinh demo startup.
     *
     * @return cau hinh demo
     */
    public Demo getDemo() {
        return demo;
    }

    /**
     * Tra ve nhom cau hinh HTTP.
     *
     * @return cau hinh HTTP
     */
    public Http getHttp() {
        return http;
    }

    /**
     * Tra ve nhom cau hinh Playwright.
     *
     * @return cau hinh Playwright
     */
    public Playwright getPlaywright() {
        return playwright;
    }

    /**
     * Tra ve nhom cau hinh dac thu theo site.
     *
     * @return cau hinh site
     */
    public Sites getSites() {
        return sites;
    }

    /**
     * Cau hinh cho luong demo khi app vua khoi dong.
     */
    public static class Demo {
        private boolean enabled;
        private String url;

        /**
         * Cho biet co bat demo startup hay khong.
         *
         * @return true neu bat demo
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Gan trang thai bat/tat demo startup.
         *
         * @param enabled true neu muon bat demo
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Lay URL demo se duoc crawl khi startup.
         *
         * @return URL demo
         */
        public String getUrl() {
            return url;
        }

        /**
         * Gan URL demo crawl.
         *
         * @param url URL demo
         */
        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * Cau hinh timeout va gioi han log cho HTTP client.
     */
    public static class Http {
        private int connectTimeoutMs = 10_000;
        private int readTimeoutMs = 15_000;
        private int writeTimeoutMs = 15_000;
        private int maxBodyLogLength = 1_200;

        /**
         * Lay timeout ket noi HTTP.
         *
         * @return so mili giay timeout ket noi
         */
        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        /**
         * Gan timeout ket noi HTTP.
         *
         * @param connectTimeoutMs so mili giay timeout ket noi
         */
        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        /**
         * Lay timeout doc response HTTP.
         *
         * @return so mili giay timeout doc
         */
        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        /**
         * Gan timeout doc response HTTP.
         *
         * @param readTimeoutMs so mili giay timeout doc
         */
        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        /**
         * Lay timeout ghi request HTTP.
         *
         * @return so mili giay timeout ghi
         */
        public int getWriteTimeoutMs() {
            return writeTimeoutMs;
        }

        /**
         * Gan timeout ghi request HTTP.
         *
         * @param writeTimeoutMs so mili giay timeout ghi
         */
        public void setWriteTimeoutMs(int writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
        }

        /**
         * Lay do dai toi da cua body duoc log preview.
         *
         * @return so ky tu toi da
         */
        public int getMaxBodyLogLength() {
            return maxBodyLogLength;
        }

        /**
         * Gan do dai toi da cua body duoc log preview.
         *
         * @param maxBodyLogLength so ky tu toi da
         */
        public void setMaxBodyLogLength(int maxBodyLogLength) {
            this.maxBodyLogLength = maxBodyLogLength;
        }
    }

    /**
     * Cau hinh cho trinh duyet Playwright.
     */
    public static class Playwright {
        private boolean headless = true;
        private int timeoutMs = 30_000;

        /**
         * Cho biet Playwright co chay headless hay khong.
         *
         * @return true neu chay an giao dien
         */
        public boolean isHeadless() {
            return headless;
        }

        /**
         * Gan che do headless cho Playwright.
         *
         * @param headless true neu muon chay an giao dien
         */
        public void setHeadless(boolean headless) {
            this.headless = headless;
        }

        /**
         * Lay timeout mac dinh cho thao tac Playwright.
         *
         * @return so mili giay timeout
         */
        public int getTimeoutMs() {
            return timeoutMs;
        }

        /**
         * Gan timeout mac dinh cho thao tac Playwright.
         *
         * @param timeoutMs so mili giay timeout
         */
        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    /**
     * Cau hinh theo site de map page URL sang API URL khi can.
     */
    public static class Sites {
        private final JcTrans jcTrans = new JcTrans();

        /**
         * Lay cau hinh cho site JCTrans.
         *
         * @return cau hinh JCTrans
         */
        public JcTrans getJcTrans() {
            return jcTrans;
        }
    }

    /**
     * Cau hinh dac thu cho luong company cua JCTrans.
     */
    public static class JcTrans {
        private String companyApiUrl;

        /**
         * Lay API URL company list neu muon map tu page URL sang API URL.
         *
         * @return API URL hoac null
         */
        public String getCompanyApiUrl() {
            return companyApiUrl;
        }

        /**
         * Gan API URL company list.
         *
         * @param companyApiUrl API URL company list
         */
        public void setCompanyApiUrl(String companyApiUrl) {
            this.companyApiUrl = companyApiUrl;
        }
    }
}
