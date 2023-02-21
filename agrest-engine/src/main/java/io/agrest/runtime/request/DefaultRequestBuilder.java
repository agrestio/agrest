package io.agrest.runtime.request;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.protocol.ControlParams;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IExpParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.ParameterExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @since 3.2
 */
public class DefaultRequestBuilder implements AgRequestBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestBuilder.class);

    private final IExpParser expParser;
    private final ISortParser sortParser;
    private final IIncludeParser includeParser;
    private final IExcludeParser excludeParser;

    private final DefaultRequest request;
    private Map<String, List<String>> clientParams;

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
        // client parameters are applied last after all server-side settings are captured
        applyClientParams();
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
    public AgRequestBuilder addSorts(List<String> unparsedSorts) {
        for (String us : unparsedSorts) {
            addSort(us);
        }

        return this;
    }

    @Override
    public AgRequestBuilder addSort(String unparsedSort) {
        if (unparsedSort != null && unparsedSort.length() > 0) {
            request.sorts.addAll(sortParser.parse(unparsedSort, null));
        }
        return this;
    }

    @Override
    public AgRequestBuilder addSort(String unparsedSort, String unparsedDirection) {
        if (unparsedSort != null && unparsedSort.length() > 0) {
            request.sorts.addAll(sortParser.parse(unparsedSort, unparsedDirection));
        }
        return this;
    }

    @Override
    public AgRequestBuilder addSort(Sort sort) {
        if (sort != null) {
            request.sorts.add(sort);
        }
        return this;
    }

    @Override
    public AgRequestBuilder addSort(int index, Sort sort) {
        if (sort != null) {
            request.sorts.add(index, sort);
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

    @Override
    public AgRequestBuilder mergeClientParams(Map<String, List<String>> params) {
        this.clientParams = params;
        return this;
    }

    @Override
    public AgRequestBuilder setRequest(AgRequest anotherRequest) {

        // Replace existing builder values with the values from another request

        request.exp = anotherRequest.getExp();

        request.sorts.clear();
        request.sorts.addAll(anotherRequest.getSorts());

        request.mapBy = anotherRequest.getMapBy();

        request.includes.clear();
        request.includes.addAll(anotherRequest.getIncludes());

        request.excludes.clear();
        request.excludes.addAll(anotherRequest.getExcludes());

        request.start = anotherRequest.getStart();
        request.limit = anotherRequest.getLimit();

        return this;
    }

    private void applyClientParams() {
        // Not overriding any existing builder values with (presumably URL-originating) parameters ...

        if (request.exp == null) {
            andExp(expFromParams(clientParams));
        }

        if (request.sorts.isEmpty()) {
            addSort(
                    ParameterExtractor.string(clientParams, ControlParams.sort),
                    directionFromParams(clientParams));
        }

        if (request.mapBy == null) {
            mapBy(ParameterExtractor.string(clientParams, ControlParams.mapBy));
        }

        if (request.includes.isEmpty()) {
            addIncludes(ParameterExtractor.strings(clientParams, ControlParams.include));
        }

        if (request.excludes.isEmpty()) {
            addExcludes(ParameterExtractor.strings(clientParams, ControlParams.exclude));
        }

        if (request.start == null) {
            start(ParameterExtractor.integerObject(clientParams, ControlParams.start));
        }

        if (request.limit == null) {
            limit(ParameterExtractor.integerObject(clientParams, ControlParams.limit));
        }

    }

    private String expFromParams(Map<String, List<String>> params) {
        String exp = ParameterExtractor.string(params, ControlParams.exp);
        String cayenneExp = ParameterExtractor.string(params, ControlParams.cayenneExp);

        // TODO: if we ever start supporting multiple "exp" keys, these two can be concatenated.
        //  For now "exp" overrides "cayenneExp"
        if (exp != null) {
            return exp;
        }

        if (cayenneExp != null) {
            LOGGER.info("*** 'cayenneExp' control parameter is deprecated in protocol v1.1 (Agrest 4.1). Consider replacing it with 'exp'");
            return cayenneExp;
        }

        return null;
    }

    private String directionFromParams(Map<String, List<String>> params) {

        String direction = ParameterExtractor.string(params, ControlParams.direction);
        String dir = ParameterExtractor.string(params, ControlParams.dir);

        // TODO: if we ever start supporting multiple "exp" keys, these two can be concatenated.
        //  For now "exp" overrides "cayenneExp"
        if (direction != null) {
            return direction;
        }

        if (dir != null) {
            LOGGER.info("*** 'dir' control parameter is deprecated in protocol v1.2 (Agrest 5.0). Consider replacing it with 'direction'");
            return dir;
        }

        return null;
    }
}
