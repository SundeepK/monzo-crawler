package com.monzo.crawler;

import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapFileCreator;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.HttpUrl;


public class OnSiteCrawled implements Observer<HttpUrl> {

    private final SiteMap siteMap;
    private final SiteMapFileCreator siteMapFileCreator = new SiteMapFileCreator();

    public OnSiteCrawled(SiteMap siteMap){
        this.siteMap = siteMap;
    }

    @Override
    public void onSubscribe(Disposable d) {
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
    }
}
