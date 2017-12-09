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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class UrlProvider {

    private final ObservableEmitter<HttpUrl> mainSubscriber;
    private final OkHttpClient client;
    private final ConcurrentHashMap<String, String> nodes = new ConcurrentHashMap<>();;
    private final ExecutorService executorService;
    private final AtomicInteger pendingTasks = new AtomicInteger();

    public UrlProvider(final ObservableEmitter<HttpUrl> mainSubscriber, final OkHttpClient client,
                       final ExecutorService executorService) {
        this.mainSubscriber = mainSubscriber;
        this.client = client;
        this.executorService = executorService;
    }

    public void provideurls(final HttpUrl url){
        System.out.println("Loading " + url);
        getAllUrls(url, this::provideurls)
                .subscribeOn(Schedulers.from(executorService))
                .subscribe(new Observer<HttpUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        pendingTasks.incrementAndGet();
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
                        mainSubscriber.onNext(url);
                        if(pendingTasks.decrementAndGet() == 0){
                            System.out.println("Complete called!!");
                            mainSubscriber.onComplete();
                            executorService.shutdown();
                        }
                    }
                });
    }

    private Observable<HttpUrl> getAllUrls(HttpUrl url, Consumer<HttpUrl> consumer) {
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
            if (link != null && link.host().startsWith("monzo.com") && !nodes.containsKey(link.toString())) {
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
