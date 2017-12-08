package com.monzo.crawler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Crawler {

    private final ConcurrentHashMap<String, String> nodes;
    private final OkHttpClient client;
    private final LinkedBlockingQueue<HttpUrl> queue;
    private final int timeout;
    private final ExecutorService executorService;

    public Crawler(OkHttpClient client, int timeoutSeconds, ExecutorService executorService, HttpUrl originalUrl){
        this.client = client;
        this.timeout = timeoutSeconds;
        this.nodes = new ConcurrentHashMap<>();
        this.executorService = executorService;
        this.queue = new LinkedBlockingQueue<>();
        queue.add(originalUrl);
    }

    public Observable<HttpUrl> crawl() {
        return crawler();
    }

    private Observable<HttpUrl> crawler() {
        return Observable.create((ObservableEmitter<HttpUrl> subscriber) -> {
            try {
                for (HttpUrl url; (url = queue.poll(timeout, TimeUnit.SECONDS)) != null; ) {
                    fetch(subscriber, url);
                }
            } catch (InterruptedException e) {
                System.out.println("No more items in the last " + timeout + " seconds.");
            } finally {
                executorService.shutdown();
                subscriber.onComplete();
            }
        });
    }

    private void fetch(ObservableEmitter<? super HttpUrl> subscriber, HttpUrl url) {
        System.out.println("Loading " + url);
        getAllUrls(url)
                .subscribeOn(Schedulers.from(executorService))
                .subscribe(new Observer<HttpUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(HttpUrl httpUrl) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("Error occurred but continuing on anyway." + e);

                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(url);
                    }
                })
        ;
    }

    private Observable<HttpUrl> getAllUrls(HttpUrl url) {
        return Observable.create(subscriber -> {
            try {
                Request request = request(url);
                Response response = client.newCall(request).execute();
                Document document = Jsoup.parse(response.body().string());
                processHrefsOnDoc(response, document);
                subscriber.onComplete();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    private void processHrefsOnDoc(Response response, Document document) {
        for (Element element : document.select("a[href]")) {
            String href = element.attr("href");
            HttpUrl link = response.request().url().resolve(href);
            if (link != null && link.host().startsWith("monzo.com") && !nodes.containsKey(link.toString())) {
                System.out.println("found " + link + " thread " + Thread.currentThread().getName());
                queue.add(link);
                nodes.put(link.toString(), "");
            }
        }
    }

    private Request request(HttpUrl httpUrl) throws IOException {
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

}
