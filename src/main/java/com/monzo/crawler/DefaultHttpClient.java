package com.monzo.crawler;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class DefaultHttpClient {
    private static final Logger logger = LogManager.getLogger(WebPageCrawler.class);

    private static final int MAX_RETRIES = 5;
    private Duration initial_backoff = Duration.ofSeconds(1);
    private static final long BACKOFF_MULTIPLIER = 2;

    private final HttpClient client;

    public DefaultHttpClient(HttpClient client, long backOffMillis) {
        this.client = client;
        this.initial_backoff = Duration.ofMillis(backOffMillis);
    }

    /**
     * Attempts to fetch the url and load the webpage. This method will retry on failures such as 500s, but
     * it could probably do a better job at checking specific status codes to determine if they should
     * be retried or not.
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public Optional<String> fetchWebPage(String url) throws MalformedURLException {
        int retry = 0;
        var backoff = initial_backoff;
        var httpRequest = getHttpRequest(url);
        var req = httpRequest.orElseThrow(() -> new MalformedURLException("Invalid url " + url));
        while (retry < MAX_RETRIES) {
            try {
                var response = getResp(req);
                if (response.statusCode() >= 200 && response.statusCode() <= 399) {
                    return Optional.of(response.body());
                }
                if (response.statusCode() >= 400 && response.statusCode() <= 499) {
                    return Optional.empty();
                }
                logger.info("Retrying count {} backoff {} since status code is non 2xx {} for url {}", retry, backoff, response.statusCode(), response.uri());
            } catch (IOException e) {
                logger.error("Retrying count {} backoff {} for url {}", retry, backoff, url, e);
            } catch (InterruptedException e) {
                logger.error("HTTP request interrupted.", e);
                break;
            }
            retry++;
            backoff = backoff.multipliedBy(BACKOFF_MULTIPLIER);
            sleep(backoff);
        }
        return Optional.empty();
    }

    private HttpResponse<String> getResp(HttpRequest request) throws IOException, InterruptedException {
        var response = client.send(request, ofString());
        return followRedirects(response);
    }

    /**
     * This method will return an HttpRequest object back based on the url passed in.
     * It attempts to urls encode the url since we may receive urls that need to be encoded, otherwise they
     * could fail. Unfortunately, java's built in methods are not so sophisticated, but the below
     * should work in most cases.
     * @param urlStr
     * @return
     */
    private static Optional<HttpRequest> getHttpRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            URI uri = new URI(url.getProtocol(),
                    url.getUserInfo(),
                    url.getHost(),
                    url.getPort(),
                    url.getPath(),
                    url.getQuery(),
                    url.getRef());
            return Optional.of(HttpRequest.newBuilder(URI.create(uri.toASCIIString()))
                    .timeout(Duration.ofMinutes(2))
                    .build());
        } catch (IllegalArgumentException | MalformedURLException | URISyntaxException e) {
            logger.error("Invalid URL {}", urlStr, e);
        }
        return Optional.empty();
    }

    private static void sleep(Duration backoff) {
        try {
            Thread.sleep(backoff.toMillis());
        } catch (InterruptedException interruptedEx) {
        }
    }

    private HttpResponse<String> followRedirects(HttpResponse<String> response) throws IOException, InterruptedException {
        if (response.statusCode() == 301 || response.statusCode() == 302) {
            var newUrl = response.headers().firstValue("Location").get();
            if (newUrl.contains("monzo.com")) {
                logger.info("Attempting to follow redirect from {} to {}", response.uri(), newUrl);
                var newRequest = getHttpRequest(newUrl);
                if (newRequest.isEmpty()) {
                    return response;
                }
                response = client.send(newRequest.get(), ofString());
                return followRedirects(response);
            }
        }
        return response;
    }

}
