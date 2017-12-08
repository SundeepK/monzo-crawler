package com.monzo.crawler;

import com.monzo.crawler.domain.Node;
import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapGenerator;
import com.monzo.crawler.sitemap.SiteMapGeneratorFactory;
import okhttp3.HttpUrl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SiteMapTest {

    @Mock
    private SiteMapGeneratorFactory siteMapGeneratorFactory;

    @Mock
    private SiteMapGenerator siteMapGenerator;

    private SiteMap underTest;

    @Before
    public void setUp(){
        when(siteMapGeneratorFactory.createSiteMapGenerator(any(Node.class))).thenReturn(siteMapGenerator);
        underTest = new SiteMap(siteMapGeneratorFactory, "monzo");
    }

    @Test
    public void testItHandlesRoot() throws Exception {
        HttpUrl url1 = HttpUrl.parse("https://monzo.com/");

        underTest.add(url1);
        Node expectedRoot = new Node("monzo", map());

        underTest.createSiteMap();
        verify(siteMapGeneratorFactory).createSiteMapGenerator(expectedRoot);
    }

    @Test
    public void testItCreatesSiteMap() throws Exception {
        HttpUrl url1 = HttpUrl.parse("https://monzo.com/about/");
        HttpUrl url2 = HttpUrl.parse("https://monzo.com/contact/");
        HttpUrl url3 = HttpUrl.parse("https://monzo.com/blog/2017/12/07/pots-android/");
        HttpUrl url4 = HttpUrl.parse("https://monzo.com/contact/subcontact/page1");
        HttpUrl url5 = HttpUrl.parse("https://monzo.com/contact/subcontact/page2");

        underTest.add(url1);
        underTest.add(url2);
        underTest.add(url3);
        underTest.add(url4);
        underTest.add(url5);

        Node expectedRoot = new Node("monzo", map());
        Node about = new Node("about", map());
        Node contact = new Node("contact", map());
        Node subcontact = new Node("subcontact", map());
        Node page1 = new Node("page1", map());
        Node page2 = new Node("page2", map());
        Node blog = new Node("blog", map());
        Node _2017 = new Node("2017", map());
        Node _12 = new Node("12", map());
        Node _07 = new Node("07", map());
        Node potsAndroid = new Node("pots-android", map());

        // blog
        _2017.getChildren().put("12", _12);
        _12.getChildren().put("07", _07);
        _07.getChildren().put("pots-android", potsAndroid);
        blog.getChildren().put("2017", _2017);

        // contact
        subcontact.getChildren().put("page1", page1);
        subcontact.getChildren().put("page2", page2);
        contact.getChildren().put("subcontact", subcontact);

        // main root
        expectedRoot.getChildren().put("about", about);
        expectedRoot.getChildren().put("contact", contact);
        expectedRoot.getChildren().put("blog", blog);

        underTest.createSiteMap();
        verify(siteMapGeneratorFactory).createSiteMapGenerator(expectedRoot);

    }

    private ConcurrentHashMap<String, Node> map() {
        return new ConcurrentHashMap<>();
    }


}