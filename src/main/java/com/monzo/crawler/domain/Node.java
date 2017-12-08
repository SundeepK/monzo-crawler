package com.monzo.crawler.domain;

import java.util.Map;

public class Node {

    private String val;
    private Map<String, Node> children;

    public Node(String val, Map<String, Node> children) {
        this.val = val;
        this.children = children;
    }

    public Map<String, Node> getChildren() {
        return children;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "Node{" +
                "val='" + val + '\'' +
                ", children=" + children +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (val != null ? !val.equals(node.val) : node.val != null) return false;
        return children != null ? children.equals(node.children) : node.children == null;
    }

    @Override
    public int hashCode() {
        int result = val != null ? val.hashCode() : 0;
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }
}
