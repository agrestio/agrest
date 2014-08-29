package com.nhl.link.rest.runtime.constraints;

import java.util.List;

import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

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

	private TreeConstraintsHandler treeConstraintsHandler;
	private EntityConstraintHandler entityConstraintHandler;

	public ConstraintsHandler(@Inject ICayennePersister persister,
			@Inject(DEFAULT_READ_CONSTRAINTS_LIST) List<EntityConstraint> readConstraints,
			@Inject(DEFAULT_WRITE_CONSTRAINTS_LIST) List<EntityConstraint> writeConstraints) {

		this.treeConstraintsHandler = new TreeConstraintsHandler();
		this.entityConstraintHandler = new EntityConstraintHandler(persister, readConstraints, writeConstraints);
	}

	@Override
	public <T> void constrainUpdate(UpdateResponse<T> response, TreeConstraints<T> c) {

		if (!treeConstraintsHandler.constrainUpdate(response, c)) {
			entityConstraintHandler.constrainUpdate(response);
		}
	}

	@Override
	public <T> void constrainResponse(DataResponse<T> response, SizeConstraints sizeConstraints, TreeConstraints<T> c) {

		if (sizeConstraints != null) {
			applySizeConstraintsForRead(response, sizeConstraints);
		}

		if (!treeConstraintsHandler.constrainResponse(response, c)) {
			entityConstraintHandler.constrainResponse(response);
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
			LOGGER.info("Reducing fetch limit from " + response.getFetchLimit() + " to max allowed value of "
					+ upperLimit);
			response.withFetchLimit(upperLimit);
		}
	}

}
