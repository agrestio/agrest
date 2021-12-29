package io.agrest.runtime.request;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.base.protocol.Exp;
import io.agrest.base.protocol.Exclude;
import io.agrest.base.protocol.Include;
import io.agrest.base.protocol.Sort;
import io.agrest.runtime.protocol.IExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;

import java.util.List;

/**
 * @since 3.2
 */
public class DefaultRequestBuilder implements AgRequestBuilder {

    private DefaultRequest request;
    private IExpParser expParser;
    private ISortParser sortParser;
    private IIncludeParser includeParser;
    private IExcludeParser excludeParser;

    public DefaultRequestBuilder(
            IExpParser expParser,
            ISortParser sortParser,
            IIncludeParser includeParser,
            IExcludeParser excludeParser) {

        this.request = new DefaultRequest();

        this.expParser = expParser;
        this.sortParser = sortParser;
        this.includeParser = includeParser;
        this.excludeParser = excludeParser;
    }

    @Override
    public AgRequest build() {
        return request;
    }

    @Override
    public AgRequestBuilder andExp(String unparsedExp) {
        Exp exp = unparsedExp != null ? expParser.fromString(unparsedExp) : null;
        return andExp(exp);
    }

    @Override
    public AgRequestBuilder andExp(Exp exp) {
        request.exp = request.exp != null ? request.exp.and(exp) : exp;
        return this;
    }

    @Override
    public AgRequestBuilder orExp(String unparsedExp) {
        Exp exp = unparsedExp != null ? expParser.fromString(unparsedExp) : null;
        return orExp(exp);
    }

    @Override
    public AgRequestBuilder orExp(Exp exp) {
        request.exp = request.exp != null ? request.exp.or(exp) : exp;
        return this;
    }

    @Override
    public AgRequestBuilder addOrdering(String unparsedOrdering) {
        if (unparsedOrdering != null && unparsedOrdering.length() > 0) {
            request.orderings.addAll(sortParser.parse(unparsedOrdering, null));
        }
        return this;
    }

    @Override
    public AgRequestBuilder addOrdering(String unparsedOrdering, String unparsedDir) {
        if (unparsedOrdering != null && unparsedOrdering.length() > 0) {
            request.orderings.addAll(sortParser.parse(unparsedOrdering, unparsedDir));
        }
        return this;
    }

    @Override
    public AgRequestBuilder addOrdering(Sort ordering) {
        if (ordering != null) {
            request.orderings.add(ordering);
        }
        return this;
    }

    @Override
    public AgRequestBuilder addOrdering(int index, Sort ordering) {
        if (ordering != null) {
            request.orderings.add(index, ordering);
        }
        return this;
    }

    @Override
    public AgRequestBuilder mapBy(String mapByPath) {
        request.mapBy = mapByPath;
        return this;
    }

    @Override
    public AgRequestBuilder start(Integer start) {
        // TODO: validate negative numbers, resetting to null?
        request.start = start;
        return this;
    }

    @Override
    public AgRequestBuilder limit(Integer limit) {
        // TODO: validate negative numbers, resetting to null?
        request.limit = limit;
        return this;
    }

    @Override
    public AgRequestBuilder addIncludes(List<String> unparsedIncludes) {

        for (String ui : unparsedIncludes) {
            addInclude(ui);
        }

        return this;
    }

    @Override
    public AgRequestBuilder addInclude(String unparsedInclude) {
        if (unparsedInclude != null) {
            request.includes.addAll(includeParser.parse(unparsedInclude));
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
    public AgRequestBuilder addExcludes(List<String> unparsedExcludes) {

        for (String ui : unparsedExcludes) {
            addExclude(ui);
        }

        return this;
    }

    @Override
    public AgRequestBuilder addExclude(String unparsedExclude) {
        if (unparsedExclude != null) {
            request.excludes.addAll(excludeParser.parse(unparsedExclude));
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
