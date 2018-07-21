package com.nhl.link.rest.sencha.parser.filter;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.sencha.protocol.Filter;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.13
 */
public class SenchaFilterParser implements ISenchaFilterParser {

    private static final String EXACT_MATCH = "exactMatch";
    private static final String PROPERTY = "property";
    private static final String VALUE = "value";
    private static final String DISABLED = "disabled";
    private static final String OPERATOR = "operator";

    private IJacksonService jsonParser;

    public SenchaFilterParser(@Inject IJacksonService jsonParser) {
        this.jsonParser = jsonParser;
    }

    private static Object extractValue(JsonNode valueNode) {
        JsonToken type = valueNode.asToken();

        // ExtJS converts everything to String except for NULL and booleans. So
        // follow the same logic here...
        // (http://docs.sencha.com/extjs/4.1.2/source/Filter.html#Ext-util-Filter)
        switch (type) {
            case VALUE_NULL:
                return null;
            case VALUE_FALSE:
                return false;
            case VALUE_TRUE:
                return true;
            case VALUE_NUMBER_INT:
                return valueNode.asInt();
            case VALUE_NUMBER_FLOAT:
                return valueNode.asDouble();
            case START_ARRAY:
                return extractArray(valueNode);
            default:
                return valueNode.asText();
        }
    }

    private static List<Object> extractArray(JsonNode arrayNode) {

        List<Object> values = new ArrayList<>(arrayNode.size());
        for (JsonNode value : arrayNode) {
            values.add(extractValue(value));
        }

        return values;
    }

    @Override
    public List<Filter> fromString(String filtersJson) {

        JsonNode rootNode = jsonParser.parseJson(filtersJson);
        if (rootNode == null || rootNode.isNull()) {
            return null;
        }

        List<Filter> filters = new ArrayList<>(rootNode.size());

        for (JsonNode filterNode : rootNode) {
            JsonNode propertyNode = filterNode.get(PROPERTY);
            if (propertyNode == null) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "filter 'property' is missing" + filterNode.asText());
            }

            JsonNode valueNode = filterNode.get(VALUE);
            if (valueNode == null) {
                throw new LinkRestException(Response.Status.BAD_REQUEST, "filter 'value' is missing" + filterNode.asText());
            }

            JsonNode disabledNode = filterNode.get(DISABLED);
            boolean disabled = disabledNode != null && disabledNode.asBoolean();
            String property = propertyNode.asText();

            Object value = extractValue(valueNode);

            // note that 'exactMatch' is ignored everywhere but in a like expression
            JsonNode exactMatchNode = filterNode.get(EXACT_MATCH);
            boolean exactMatch = exactMatchNode != null && exactMatchNode.asBoolean();

            JsonNode operatorNode = filterNode.get(OPERATOR);
            // TODO: operators as enum - instant validation
            String operator = (operatorNode != null) ? operatorNode.asText() : "like";

            filters.add(new Filter(property, value, operator, disabled, exactMatch));
        }

        return filters;
    }
}
