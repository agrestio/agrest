package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectId;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * A stage that updates the response after the data got stored in the DB in the
 * previous stages.
 * 
 * @since 1.16
 */
public class CayenneUpdatePostProcessStage<T extends DataObject> extends ProcessingStage<UpdateContext<T>, T> {

	private Status status;

	public CayenneUpdatePostProcessStage(Processor<UpdateContext<T>, ? super T> next, Status status) {
		super(next);
		this.status = status;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doExecute(UpdateContext<T> context) {

		UpdateResponse<T> response = context.getResponse();
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
			List<T> objects = new ArrayList<>(response.getUpdates().size());

			// 'seen' is for a less common case of multiple updates per object
			// in a request
			Set<ObjectId> seen = new HashSet<>();

			for (EntityUpdate u : response.getUpdates()) {

				T o = (T) u.getMergedTo();
				if (o != null && seen.add(o.getObjectId())) {
					objects.add(o);
				}
			}

			response.withObjects(objects);
		}

	}
}
