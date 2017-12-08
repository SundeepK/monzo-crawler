import com.monzo.crawler.Crawler;
import com.monzo.crawler.OnSiteCrawled;
import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapGeneratorFactory;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MonzoCrawlerMain {

    static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        Crawler c = new Crawler(client, 2, Executors.newFixedThreadPool(10), HttpUrl.parse("https://monzo.com/"));
        long time = System.currentTimeMillis();
        c.crawl()
                .subscribe(new OnSiteCrawled(new SiteMap(new SiteMapGeneratorFactory(), "monzo")));
        System.out.println("Timetaken " + TimeUnit.SECONDS.convert(System.currentTimeMillis() - time, TimeUnit.MILLISECONDS));
    }

}
