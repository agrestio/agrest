package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataObject;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public class CayenneCreateOrUpdateStage<T extends DataObject> extends CayenneUpdateStage<T> {

	private boolean idempotent;

	public CayenneCreateOrUpdateStage(Processor<UpdateContext<T>, ? super T> next, boolean idempotent) {
		super(next);
		this.idempotent = idempotent;
	}

	@Override
	protected void afterUpdatesMerge(UpdateContext<T> context, Map<Object, Collection<EntityUpdate>> keyMap) {

		if (keyMap.isEmpty()) {
			return;
		}

		ObjectRelator relator = createRelator(context);

		for (Entry<Object, Collection<EntityUpdate>> e : keyMap.entrySet()) {

			// null key - each update is individual object to create;
			// explicit key - each update applies to the same object;

			if (e.getKey() == null) {

				if (idempotent) {
					throw new LinkRestException(Status.BAD_REQUEST, "Request is not idempotent.");
				}

			}

			for (EntityUpdate u : e.getValue()) {
				createSingle(context, relator, u);
			}
		}
	}

}
