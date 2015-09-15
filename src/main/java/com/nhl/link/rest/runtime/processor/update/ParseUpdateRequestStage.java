package com.nhl.link.rest.runtime.processor.update;

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.IUpdateParser;

public class ParseUpdateRequestStage<T> extends BaseLinearProcessingStage<UpdateContext<T>, T> {

	private IRequestParser requestParser;
	private IUpdateParser updateParser;
	private IMetadataService metadataService;

	public ParseUpdateRequestStage(ProcessingStage<UpdateContext<T>, ? super T> next, IRequestParser requestParser,
			IUpdateParser updateParser, IMetadataService metadataService) {
		super(next);
		this.requestParser = requestParser;
		this.updateParser = updateParser;
		this.metadataService = metadataService;
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return UpdateRequestParsed.class;
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {

		LrEntity<T> entity = metadataService.getLrEntity(context.getType());

		// TODO: should we skip this for SimpleResponse-returning updates?
		ResourceEntity<T> resourceEntity = requestParser.parseUpdate(entity, context.getUriInfo());
		context.getResponse().resourceEntity(resourceEntity);

		// skip parsing if we already received EntityUpdates collection parsed
		// by MessageBodyReader
		if (context.getUpdates() == null) {
			Collection<EntityUpdate<T>> updates = updateParser.parse(entity, context.getEntityData());
			context.setUpdates(updates);
		}
	}
}
