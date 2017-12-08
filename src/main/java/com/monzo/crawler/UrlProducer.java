package com.monzo.crawler;

import io.reactivex.Observable;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class UrlProducer {

    private final OkHttpClient client;
    private final LinkedBlockingQueue<HttpUrl> urlBlockingQueue;
    private final ConcurrentHashMap<String, String> nodes;

    public UrlProducer(final OkHttpClient client, final LinkedBlockingQueue<HttpUrl> queue, HttpUrl originalUrl) {
        this.nodes = new ConcurrentHashMap<>();
        this.client = client;
        this.urlBlockingQueue = queue;
        urlBlockingQueue.add(originalUrl);
    }

    public Observable<HttpUrl> poll() {
        Observable<HttpUrl> urlObservable = Observable.just(HttpUrl.parse(""));
        for (HttpUrl url; (url = urlBlockingQueue.poll()) != null; ) {
            urlObservable = getAllUrls(url);
            break;
        }
        return urlObservable;
    }

    private Observable<HttpUrl> getAllUrls(HttpUrl url) {
        System.out.println("Loading " + url);
        return Observable.create(subscriber -> {
            try {
                Request request = request(url);
                Response response = client.newCall(request).execute();
                Document document = Jsoup.parse(response.body().string());
                processHrefsOnDoc(response, document);
                subscriber.onNext(url);
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
                urlBlockingQueue.add(link);
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
