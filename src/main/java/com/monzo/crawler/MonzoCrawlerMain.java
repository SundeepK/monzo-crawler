package com.monzo.crawler;

import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapFileCreator;
import com.monzo.crawler.sitemap.SiteMapGeneratorFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutionException;


public class MonzoCrawlerMain {
    private static final Logger logger = LogManager.getLogger(MonzoCrawlerMain.class);

    public static void main(String[] args) throws MalformedURLException, ExecutionException, InterruptedException {
        var webPageCrawler = new WebPageCrawler(new DefaultHttpClient(HttpClient.newBuilder().build(), 1000), "monzo.com");
        final var crawler = new Crawler(webPageCrawler, new ExecutorFactory(90));
        var urls = crawler.crawl(new URL("https://monzo.com"));
        final var siteMap = new SiteMap(new SiteMapGeneratorFactory(), "monzo");
        siteMap.addAll(urls);
        var map = siteMap.createSiteMap();
        final var siteMapFileCreator = new SiteMapFileCreator();
        siteMapFileCreator.createSiteMapFile(map);
        logger.info("Finished crawling https://monzo.com, found total urls {}", urls.size());
    }

}
