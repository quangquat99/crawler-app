package com.quangph.crawlerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Lớp bootstrap chính của ứng dụng crawler.
 * Tạm thời tắt auto datasource/JPA để project có thể chạy ngay
 * trong giai đoạn mới log dữ liệu, chưa lưu vào database.
 */
@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
public class CrawlerAppApplication {

    /**
     * Điểm vào chính để khởi động Spring Boot application.
     *
     * @param args tham số dòng lệnh khi chạy ứng dụng
     */
    public static void main(String[] args) {
        SpringApplication.run(CrawlerAppApplication.class, args);
    }
}
