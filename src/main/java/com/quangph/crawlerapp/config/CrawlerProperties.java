package com.quangph.crawlerapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {

    private final Demo demo = new Demo();
    private final Http http = new Http();
    private final Playwright playwright = new Playwright();

    public Demo getDemo() {
        return demo;
    }

    public Http getHttp() {
        return http;
    }

    public Playwright getPlaywright() {
        return playwright;
    }

    public static class Demo {
        private boolean enabled;
        private String url;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Http {
        private int connectTimeoutMs = 10_000;
        private int readTimeoutMs = 15_000;
        private int writeTimeoutMs = 15_000;
        private int maxBodyLogLength = 1_200;

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public int getWriteTimeoutMs() {
            return writeTimeoutMs;
        }

        public void setWriteTimeoutMs(int writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
        }

        public int getMaxBodyLogLength() {
            return maxBodyLogLength;
        }

        public void setMaxBodyLogLength(int maxBodyLogLength) {
            this.maxBodyLogLength = maxBodyLogLength;
        }
    }

    public static class Playwright {
        private boolean headless = true;
        private int timeoutMs = 30_000;

        public boolean isHeadless() {
            return headless;
        }

        public void setHeadless(boolean headless) {
            this.headless = headless;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }
}
