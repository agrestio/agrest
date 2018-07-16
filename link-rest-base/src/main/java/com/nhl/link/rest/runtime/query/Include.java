package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.13
 *
 * Represents Include query parameter
 */
public class Include {
    private static final String INCLUDE = "include";

    private String value;
    private CayenneExp cayenneExp;
    private Sort sort;
    private MapBy mapBy;
    private String path;
    private Start start;
    private Limit limit;
    private List<Include> includes = new ArrayList<>();

    public Include(String value) {
        this.value = value;
    }

    public Include(List<Include> includes) {
        this.includes = includes;
    }

    public Include(CayenneExp cayenneExp, Sort sort, MapBy mapBy, String path, Start start, Limit limit) {
        this.cayenneExp = cayenneExp;
        this.sort = sort;
        this.mapBy = mapBy;
        this.path = path;
        this.start = start;
        this.limit = limit;
    }

    public static String getName() {
        return INCLUDE;
    }

    public String getValue() {
        return value;
    }

    public MapBy getMapBy() {
        return mapBy;
    }

    public String getPath() {
        return path;
    }

    public Start getStart() {
        return start;
    }

    public Limit getLimit() {
        return limit;
    }

    public CayenneExp getCayenneExp() {
        return cayenneExp;
    }

    public Sort getSort() {
        return sort;
    }

    public List<Include> getIncludes() {
        return includes;
    }
}
