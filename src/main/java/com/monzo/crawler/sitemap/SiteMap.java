package com.monzo.crawler.sitemap;

import com.monzo.crawler.domain.Node;
import okhttp3.HttpUrl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SiteMap  {

    private SiteMapGeneratorFactory siteMapGeneratorFactory;
    private Node root;

    public SiteMap(SiteMapGeneratorFactory siteMapGeneratorFactory, String monzo){
        this.siteMapGeneratorFactory = siteMapGeneratorFactory;
        this.root = new Node(monzo, new ConcurrentHashMap<>());
    }

    public void add(HttpUrl httpUrl){
        List<String> paths = httpUrl.pathSegments();
        Node r = root;
        for (String s : paths) {
            if(!s.isEmpty()){
                if(r.getChildren().containsKey(s)){
                    r = r.getChildren().get(s);
                } else {
                    Node n = new Node(s, new ConcurrentHashMap<>());
                    r.getChildren().put(s, n);
                    r = n;
                }
            }
        }
    }

    public synchronized String createSiteMap() {
        return siteMapGeneratorFactory.createSiteMapGenerator(root).getSiteMap();
    }

}
