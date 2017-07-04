package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

import java.lang.annotation.Annotation;

/**
 * @since 1.19
 * @deprecated since 2.7 in favor of {@link com.nhl.link.rest.processor2.Processor} based stages.
 */
public class ParseSelectRequestStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

    private IRequestParser requestParser;
    private IMetadataService metadataService;

    public ParseSelectRequestStage(ProcessingStage<SelectContext<T>, ? super T> next, IRequestParser requestParser,
                                   IMetadataService metadataService) {
        super(next);
        this.requestParser = requestParser;
        this.metadataService = metadataService;
    }

    @Override
    public Class<? extends Annotation> afterStageListener() {
        return SelectRequestParsed.class;
    }

    @Override
    protected void doExecute(SelectContext<T> context) {

        LrEntity<T> entity = metadataService.getLrEntity(context.getType());
        ResourceEntity<T> resourceEntity = requestParser.parseSelect(entity,
                context.getProtocolParameters(),
                context.getAutocompleteProperty());
        context.setEntity(resourceEntity);
    }
}
