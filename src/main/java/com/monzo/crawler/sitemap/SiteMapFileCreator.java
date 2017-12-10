package com.monzo.crawler.sitemap;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SiteMapFileCreator {

    public void createSiteMapFile(String sitemap){
        try(PrintWriter out = new PrintWriter(System.getProperty("user.dir") + "/sitemap.txt")) {
            out.println(sitemap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
