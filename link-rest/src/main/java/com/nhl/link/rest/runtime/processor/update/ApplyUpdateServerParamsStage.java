package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LrObjectId;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.UpdateServerParamsApplied;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentRelationship;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApplyUpdateServerParamsStage<T> extends BaseLinearProcessingStage<UpdateContext<T>, T> {

	private IEncoderService encoderService;
	private IConstraintsHandler constraintsHandler;
	private IMetadataService metadataService;

	public ApplyUpdateServerParamsStage(ProcessingStage<UpdateContext<T>, ? super T> next,
			IEncoderService encoderService, IConstraintsHandler constraintsHandler, IMetadataService metadataService) {
		super(next);
		this.encoderService = encoderService;
		this.constraintsHandler = constraintsHandler;
		this.metadataService = metadataService;
	}

	@Override
	public Class<? extends Annotation> afterStageListener() {
		return UpdateServerParamsApplied.class;
	}

	@Override
	protected void doExecute(UpdateContext<T> context) {

		ResourceEntity<T> entity = context.getEntity();

		processExplicitId(context);
		processParentId(context);

		constraintsHandler.constrainUpdate(context, context.getWriteConstraints());

		// apply read constraints (TODO: should we only care about response
		// constraints after the commit?)
		constraintsHandler.constrainResponse(entity, null, context.getReadConstraints());

		// TODO: we don't need encoder if includeData=false... should we
		// conditionally skip this step?
		context.setEncoder(encoderService.dataEncoder(entity));
	}

	private void processExplicitId(UpdateContext<T> context) {

		if (context.isById()) {

			// id was specified explicitly ... this means a few things:
			// * we expect zero or one object in the body
			// * if zero, create an empty update that will be attached to the
			// ID.
			// * if more than one - throw...

			if (context.getUpdates().isEmpty()) {
				context.setUpdates(Collections.singleton(new EntityUpdate<>(context.getEntity().getLrEntity())));
			}

			LrEntity<T> entity = context.getEntity().getLrEntity();
			EntityUpdate<T> u = context.getFirst();
			Map<String, Object> idMap = u.getOrCreateId();
			idMap.putAll(context.getId().asMap(entity));

			u.setExplicitId();
		}
	}

	private void processParentId(UpdateContext<T> context) {

		EntityParent<?> parent = context.getParent();

		if (parent != null && parent.getId() != null) {
			LrRelationship fromParent = relationshipFromParent(context);
			if (fromParent instanceof LrPersistentRelationship) {
				LrPersistentRelationship r = (LrPersistentRelationship) fromParent;
				if (r.isToDependentEntity()) {
					for (EntityUpdate<T> u : context.getUpdates()) {
						u.getOrCreateId().putAll(extractRelatedId(r, parent.getId()));
					}
				}
			}
		}
	}

	private Map<String, Object> extractRelatedId(LrPersistentRelationship relationship, LrObjectId objectId) {
		return relationship.getJoins().stream()
				.collect(HashMap::new,
						(m, j) -> m.put(j.getTargetColumnName(), objectId.get(j.getSourceColumnName())),
						Map::putAll);
	}

	private LrRelationship relationshipFromParent(UpdateContext<?> context) {

		EntityParent<?> parent = context.getParent();

		if (parent == null) {
			return null;
		}

		LrRelationship r = metadataService.getLrRelationship(parent);
		if (r instanceof LrPersistentRelationship) {
			return r;
		}

		return null;
	}

}
