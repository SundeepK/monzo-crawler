package com.monzo.crawler.sitemap;

import com.monzo.crawler.domain.Node;

public class SiteMapGeneratorFactory {

    public SiteMapGenerator createSiteMapGenerator(Node node){
        return new SiteMapGenerator(node);
    }

}
