package com.nhl.link.rest.runtime.cayenne.processor;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.util.List;

public class CayenneFetchStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

    private ICayennePersister persister;

	public CayenneFetchStage(ProcessingStage<SelectContext<T>, ? super T> next, ICayennePersister persister) {
		super(next);
		this.persister = persister;
	}

    @Override
	public Class<? extends Annotation> afterStageListener() {
		return DataFetched.class;
	}

    @Override
    protected void doExecute(SelectContext<T> context) {
        DataResponse<T> response = context.getResponse();
		SelectQuery<T> select = context.getSelect();

		List<T> objects = persister.sharedContext().select(select);

		if (context.isAtMostOneObject() && objects.size() != 1) {

			LrEntity<?> entity = context.getEntity().getLrEntity();

			if (objects.isEmpty()) {
				throw new LinkRestException(Response.Status.NOT_FOUND,
						String.format("No object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
			} else {
				throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
						"Found more than one object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
			}
		}

		response.withObjects(objects);
    }
}
