package com.monzo.crawler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.concurrent.Executors;

public class Crawler {

    private final OkHttpClient client;
    private final int threads;
    private final HttpUrl originalUrl;

    public Crawler(OkHttpClient client, int threads, HttpUrl originalUrl){
        this.client = client;
        this.threads = threads;
        this.originalUrl = originalUrl;
    }

    public Observable<HttpUrl> produceUrls() {
        return crawl(originalUrl);
    }

    private Observable<HttpUrl> crawl(HttpUrl originalUrl) {
        return Observable.create((ObservableEmitter<HttpUrl> subscriber) -> {
            UrlProvider urlProvider = new UrlProvider(subscriber, new WebPageCrawler(client, originalUrl.host()), Executors.newFixedThreadPool(threads));
            urlProvider.provideUrl(originalUrl);
        });
    }

}
