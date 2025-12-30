package com.cm.keyMatrix.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the encrypted index of all credential entries.
 * Only lightweight metadata is stored here (id, site/app name, tag, updated timestamp).
 */
public class Index {

    public static class IndexItem {
        public String id;
        public String name;
        public String tag;
        public String updated;

        // Default constructor for Jackson
        public IndexItem() {}

        public IndexItem(String id, String name, String tag, String updated) {
            this.id = id;
            this.name = name;
            this.tag = tag;
            this.updated = updated;
        }
    }

    private List<IndexItem> items = new ArrayList<>();

    public Index() {}

    public List<IndexItem> getItems() {
        return items;
    }

    public void setItems(List<IndexItem> items) {
        this.items = items;
    }
}
