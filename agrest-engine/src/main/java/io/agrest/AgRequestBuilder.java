package io.agrest;

import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import io.agrest.protocol.Sort;

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

    AgRequestBuilder cayenneExp(String unparsedExp);

    AgRequestBuilder cayenneExp(CayenneExp exp);

    AgRequestBuilder mapBy(String mapByPath);

    AgRequestBuilder start(Integer start);

    AgRequestBuilder limit(Integer limit);
}
