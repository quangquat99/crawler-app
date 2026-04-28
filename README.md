# Crawler App

Project crawl data theo flow:

1. Thu API truoc
2. Neu khong co API thi crawl HTML bang Jsoup
3. Neu trang render bang JS thi fallback sang Playwright Java

## API crawl

- Endpoint: `POST /api/v1/crawls`
- Body:

```json
{
  "url": "https://www.jctrans.com/en/company/"
}
```

## Cau truc chinh

- `controller`: REST API nhan request crawl
- `service`: orchestration va runner demo
- `service/strategy`: tung chien luoc crawl
- `service/site`: parser rieng cho tung site
- `entity`, `repository`: dat san de luu DB khi can

## Luu y

- Hien tai project moi log du lieu, chua save table
- JPA/PostgreSQL da co dependency va package structure
- Startup dang tam tat auto datasource de app chay ngay duoc
- Khi can luu DB, bat lai datasource trong `application.properties`
