package com.monzo.crawler;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class WebPageCrawler {
    private static final Logger logger = LogManager.getLogger(WebPageCrawler.class);

    private final DefaultHttpClient client;
    private final ConcurrentHashMap<String, String> nodes = new ConcurrentHashMap<>();
    private final String allowedDomain;

    public WebPageCrawler(DefaultHttpClient client, String allowedDomain) {
        this.client = client;
        this.allowedDomain = allowedDomain;
    }

    public CompletableFuture<List<URL>> crawlWebPage(URL url, Consumer<URL> consumer, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Submitting job {} ", url);
            var body = fetch(url);
            if (body.isPresent()) {
                try {
                    var document = Jsoup.parse(body.toString());
                    return processHrefsOnDoc(url, document, consumer);
                } catch (Exception e) {
                    logger.error("Unable to parse doc for {} ", url, e);
                }
            }
            return new ArrayList<>();
        }, executor);
    }

    private Optional<String> fetch(URL url) {
        try {
            return client.fetchWebPage(url.toString());
        } catch (MalformedURLException e) {
            logger.error("Invalid URL when fetching {} ", url);
            return Optional.empty();
        }
    }

    private List<URL> processHrefsOnDoc(URL url, Document document, Consumer<URL> consumer) {
        var urls = new ArrayList<URL>();
        var elements = document.select("a[href]");
        elements.removeIf(link -> !link.tagName().equals("a") ||
                link.attr("href").contains("#") ||
                link.attr("href").contains("mailto"));
        for (Element element : elements) {
            var optionalUrl = parseUrl(url, element);
            if (optionalUrl.isPresent() &&
                    optionalUrl.get().getHost().startsWith(allowedDomain) &&
                    !nodes.containsKey(optionalUrl.get().toString())) {
                var link = optionalUrl.get();
                logger.info("Found url {} to process", link);
                consumer.accept(link);
                nodes.put(link.toString(), "");
                urls.add(link);
            }
        }
        logger.debug("Returning parsed urls from doc {}", urls.size());
        return urls;
    }

    private Optional<URL> parseUrl(URL base, Element element){
        var href = element.attr("href");
        try {
            if (href.startsWith("/")){
                return Optional.of(new URL(base, href));
            }
            if (!href.startsWith("http")) {
                return Optional.empty();
            }
            return Optional.of(new URL(href));
        } catch (MalformedURLException e) {
            logger.error("Malformed url for {}", href, e);
            return Optional.empty();
        }
    }

}
