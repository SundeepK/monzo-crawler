package com.monzo.crawler;

import com.monzo.crawler.domain.Node;
import com.monzo.crawler.sitemap.SiteMap;
import com.monzo.crawler.sitemap.SiteMapGenerator;
import com.monzo.crawler.sitemap.SiteMapGeneratorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SiteMapTest {

    @Mock
    private SiteMapGeneratorFactory siteMapGeneratorFactory;

    @Mock
    private SiteMapGenerator siteMapGenerator;

    private SiteMap underTest;

    @BeforeEach
    public void setUp(){
        when(siteMapGeneratorFactory.createSiteMapGenerator(any(Node.class))).thenReturn(siteMapGenerator);
        underTest = new SiteMap(siteMapGeneratorFactory, "monzo");
    }

    @Test
    public void testItHandlesRoot() throws Exception {
        URL url1 = new URL("https://monzo.com/");

        underTest.add(url1);
        Node expectedRoot = new Node("monzo", map());

        underTest.createSiteMap();
        verify(siteMapGeneratorFactory).createSiteMapGenerator(expectedRoot);
    }

    @Test
    public void testItCreatesSiteMap() throws Exception {
        URL url1 = new URL("https://monzo.com/about/");
        URL url2 = new URL("https://monzo.com/contact/");
        URL url3 = new URL("https://monzo.com/blog/2017/12/07/pots-android/");
        URL url4 = new URL("https://monzo.com/contact/subcontact/page1");
        URL url5 = new URL("https://monzo.com/contact/subcontact/page2");

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