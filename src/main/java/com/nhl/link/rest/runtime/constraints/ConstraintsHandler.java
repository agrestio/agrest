package com.nhl.link.rest.runtime.constraints;

import java.util.List;

import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * An {@link IConstraintsHandler} that ensures that no target attributes exceed
 * the defined bounds.
 * 
 * @since 1.5
 */
public class ConstraintsHandler implements IConstraintsHandler {

	public static final String DEFAULT_READ_CONSTRAINTS_LIST = "linkrest.constraints.read.list";
	public static final String DEFAULT_WRITE_CONSTRAINTS_LIST = "linkrest.constraints.write.list";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintsHandler.class);

	private RequestConstraintsHandler treeConstraintsHandler;
	private EntityConstraintHandler entityConstraintHandler;

	public ConstraintsHandler(@Inject(DEFAULT_READ_CONSTRAINTS_LIST) List<EntityConstraint> readConstraints,
			@Inject(DEFAULT_WRITE_CONSTRAINTS_LIST) List<EntityConstraint> writeConstraints,
			@Inject IMetadataService metadataService) {

		this.treeConstraintsHandler = new RequestConstraintsHandler(metadataService);
		this.entityConstraintHandler = new EntityConstraintHandler(readConstraints, writeConstraints);
	}

	@Override
	public <T> void constrainUpdate(UpdateContext<T> context, ConstraintsBuilder<T> c) {

		if (!treeConstraintsHandler.constrainUpdate(context, c)) {
			entityConstraintHandler.constrainUpdate(context);
		}
	}

	@Override
	public <T> void constrainResponse(DataResponse<T> response, SizeConstraints sizeConstraints,
			ConstraintsBuilder<T> c) {

		if (sizeConstraints != null) {
			applySizeConstraintsForRead(response, sizeConstraints);
		}

		ResourceEntity<T> resourceEntity = response.getEntity();
		if (!treeConstraintsHandler.constrainResponse(resourceEntity, c)) {
			entityConstraintHandler.constrainResponse(resourceEntity);
		}
	}

	protected void applySizeConstraintsForRead(DataResponse<?> response, SizeConstraints constraints) {

		// fetchOffset - do not exceed source offset
		int upperOffset = constraints.getFetchOffset();
		if (upperOffset > 0 && response.getFetchOffset() > upperOffset) {
			LOGGER.info("Reducing fetch offset from " + response.getFetchOffset() + " to max allowed value of "
					+ upperOffset);
			response.withFetchOffset(upperOffset);
		}

		// fetchLimit - do not exceed source limit
		int upperLimit = constraints.getFetchLimit();
		if (upperLimit > 0 && response.getFetchLimit() > upperLimit) {
			LOGGER.info(
					"Reducing fetch limit from " + response.getFetchLimit() + " to max allowed value of " + upperLimit);
			response.withFetchLimit(upperLimit);
		}
	}

}
