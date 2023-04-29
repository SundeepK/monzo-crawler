package com.monzo.crawler.sitemap;

import com.monzo.crawler.domain.Node;

import java.net.URL;
import java.util.List;
import java.util.TreeMap;

public class SiteMap {

    private SiteMapGeneratorFactory siteMapGeneratorFactory;
    private Node root;

    public SiteMap(SiteMapGeneratorFactory siteMapGeneratorFactory, String monzo){
        this.siteMapGeneratorFactory = siteMapGeneratorFactory;
        this.root = new Node(monzo, new TreeMap<>());
    }

    public void addAll(List<URL> urls){
        urls.forEach(this::add);
    }

    public void add(URL url){
        String[] paths = url.getPath().split("/");
        Node r = root;
        for (String s : paths) {
            if(!s.isEmpty()){
                if(r.getChildren().containsKey(s)){
                    r = r.getChildren().get(s);
                } else {
                    Node n = new Node(s, new TreeMap<>());
                    r.getChildren().put(s, n);
                    r = n;
                }
            }
        }
    }

    public String createSiteMap() {
        return siteMapGeneratorFactory.createSiteMapGenerator(root).getSiteMap();
    }

}
