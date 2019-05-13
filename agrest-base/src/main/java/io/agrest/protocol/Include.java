package io.agrest.protocol;

import java.util.Collections;
import java.util.List;

/**
 * Represents 'include' Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Include {

    private String value;
    private CayenneExp cayenneExp;
    private Sort sort;
    private String mapBy;
    private String path;
    private Integer start;
    private Integer limit;
    private List<Include> includes;

    public Include(String value) {
        this.value = value;
    }

    public Include(List<Include> includes) {
        this.includes = includes;
    }

    public Include(
            CayenneExp cayenneExp,
            Sort sort,
            String mapBy,
            String path,
            Integer start,
            Integer limit,
            List<Include> includes) {

        this.cayenneExp = cayenneExp;
        this.sort = sort;
        this.mapBy = mapBy;
        this.path = path;
        this.start = start;
        this.limit = limit;
        this.includes = includes;
    }

    public String getValue() {
        return value;
    }

    public String getMapBy() {
        return mapBy;
    }

    public String getPath() {
        return path;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getLimit() {
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
