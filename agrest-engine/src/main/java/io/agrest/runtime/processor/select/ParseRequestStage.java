package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.base.protocol.AgProtocol;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.protocol.ParameterExtractor;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<SelectContext<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseRequestStage.class);

    protected final IAgRequestBuilderFactory requestBuilderFactory;

    public ParseRequestStage(@Inject IAgRequestBuilderFactory requestBuilderFactory) {
        this.requestBuilderFactory = requestBuilderFactory;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        context.setMergedRequest(mergeRequest(context));
    }

    private AgRequest mergeRequest(SelectContext<?> context) {

        AgRequest request = context.getRequest();
        Map<String, List<String>> parameters = context.getProtocolParameters();

        if (request == null) {
            return requestFromParams(parameters).build();
        } else if (!parameters.isEmpty()) {
            return requestFromRequestAndParams(request, parameters).build();
        } else {
            return request;
        }
    }

    private String exp(Map<String, List<String>> params) {
        String exp = ParameterExtractor.string(params, AgProtocol.exp);
        String cayenneExp = ParameterExtractor.string(params, AgProtocol.cayenneExp);

        // keep supporting deprecated "cayenneExp" key
        // TODO: if we ever start supporting multiple "exp" keys, these two can be concatenated.
        //  For now "exp" overrides "cayenneExp"
        if (exp != null) {
            return exp;
        }

        if (cayenneExp != null) {
            LOGGER.info("*** 'cayenneExp' parameter is deprecated since Agrest 4.1. Consider replacing it with 'exp'");
        }

        return cayenneExp;
    }

    // keeping as protected, and returning a mutable builder to allow subclasses to interfere
    protected AgRequestBuilder requestFromParams(Map<String, List<String>> parameters) {

        return requestBuilderFactory.builder()
                .andExp(exp(parameters))
                .addOrdering(ParameterExtractor.string(parameters, AgProtocol.sort), ParameterExtractor.string(parameters, AgProtocol.dir))
                .mapBy(ParameterExtractor.string(parameters, AgProtocol.mapBy))
                .addIncludes(ParameterExtractor.strings(parameters, AgProtocol.include))
                .addExcludes(ParameterExtractor.strings(parameters, AgProtocol.exclude))
                .start(ParameterExtractor.integerObject(parameters, AgProtocol.start))
                .limit(ParameterExtractor.integerObject(parameters, AgProtocol.limit));
    }

    // keeping as protected, and returning a mutable builder to allow subclasses to interfere
    protected AgRequestBuilder requestFromRequestAndParams(AgRequest request, Map<String, List<String>> parameters) {
        return new RequestParametersMerger(request, parameters).merge();
    }

    private class RequestParametersMerger {

        private final Map<String, List<String>> parameters;
        private final AgRequest request;

        RequestParametersMerger(AgRequest request, Map<String, List<String>> parameters) {
            this.parameters = parameters;
            this.request = request;
        }

        AgRequestBuilder merge() {
            AgRequestBuilder builder = requestBuilderFactory.builder();

            setExp(builder);
            setOrderings(builder);
            setMapBy(builder);
            setIncludes(builder);
            setExcludes(builder);
            setStart(builder);
            setLimit(builder);

            return builder;
        }

        private void setExp(AgRequestBuilder builder) {
            if (request.getExp() != null) {
                builder.andExp(request.getExp());
            } else {
                builder.andExp(exp(parameters));
            }
        }

        private void setOrderings(AgRequestBuilder builder) {

            if (!request.getOrderings().isEmpty()) {
                request.getOrderings().forEach(builder::addOrdering);
            } else {
                builder.addOrdering(
                        ParameterExtractor.string(parameters, AgProtocol.sort),
                        ParameterExtractor.string(parameters, AgProtocol.dir));
            }
        }

        private void setMapBy(AgRequestBuilder builder) {
            if (request.getMapBy() != null) {
                builder.mapBy(request.getMapBy());
            } else {
                builder.mapBy(ParameterExtractor.string(parameters, AgProtocol.mapBy));
            }
        }

        private void setIncludes(AgRequestBuilder builder) {
            if (!request.getIncludes().isEmpty()) {
                request.getIncludes().forEach(builder::addInclude);
            } else {
                builder.addIncludes(ParameterExtractor.strings(parameters, AgProtocol.include));
            }
        }

        private void setExcludes(AgRequestBuilder builder) {
            if (!request.getExcludes().isEmpty()) {
                request.getExcludes().forEach(builder::addExclude);
            } else {
                builder.addExcludes(ParameterExtractor.strings(parameters, AgProtocol.exclude));
            }
        }

        private void setStart(AgRequestBuilder builder) {
            if (request.getStart() != null) {
                builder.start(request.getStart());
            } else {
                builder.start(ParameterExtractor.integerObject(parameters, AgProtocol.start));
            }
        }

        private void setLimit(AgRequestBuilder builder) {
            if (request.getLimit() != null) {
                builder.limit(request.getLimit());
            } else {
                builder.limit(ParameterExtractor.integerObject(parameters, AgProtocol.limit));
            }
        }
    }
}