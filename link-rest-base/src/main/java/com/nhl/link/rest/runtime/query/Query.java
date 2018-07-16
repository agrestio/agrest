package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.13
 *
 * Holds all query parameters
 */
public class Query {

    private CayenneExp cayenneExp;
    private List<Include> include = new ArrayList<>();
    private List<Exclude> exclude = new ArrayList<>();
    private Sort sort;
    private MapBy mapBy;
    private Start start;
    private Limit limit;

    public CayenneExp getCayenneExp() {
        return cayenneExp;
    }

    public void setCayenneExp(CayenneExp cayenneExp) {
        this.cayenneExp = cayenneExp;
    }

    public List<Include> getInclude() {
        return include;
    }

    public void setInclude(List<Include> include) {
        this.include = include;
    }

    public List<Exclude> getExclude() {
        return exclude;
    }

    public void setExclude(List<Exclude> exclude) {
        this.exclude = exclude;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public MapBy getMapBy() {
        return mapBy;
    }

    public void setMapBy(MapBy mapBy) {
        this.mapBy = mapBy;
    }

    public Start getStart() {
        return start;
    }

    public void setStart(Start start) {
        this.start = start;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

}
