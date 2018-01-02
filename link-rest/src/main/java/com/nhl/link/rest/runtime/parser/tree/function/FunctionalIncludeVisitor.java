package com.nhl.link.rest.runtime.parser.tree.function;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.tree.IncludeVisitor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;

public class FunctionalIncludeVisitor extends IncludeVisitor {

    private final Map<String, FunctionProcessor> functionProcessors;

	public FunctionalIncludeVisitor(Map<String, FunctionProcessor> functionProcessors) {
        // sanity check
        if (functionProcessors.isEmpty()) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "No function processors provided");
        }
		this.functionProcessors = withNormalizedNames(functionProcessors);
	}

    /**
     * Converts all keys to lower case
     */
    private Map<String, FunctionProcessor> withNormalizedNames(Map<String, FunctionProcessor> functionProcessors) {
        Map<String, FunctionProcessor> result = new HashMap<>();
        functionProcessors.forEach((k, v) -> result.put(k.toLowerCase(), v));
        return result;
    }

    @Override
    public void visitFunction(ResourceEntity<?> context, String functionName, String callExpression) {
        // case-insensitive names
        functionName = functionName.toLowerCase();
        FunctionProcessor functionProcessor = functionProcessors.get(functionName);
        if (functionProcessor == null) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Unknown function: " + functionName);
        }
        functionProcessor.processCallExpression(context, callExpression);
    }
}
