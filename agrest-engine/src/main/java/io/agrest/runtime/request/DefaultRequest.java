package io.agrest.runtime.request;

import io.agrest.AgRequest;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.2
 */
public class DefaultRequest implements AgRequest {

    protected final List<Include> includes;
    protected final List<Exclude> excludes;
    protected final List<Sort> sorts;

    protected Exp exp;
    protected String mapBy;
    protected Integer start;
    protected Integer limit;


    protected DefaultRequest() {
        this.includes = new ArrayList<>();
        this.excludes = new ArrayList<>();
        this.sorts = new ArrayList<>();
    }

    @Override
    public Exp getExp() {
        return exp;
    }

    @Override
    public List<Include> getIncludes() {
        return includes;
    }

    @Override
    public List<Exclude> getExcludes() {
        return excludes;
    }

    @Override
    public List<Sort> getSorts() {
        return sorts;
    }

    @Override
    public String getMapBy() {
        return mapBy;
    }

    @Override
    public Integer getStart() {
        return start;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }
}
