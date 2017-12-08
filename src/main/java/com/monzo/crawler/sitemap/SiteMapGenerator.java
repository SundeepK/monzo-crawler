package com.monzo.crawler.sitemap;

import com.monzo.crawler.domain.Node;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SiteMapGenerator {

    private final String lineSep;
    private final StringBuilder siteMapSb;
    private final Node root;

    public SiteMapGenerator(Node root) {
        this.root = root;
        this.lineSep = System.getProperty("line.separator");
        this.siteMapSb = new StringBuilder();
    }

    public String getSiteMap(){
        if (root == null) {
            return "";
        }
        getSiteMap(root, false, new LinkedList<>());
        return siteMapSb.toString();
    }

    private void getSiteMap(Node root, boolean isLast, LinkedList<Boolean> subtrees) {
        addIndents(subtrees);
        addNodeSeparator(isLast, subtrees);
        addNodeName(root);

        Collection<Node> objects = root.getChildren().values();
        Iterator<Node> iterator = objects.iterator();
        int i = 0;
        while(iterator.hasNext()){
            if (i < objects.size() - 1) {
                subtrees.add(true);
            } else {
                subtrees.add(false);
            }
            Node n = iterator.next();
            getSiteMap(n, i + 1 == objects.size(), subtrees);
            subtrees.removeLast();
            i++;
        }
    }

    private void addNodeName(Node root) {
        siteMapSb.append(root.getVal())
                 .append(lineSep);
    }

    private void addNodeSeparator(boolean isLast, LinkedList<Boolean> subtrees) {
        if (subtrees.size() > 0) {
            siteMapSb.append(isLast ? "└── " : "├── ");
        }
    }

    private void addIndents(List<Boolean> subTrees) {
        for (int i = 0; i < subTrees.size() - 1; ++i) {
            if (subTrees.get(i)) {
                siteMapSb.append("│   ");
            } else {
                siteMapSb.append("    ");
            }
        }
    }

}
