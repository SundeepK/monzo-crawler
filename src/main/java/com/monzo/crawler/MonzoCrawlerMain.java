package com.monzo.crawler;

import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapGeneratorFactory;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;

public class MonzoCrawlerMain {

    public static void main(String[] args) throws IOException {
        final Crawler crawler = new Crawler(new OkHttpClient(), 20, HttpUrl.parse("https://monzo.com/"));
        final SiteMap siteMap = new SiteMap(new SiteMapGeneratorFactory(), "monzo");
        final UrlConsumer urlConsumer = new UrlConsumer(siteMap);
        crawler.produceUrls()
                .subscribe(urlConsumer);

    }

}
