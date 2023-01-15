package io.agrest.runtime.protocol;

import io.agrest.protocol.Include;

import java.util.List;

/**
 * Parsing of Include query parameter from string value.
 *
 * @since 2.13
 */
public interface IIncludeParser {

    // TODO: this API is not taking "maxPathDepth" policy into account. Mainly because it doesn't do full parse of paths,
    //  so it has no information about the full depth just yet. The policy is applied later in the IIncludeMerger.
    //  Perhaps we should add some checks here too?

    List<Include> parse(String unparsed);
}
