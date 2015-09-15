package com.nhl.link.rest.runtime.processor.select;

import java.lang.annotation.Annotation;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * @since 1.19
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
		ResourceEntity<T> resourceEntity = requestParser.parseSelect(entity, context.getUriInfo(),
				context.getAutocompleteProperty());
		context.getResponse().resourceEntity(resourceEntity);
	}
}
