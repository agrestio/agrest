package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * A container of LinkRest protocol parameters.
 *
 * @since 2.13
 */
public class Query {

    private CayenneExp cayenneExp;
    private Sort sort;
    private Dir dir;
    private MapBy mapBy;
    private Start start;
    private Limit limit;
    private List<Include> include;
    private List<Exclude> exclude;

    public Query(CayenneExp cayenneExp, Sort sort, Dir dir, MapBy mapBy, Start start, Limit limit, List<Include> include, List<Exclude> exclude) {
        this.cayenneExp = cayenneExp;
        this.sort = sort;
        this.dir = dir;
        this.mapBy = mapBy;
        this.start = start;
        this.limit = limit;
        this.include = include;
        this.exclude = exclude;
    }

    public CayenneExp getCayenneExp() {
        return cayenneExp;
    }

    public List<Include> getInclude() {
        return include == null ? new ArrayList<>() : include;
    }

    public List<Exclude> getExclude() {
        return exclude == null ? new ArrayList<>() : exclude;
    }

    public Sort getSort() {
        return sort;
    }

    public Dir getDir() {
        return dir;
    }

    public MapBy getMapBy() {
        return mapBy;
    }

    public Start getStart() {
        return start;
    }

    public Limit getLimit() {
        return limit;
    }

}
