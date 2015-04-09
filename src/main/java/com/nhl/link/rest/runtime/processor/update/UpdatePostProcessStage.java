package com.nhl.link.rest.runtime.processor.update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectId;
import org.apache.cayenne.Persistent;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public class UpdatePostProcessStage extends ProcessingStage<UpdateContext<?>> {

	private Status status;

	public UpdatePostProcessStage(Processor<UpdateContext<?>> next, Status status) {
		super(next);
		this.status = status;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doExecute(UpdateContext<?> context) {

		UpdateResponse<?> response = context.getResponse();
		response.withStatus(status);

		// response objects are attached to EntityUpdate instances ... if
		// 'includeData' is true create a list of unique updated objects in the
		// order corresponding to their initial appearance in the update.
		// We do not have to guarantee the order of objects in response (and
		// only Sencha seems to care - see #46), but there's not much overhead
		// involved, so we are doing it for all clients, not just Sencha

		if (context.isIncludingDataInResponse()) {

			// if there are dupes, the list size will be smaller... sizing it
			// pessimistically
			List objects = new ArrayList<>(response.getUpdates().size());

			// TODO: Cayenne API leak - here we should not know about ObjectId.

			// 'seen' is for a less common case of multiple updates per object
			// in a request
			Set<ObjectId> seen = new HashSet<>();

			for (EntityUpdate u : response.getUpdates()) {

				Persistent o = (Persistent) u.getMergedTo();
				if (o != null && seen.add(o.getObjectId())) {
					objects.add(o);
				}
			}

			response.withObjects(objects);
		}

	}
}
