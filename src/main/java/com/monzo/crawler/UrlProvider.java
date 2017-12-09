package com.monzo.crawler;

import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class UrlProvider {

    private final ObservableEmitter<HttpUrl> mainSubscriber;
    private final ExecutorService executorService;
    private final AtomicInteger pendingTasks = new AtomicInteger();
    private final WebPageCrawler webPageCrawler;

    public UrlProvider(final ObservableEmitter<HttpUrl> mainSubscriber, WebPageCrawler webPageCrawler,
                       final ExecutorService executorService) {
        this.mainSubscriber = mainSubscriber;
        this.webPageCrawler = webPageCrawler;
        this.executorService = executorService;
    }

    public void provideUrl(final HttpUrl url){
        System.out.println("Loading " + url);
        webPageCrawler.crawlWebPage(url, this::provideUrl)
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
                            mainSubscriber.onComplete();
                            executorService.shutdown();
                        }
                    }
                });
    }


}
