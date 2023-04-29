package com.monzo.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class Crawler {
    private static final Logger logger = LogManager.getLogger(Crawler.class);

    private final WebPageCrawler webPageCrawler;
    private final ExecutorFactory executorFactory;

    public Crawler(WebPageCrawler webPageCrawler, ExecutorFactory executorFactory) {
        this.webPageCrawler = webPageCrawler;
        this.executorFactory = executorFactory;
    }

    public List<URL> crawl(final URL url) throws ExecutionException, InterruptedException {
        logger.info("Crawling url {}", url);
        final var allUrls = new ArrayList<URL>();
        final var urlsToTraverse = new LinkedList<URL>();
        urlsToTraverse.add(url);
        final ExecutorService executor = executorFactory.getExecutor();
        try {
            while (true){
                var crawlers = new ArrayList<CompletableFuture<List<URL>>>();
                while(!urlsToTraverse.isEmpty()){
                    var urlToCrawl = urlsToTraverse.pop();
                    allUrls.add(urlToCrawl);
                    crawlers.add(webPageCrawler
                            .crawlWebPage(urlToCrawl, (u) ->{}, executor));
                }
                try {
                    var resultUrls = waitForCrawlers(crawlers);
                    urlsToTraverse.addAll(resultUrls);
                    if (urlsToTraverse.isEmpty()) {
                        break;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Interrupted while crawling {} ", url, e);
                    throw e;
                }
            }
        } finally {
            executor.shutdown();
        }
        return allUrls;
    }

    private static List<URL> waitForCrawlers(List<CompletableFuture<List<URL>>> crawlers)
            throws InterruptedException, ExecutionException {
        return CompletableFuture.allOf(crawlers.toArray(new CompletableFuture[0])).thenApply(
                        t -> joinCrawlers(crawlers)
                ).get()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static List<List<URL>> joinCrawlers(List<CompletableFuture<List<URL>>> crawlers) {
        return crawlers.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

}
