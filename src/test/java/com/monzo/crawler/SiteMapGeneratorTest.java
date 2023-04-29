package com.monzo.crawler;

import com.monzo.crawler.domain.Node;
import com.monzo.crawler.sitemap.SiteMapGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SiteMapGeneratorTest {

    @Test
    public void testItPrintsEmptyWhenNull() {
        String siteMap = new SiteMapGenerator(null).getSiteMap();
        assertEquals("", siteMap);
    }

    @Test
    public void testItPrintsSingleListNodes() throws IOException {
        Node root = new Node("monzo", map());
        root.getChildren().put("item1", new Node("item1", map()));
        root.getChildren().put("item2", new Node("item2", map()));
        root.getChildren().put("item3", new Node("item3", map()));

        String siteMap = new SiteMapGenerator(root).getSiteMap();
        String expectedSiteMap = getExpectedSiteMap("multipleChildrenForRootNodeOnly.txt");
        assertEquals(expectedSiteMap, siteMap);
    }

    @Test
    public void testItHandlesOneNode() throws IOException {
        Node root = new Node("monzo", map());
        String siteMap = new SiteMapGenerator(root).getSiteMap();
        assertEquals("monzo\n", siteMap);
    }

    @Test
    public void testItPrintsWhenOnlyLastNodeHasChildren() throws IOException {
        Node root = new Node("monzo", map());
        Node ch1 = new Node("blog", map());
        Node ch2 = new Node("news", map());
        ch2.getChildren().put("item1", new Node("item1", map()));
        ch2.getChildren().put("item2", new Node("item2", map()));
        ch2.getChildren().put("item3", new Node("item3", map()));
        ch1.getChildren().put("news", ch2);
        root.getChildren().put("blog", ch1);

        String siteMap = new SiteMapGenerator(root).getSiteMap();
        String expectedSiteMap = getExpectedSiteMap("onlyLastNodeHasMultipleChildren.txt");
        assertEquals(expectedSiteMap, siteMap);
    }

    @Test
    public void testItPrintsRootNodesOnly() throws IOException {
        Node root = new Node("monzo", map());
        Node ch1 = new Node("blog", map());
        Node ch2 = new Node("news", map());

        ch1.getChildren().put("news", ch2);
        root.getChildren().put("blog", ch1);

        String siteMap = new SiteMapGenerator(root).getSiteMap();
        String expectedSiteMap = getExpectedSiteMap("rootNodesOnly.txt");
        assertEquals(expectedSiteMap, siteMap);
    }

    @Test
    public void testItPrintsMultipleLevelsDeep() throws Exception {
        Node root = new Node("monzo", map());
        Node ch1 = new Node("blog", map());
        Node ch2 = new Node("news", map());
        ch2.getChildren().put("item1", new Node("item1", map()));
        ch2.getChildren().put("item2", new Node("item2", map()));
        ch1.getChildren().put("news", ch2);
        root.getChildren().put("blog", ch1);
        root.getChildren().put("contacts", new Node("contacts", map()));

        String siteMap = new SiteMapGenerator(root).getSiteMap();

        String expectedSiteMap = getExpectedSiteMap("2nodesAtRootSiteMap.txt");
        assertEquals(expectedSiteMap, siteMap);
    }

    @Test
    public void testItPrintsComplexTree() throws Exception {
        Node root = new Node("monzo", map());
        Node ch1 = new Node("blog", map());
        Node ch2 = new Node("news", map());
        Node news2 = new Node("news2", map());
        Node item2 = new Node("item2", map());
        Node contacts = new Node("contacts",map());

        item2.getChildren().put("item2 sub node", new Node("item2 sub node", map()));

        news2.getChildren().put("sub item1", new Node("sub item1", map()));
        news2.getChildren().put("sub item2", new Node("sub item2", map()));

        ch2.getChildren().put("news2", news2);
        ch2.getChildren().put("item2", item2);

        ch1.getChildren().put("news", ch2);

        root.getChildren().put("blog", ch1);

        contacts.getChildren().put("sub contacts node" ,new Node("sub contacts node", map()));

        root.getChildren().put("contacts", contacts);

        String siteMap = new SiteMapGenerator(root).getSiteMap();

        String expectedSiteMap = getExpectedSiteMap("complexSiteMap.txt");
        assertEquals(expectedSiteMap, siteMap);
    }

    private String getExpectedSiteMap(String siteMap) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("sitemaps/" + siteMap).getFile());
        return new String(Files.readAllBytes(file.toPath()), "UTF8");
    }

    private LinkedHashMap<String, Node> map() {
        return new LinkedHashMap<>();
    }

}
