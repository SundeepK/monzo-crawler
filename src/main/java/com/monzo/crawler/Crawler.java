package com.monzo.crawler;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class Crawler {

    private final int timeout;
    private final ExecutorService executorService;
    private final UrlProducer urlProducer;
    private final LinkedBlockingQueue<Disposable> observables = new LinkedBlockingQueue<>();

    public Crawler(int timeoutSeconds, ExecutorService executorService, UrlProducer urlProducer){
        this.timeout = timeoutSeconds;
        this.executorService = executorService;
        this.urlProducer = urlProducer;
    }

    public Observable<HttpUrl> crawl() {
        return crawler();
    }

    private Observable<HttpUrl> crawler() {
        return Observable.create((ObservableEmitter<HttpUrl> subscriber) -> {
            try {
                poll(subscriber);
                while(!observables.poll().isDisposed()){
                    poll(subscriber);
                }

            } finally {
                executorService.shutdown();
                subscriber.onComplete();
            }
        });
    }

    private void poll(final ObservableEmitter<HttpUrl> subscriber) {
        urlProducer.poll()
                .subscribeOn(Schedulers.from(executorService))
                .subscribe(new Observer<HttpUrl>() {
                    Disposable d;
                    @Override
                    public void onSubscribe(Disposable d) {
                        this.d = d;
                        observables.add(d);
                    }

                    @Override
                    public void onNext(HttpUrl httpUrl) {
                        subscriber.onNext(httpUrl);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        System.out.println("Error occurred but continuing on anyway." + e);

                    }

                    @Override
                    public void onComplete() {
                        d.isDisposed();
                    }
                });
    }


}
