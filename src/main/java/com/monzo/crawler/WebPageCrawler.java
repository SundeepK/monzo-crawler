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
import java.util.function.Consumer;

public class WebPageCrawler {

    private final OkHttpClient client;
    private final ConcurrentHashMap<String, String> nodes = new ConcurrentHashMap<>();
    private final String allowedDomain;

    public WebPageCrawler(OkHttpClient client, String allowedDomain) {
        this.client = client;
        this.allowedDomain = allowedDomain;
    }

    public Observable<HttpUrl> crawlWebPage(HttpUrl url, Consumer<HttpUrl> consumer) {
        return Observable.create(subscriber -> {
            try {
                Request request = request(url);
                Response response = client.newCall(request).execute();
                Document document = Jsoup.parse(response.body().string());
                processHrefsOnDoc(response, document, consumer);
                subscriber.onComplete();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    private void processHrefsOnDoc(Response response, Document document, Consumer<HttpUrl> consumer) {
        for (Element element : document.select("a[href]")) {
            String href = element.attr("href");
            HttpUrl link = response.request().url().resolve(href);
            if (link != null && link.host().startsWith(allowedDomain) && !nodes.containsKey(link.toString())) {
                System.out.println("found " + link + " thread " + Thread.currentThread().getName());
                consumer.accept(link);
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
