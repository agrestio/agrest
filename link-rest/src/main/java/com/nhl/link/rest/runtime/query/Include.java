package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.13
 *
 * Represents Include query parameter
 */
public class Include {

    private String value;
    private CayenneExp cayenneExp;
    private Sort sort;
    private String mapBy;
    private String path;
    private Integer start;
    private Integer limit;
    private List<Include> includes = new ArrayList<>();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMapBy() {
        return mapBy;
    }

    public void setMapBy(String mapBy) {
        this.mapBy = mapBy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public CayenneExp getCayenneExp() {
        return cayenneExp;
    }

    public void setCayenneExp(CayenneExp cayenneExp) {
        this.cayenneExp = cayenneExp;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public List<Include> getIncludes() {
        return includes;
    }

    public void setIncludes(List<Include> includes) {
        this.includes = includes;
    }
}
