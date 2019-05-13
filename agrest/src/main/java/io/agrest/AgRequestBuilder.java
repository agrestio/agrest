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

    AgRequestBuilder sort(String unparsedSort);

    AgRequestBuilder sort(String unparsedSort, String unparsedDir);

    AgRequestBuilder sort(Sort sort);

    AgRequestBuilder cayenneExp(String unparsedExp);

    AgRequestBuilder cayenneExp(CayenneExp exp);

    AgRequestBuilder mapBy(String mapByPath);

    AgRequestBuilder start(Integer start);

    AgRequestBuilder limit(Integer limit);
}
