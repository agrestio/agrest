package io.agrest.protocol;

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

    public Include(String value) {
        this.value = value;
    }

    public Include(
            CayenneExp cayenneExp,
            Sort sort,
            String mapBy,
            String path,
            Integer start,
            Integer limit) {

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

    @Override
    public String toString() {
        return "[Include:" + value + "]";
    }
}
