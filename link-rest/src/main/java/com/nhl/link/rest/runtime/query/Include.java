package com.nhl.link.rest.runtime.query;

/**
 * @since 2.13
 *
 * Represents Include query parameter
 */
public class Include {

    private CayenneExp cayenneExp;
    private Sort sort;

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
}
