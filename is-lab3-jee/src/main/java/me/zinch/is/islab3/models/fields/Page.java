package me.zinch.is.islab3.models.fields;

import java.util.List;

public class Page<T> {
    private long total;
    private List<T> items;

    public Page(long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
