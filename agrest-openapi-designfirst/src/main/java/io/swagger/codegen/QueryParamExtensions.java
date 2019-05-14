package io.swagger.codegen;

import org.openapitools.codegen.CodegenParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class QueryParamExtensions {

    private static final String PARAM_ADD_TO_REQUEST_METHOD = "addToRequestMethod";
    private static final String PARAM_REQUEST_METHOD_SECOND_ARG = "requestMethodSecondArg";

    // AgRequestBuilder methods that differ from param name
    private static final Map<String, String> addToRequestMethods = new HashMap<String, String>() {
        {
            put("include", "addIncludes");
            put("exclude", "addExcludes");
        }
    };

    public static List<CodegenParameter> extend(List<CodegenParameter> parameters) {

        // TODO: does it ever happen?
        if (parameters == null) {
            return null;
        }

        CodegenParameter sort = null;
        CodegenParameter dir = null;

        for (CodegenParameter p : parameters) {

            if ("dir".equals(p.baseName)) {
                // do not add "dir" value to the builder directly... track it here and combine with "sort" later
                dir = p;
            } else {

                if ("sort".equals(p.baseName)) {
                    sort = p;
                }

                p.vendorExtensions.put(PARAM_ADD_TO_REQUEST_METHOD, addToRequestMethods.getOrDefault(p.baseName, p.baseName));
            }
        }

        // combine sort and dir
        if (sort != null && dir != null) {
            sort.vendorExtensions.put(PARAM_REQUEST_METHOD_SECOND_ARG, ", dir");
        }

        return parameters;
    }
}
