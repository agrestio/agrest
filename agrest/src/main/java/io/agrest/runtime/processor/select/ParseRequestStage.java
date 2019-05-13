package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.protocol.ParameterExtractor;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<SelectContext<?>> {

    protected static final String PROTOCOL_CAYENNE_EXP = "cayenneExp";
    protected static final String PROTOCOL_DIR = "dir";
    protected static final String PROTOCOL_EXCLUDE = "exclude";
    protected static final String PROTOCOL_INCLUDE = "include";
    protected static final String PROTOCOL_LIMIT = "limit";
    protected static final String PROTOCOL_MAP_BY = "mapBy";
    protected static final String PROTOCOL_SORT = "sort";
    protected static final String PROTOCOL_START = "start";

    protected IAgRequestBuilderFactory requestBuilderFactory;

    public ParseRequestStage(@Inject IAgRequestBuilderFactory requestBuilderFactory) {
        this.requestBuilderFactory = requestBuilderFactory;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        context.setRawRequest(mergedRequest(context));
    }

    private AgRequest mergedRequest(SelectContext<?> context) {

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

    // keeping as protected, and returning a mutable builder to allow subclasses to interfere
    protected AgRequestBuilder requestFromParams(Map<String, List<String>> parameters) {
        return requestBuilderFactory.builder()
                .cayenneExp(ParameterExtractor.string(parameters, PROTOCOL_CAYENNE_EXP))
                .sort(ParameterExtractor.string(parameters, PROTOCOL_SORT), ParameterExtractor.string(parameters, PROTOCOL_DIR))
                .mapBy(ParameterExtractor.string(parameters, PROTOCOL_MAP_BY))
                .addIncludes(ParameterExtractor.strings(parameters, PROTOCOL_INCLUDE))
                .addExcludes(ParameterExtractor.strings(parameters, PROTOCOL_EXCLUDE))
                .start(ParameterExtractor.integerObject(parameters, PROTOCOL_START))
                .limit(ParameterExtractor.integerObject(parameters, PROTOCOL_LIMIT));
    }

    // keeping as protected, and returning a mutable builder to allow subclasses to interfere
    protected AgRequestBuilder requestFromRequestAndParams(AgRequest request, Map<String, List<String>> parameters) {
        return new RequestParametersMerger(request, parameters).merge();
    }

    private class RequestParametersMerger {

        private Map<String, List<String>> parameters;
        private AgRequest request;

        RequestParametersMerger(AgRequest request, Map<String, List<String>> parameters) {
            this.parameters = parameters;
            this.request = request;
        }

        AgRequestBuilder merge() {
            AgRequestBuilder builder = requestBuilderFactory.builder();

            setCayenneExp(builder);
            setSort(builder);
            setMapBy(builder);
            setIncludes(builder);
            setExcludes(builder);
            setStart(builder);
            setLimit(builder);

            return builder;
        }

        private void setCayenneExp(AgRequestBuilder builder) {
            if (request.getCayenneExp() != null) {
                builder.cayenneExp(request.getCayenneExp());
            } else {
                builder.cayenneExp(ParameterExtractor.string(parameters, PROTOCOL_CAYENNE_EXP));
            }
        }

        private void setSort(AgRequestBuilder builder) {

            if (request.getSort() != null) {
                builder.sort(request.getSort());
            } else {
                builder.sort(
                        ParameterExtractor.string(parameters, PROTOCOL_SORT),
                        ParameterExtractor.string(parameters, PROTOCOL_DIR));
            }
        }

        private void setMapBy(AgRequestBuilder builder) {
            if (request.getMapBy() != null) {
                builder.mapBy(request.getMapBy());
            } else {
                builder.mapBy(ParameterExtractor.string(parameters, PROTOCOL_MAP_BY));
            }
        }

        private void setIncludes(AgRequestBuilder builder) {
            if (!request.getIncludes().isEmpty()) {
                request.getIncludes().forEach(builder::addInclude);
            } else {
                builder.addIncludes(ParameterExtractor.strings(parameters, PROTOCOL_INCLUDE));
            }
        }

        private void setExcludes(AgRequestBuilder builder) {
            if (!request.getExcludes().isEmpty()) {
                request.getExcludes().forEach(builder::addExclude);
            } else {
                builder.addExcludes(ParameterExtractor.strings(parameters, PROTOCOL_EXCLUDE));
            }
        }

        private void setStart(AgRequestBuilder builder) {
            if (request.getStart() != null) {
                builder.start(request.getStart());
            } else {
                builder.start(ParameterExtractor.integerObject(parameters, PROTOCOL_START));
            }
        }

        private void setLimit(AgRequestBuilder builder) {
            if (request.getLimit() != null) {
                builder.limit(request.getLimit());
            } else {
                builder.limit(ParameterExtractor.integerObject(parameters, PROTOCOL_LIMIT));
            }
        }
    }
}