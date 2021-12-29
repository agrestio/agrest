package io.agrest;

import io.agrest.base.protocol.Exp;
import io.agrest.base.protocol.Exclude;
import io.agrest.base.protocol.Include;
import io.agrest.base.protocol.Sort;

import java.util.List;

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

    AgRequestBuilder addOrdering(String unparsedOrdering);

    AgRequestBuilder addOrdering(String unparsedOrdering, String unparsedDir);

    AgRequestBuilder addOrdering(Sort ordering);

    AgRequestBuilder addOrdering(int index, Sort ordering);

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
}
