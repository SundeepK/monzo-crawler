package com.monzo.crawler;

import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapGeneratorFactory;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MonzoCrawlerMain {

    static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        Crawler c = new Crawler(2, Executors.newFixedThreadPool(10), new UrlProducer(client, new LinkedBlockingQueue<>(), HttpUrl.parse("https://monzo.com/")));
        long time = System.currentTimeMillis();
        c.crawl()
                .subscribe(new OnSiteCrawled(new SiteMap(new SiteMapGeneratorFactory(), "monzo")));
        System.out.println("Timetaken " + TimeUnit.SECONDS.convert(System.currentTimeMillis() - time, TimeUnit.MILLISECONDS));
    }

}
