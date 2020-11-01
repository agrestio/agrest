package io.agrest.runtime.request;

import io.agrest.AgRequestBuilder;
import io.agrest.runtime.protocol.IExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISortParser;
import org.apache.cayenne.di.Inject;

/**
 * @since 3.2
 */
public class DefaultRequestBuilderFactory implements IAgRequestBuilderFactory {

    private IExpParser expParser;
    private ISortParser sortParser;
    private IIncludeParser includeParser;
    private IExcludeParser excludeParser;

    public DefaultRequestBuilderFactory(
            @Inject IExpParser expParser,
            @Inject ISortParser sortParser,
            @Inject IIncludeParser includeParser,
            @Inject IExcludeParser excludeParser) {

        this.expParser = expParser;
        this.sortParser = sortParser;
        this.includeParser = includeParser;
        this.excludeParser = excludeParser;
    }

    @Override
    public AgRequestBuilder builder() {
        return new DefaultRequestBuilder(expParser, sortParser, includeParser, excludeParser);
    }
}
