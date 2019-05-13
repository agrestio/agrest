package io.agrest.runtime.request;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;

import java.util.List;

/**
 * @since 3.2
 */
public class DefaultRequestBuilder implements AgRequestBuilder {

    private DefaultRequest request;
    private ICayenneExpParser cayenneExpParser;
    private ISortParser sortParser;
    private IIncludeParser includeParser;
    private IExcludeParser excludeParser;

    public DefaultRequestBuilder(
            ICayenneExpParser cayenneExpParser,
            ISortParser sortParser,
            IIncludeParser includeParser,
            IExcludeParser excludeParser) {

        this.request = new DefaultRequest();

        this.cayenneExpParser = cayenneExpParser;
        this.sortParser = sortParser;
        this.includeParser = includeParser;
        this.excludeParser = excludeParser;
    }

    @Override
    public AgRequest build() {
        return request;
    }

    @Override
    public AgRequestBuilder cayenneExp(String unparsedExp) {
        CayenneExp cayenneExp = unparsedExp != null ? cayenneExpParser.fromString(unparsedExp) : null;
        return cayenneExp(cayenneExp);
    }

    @Override
    public AgRequestBuilder cayenneExp(CayenneExp exp) {
        request.cayenneExp = exp;
        return this;
    }

    @Override
    public AgRequestBuilder sort(String unparsedSort) {
        Sort sort = unparsedSort != null ? sortParser.fromString(unparsedSort) : null;
        return sort(sort);
    }

    @Override
    public AgRequestBuilder sort(String unparsedSort, String unparsedDir) {
        Sort sort = unparsedSort != null ? sortParser.fromString(unparsedSort) : null;

        // "dir" makes sense only if we are dealing with a simple sort
        if (sort != null && sort.getSorts().isEmpty()) {
            Dir dir = unparsedDir != null ? sortParser.dirFromString(unparsedDir) : null;
            if (dir != null) {
                sort = new Sort(sort.getProperty(), dir);
            }
        }

        request.sort = sort;
        return this;
    }

    @Override
    public AgRequestBuilder sort(Sort sort) {
        request.sort = sort;
        return this;
    }

    @Override
    public AgRequestBuilder mapBy(String mapByPath) {
        request.mapBy = mapByPath;
        return this;
    }

    @Override
    public AgRequestBuilder start(Integer start) {
        request.start = start;
        return this;
    }

    @Override
    public AgRequestBuilder limit(Integer limit) {
        request.limit = limit;
        return this;
    }

    @Override
    public AgRequestBuilder includes(List<String> unparsedIncludes) {

        request.includes.clear();

        for (String ui : unparsedIncludes) {
            addInclude(ui);
        }

        return this;
    }

    @Override
    public AgRequestBuilder addInclude(String unparsedInclude) {
        if (unparsedInclude != null) {
            request.includes.add(includeParser.oneFromString(unparsedInclude));
        }
        return this;
    }

    @Override
    public AgRequestBuilder addInclude(Include include) {
        if (include != null) {
            request.includes.add(include);
        }

        return this;
    }

    @Override
    public AgRequestBuilder excludes(List<String> unparsedExcludes) {
        request.includes.clear();

        for (String ui : unparsedExcludes) {
            addExclude(ui);
        }

        return this;
    }

    @Override
    public AgRequestBuilder addExclude(String unparsedExclude) {
        if (unparsedExclude != null) {
            request.excludes.add(excludeParser.oneFromString(unparsedExclude));
        }
        return this;
    }

    @Override
    public AgRequestBuilder addExclude(Exclude exclude) {
        if (exclude != null) {
            request.excludes.add(exclude);
        }

        return this;
    }
}
