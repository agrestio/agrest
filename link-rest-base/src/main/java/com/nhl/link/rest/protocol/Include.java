package com.nhl.link.rest.protocol;

import java.util.Collections;
import java.util.List;

/**
 * Represents 'include' LinkRest protocol parameter.
 *
 * @since 2.13
 */
public class Include {
    public static final String INCLUDE = "include";

    private String value;
    private CayenneExp cayenneExp;
    private Sort sort;
    private MapBy mapBy;
    private String path;
    private Start start;
    private Limit limit;
    private List<Include> includes;

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
        return includes != null ? includes : Collections.emptyList();
    }
}
