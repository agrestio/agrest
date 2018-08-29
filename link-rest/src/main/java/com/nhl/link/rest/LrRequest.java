package com.nhl.link.rest;

import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.MapBy;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.protocol.Start;

import java.util.Collections;
import java.util.List;

/**
 * A container of LinkRest protocol parameters for a single request.
 *
 * @since 2.13
 */
public class LrRequest {

    private CayenneExp cayenneExp;
    private Sort sort;
    private Dir sortDirection;
    private MapBy mapBy;
    private Start start;
    private Limit limit;
    private List<Include> includes;
    private List<Exclude> excludes;

    protected LrRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public CayenneExp getCayenneExp() {
        return cayenneExp;
    }

    public List<Include> getIncludes() {
        return includes == null ? Collections.emptyList() : includes;
    }

    public List<Exclude> getExcludes() {
        return excludes == null ? Collections.emptyList() : excludes;
    }

    public Sort getSort() {
        return sort;
    }

    public Dir getSortDirection() {
        return sortDirection;
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

    public static class Builder {
        private LrRequest request;

        public Builder() {
            this.request = new LrRequest();
        }

        public LrRequest build() {
            return request;
        }

        public Builder cayenneExp(CayenneExp cayenneExp) {
            this.request.cayenneExp = cayenneExp;
            return this;
        }

        public Builder sort(Sort sort) {
            this.request.sort = sort;
            return this;
        }

        public Builder sortDirection(Dir direction) {
            this.request.sortDirection = direction;
            return this;
        }

        public Builder mapBy(MapBy mapBy) {
            this.request.mapBy = mapBy;
            return this;
        }

        public Builder start(Start start) {
            this.request.start = start;
            return this;
        }

        public Builder limit(Limit limit) {
            this.request.limit = limit;
            return this;
        }

        public Builder includes(List<Include> includes) {
            this.request.includes = includes;
            return this;
        }

        public Builder excludes(List<Exclude> excludes) {
            this.request.excludes = excludes;
            return this;
        }
    }
}
