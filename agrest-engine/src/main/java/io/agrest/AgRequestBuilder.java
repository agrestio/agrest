package io.agrest;

import io.agrest.protocol.Exclude;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;

import java.util.List;
import java.util.Map;

/**
 * @since 3.2
 */
public interface AgRequestBuilder {

    AgRequest build();

    AgRequestBuilder addIncludes(List<String> unparsedIncludes);

    AgRequestBuilder addInclude(Include include);

    AgRequestBuilder addInclude(String unparsedInclude);

    AgRequestBuilder addExcludes(List<String> unparsedExcludes);

    AgRequestBuilder addExclude(Exclude exclude);

    AgRequestBuilder addExclude(String unparsedExclude);

    /**
     * @since 5.0
     */
    AgRequestBuilder addSorts(List<String> unparsedSorts);

    /**
     * @since 5.0
     */
    AgRequestBuilder addSort(String unparsedSort);

    /**
     * @since 5.0
     */
    AgRequestBuilder addSort(String unparsedSort, String unparsedDirection);

    /**
     * @since 5.0
     */
    AgRequestBuilder addSort(Sort sort);

    /**
     * @since 5.0
     */
    AgRequestBuilder addSort(int index, Sort sort);

    /**
     * @since 4.4
     */
    AgRequestBuilder andExp(String unparsedExp);

    /**
     * @since 4.4
     */
    AgRequestBuilder andExp(Exp exp);

    /**
     * @since 4.4
     */
    AgRequestBuilder orExp(String unparsedExp);

    /**
     * @since 4.4
     */
    AgRequestBuilder orExp(Exp exp);

    AgRequestBuilder mapBy(String mapByPath);

    AgRequestBuilder start(Integer start);

    AgRequestBuilder limit(Integer limit);

    /**
     * Loads a map of parameters coming in the client request. Parameters will only be added if they are not already
     * defined in the builder. This way parameters provided by the server take precedence over client parameters.
     *
     * @since 5.0
     */
    AgRequestBuilder mergeClientParams(Map<String, List<String>> params);

    /**
     * Overrides all collected protocol values with parameters from the provided request.
     *
     * @since 5.0
     */
    AgRequestBuilder setRequest(AgRequest request);
}
