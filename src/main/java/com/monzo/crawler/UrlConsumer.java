package com.monzo.crawler;

import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapFileCreator;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.HttpUrl;

import java.util.concurrent.TimeUnit;


public class UrlConsumer implements Observer<HttpUrl> {

    private final SiteMap siteMap;
    private final SiteMapFileCreator siteMapFileCreator = new SiteMapFileCreator();
    private long time;

    public UrlConsumer(SiteMap siteMap){
        this.siteMap = siteMap;
    }

    @Override
    public void onSubscribe(Disposable d) {
        time = System.currentTimeMillis();
    }

    @Override
    public void onNext(HttpUrl httpUrl) {
        siteMap.add(httpUrl);
    }

    @Override
    public void onError(Throwable e) {
    }

    @Override
    public void onComplete() {
        String siteMap = this.siteMap.createSiteMap();
        siteMapFileCreator.createSiteMapFile(siteMap);
        System.out.println(siteMap);
        long timeInSecs = TimeUnit.SECONDS.convert(System.currentTimeMillis() - time, TimeUnit.MILLISECONDS);
        System.out.println("Time taken " + timeInSecs + "s");
    }
}
