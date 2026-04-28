package com.quangph.crawlerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Class bootstrap chinh cua ung dung crawler.
 * Tam thoi tat auto datasource/JPA de project co the chay ngay
 * trong giai doan moi log du lieu, chua luu vao database.
 */
@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
public class CrawlerAppApplication {

    /**
     * Diem vao chinh de khoi dong Spring Boot application.
     *
     * @param args tham so dong lenh khi chay ung dung
     */
    public static void main(String[] args) {
        SpringApplication.run(CrawlerAppApplication.class, args);
    }
}
