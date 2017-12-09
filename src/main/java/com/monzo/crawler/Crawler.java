package com.monzo.crawler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.concurrent.ExecutorService;

public class Crawler {

    private final OkHttpClient client;
    private final ExecutorService executorService;
    private final HttpUrl originalUrl;

    public Crawler(OkHttpClient client, ExecutorService executorService, HttpUrl originalUrl){
        this.client = client;
        this.executorService = executorService;
        this.originalUrl = originalUrl;
    }

    public Observable<HttpUrl> produceUrls() {
        return crawl(originalUrl);
    }

    private Observable<HttpUrl> crawl(HttpUrl originalUrl) {
        return Observable.create((ObservableEmitter<HttpUrl> subscriber) -> {
            UrlProvider urlProvider = new UrlProvider(subscriber, client, executorService);
            urlProvider.provideurls(originalUrl);
        });
    }

}
