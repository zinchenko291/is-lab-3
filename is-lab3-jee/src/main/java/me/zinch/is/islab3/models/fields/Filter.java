package me.zinch.is.islab3.models.fields;

public class Filter<T extends EntityField> {
    private T field;
    private String value;
    private SortDirection sortDirection;

    public Filter(T field, String value, SortDirection sortDirection) {
        this.field = field;
        this.value = value;
        this.sortDirection = sortDirection;
    }

    public T getField() {
        return field;
    }

    public void setField(T field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }
}
