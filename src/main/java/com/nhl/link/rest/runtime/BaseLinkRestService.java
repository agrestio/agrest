package com.nhl.link.rest.runtime;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

/**
 * A backend-agnostic abstract {@link ILinkRestService} that can serve to
 * implement more specific versions.
 */
public abstract class BaseLinkRestService implements ILinkRestService {

	protected IRequestParser requestParser;
	protected IEncoderService encoderService;

	public BaseLinkRestService(IRequestParser requestParser, IEncoderService encoderService) {
		this.requestParser = requestParser;
		this.encoderService = encoderService;
	}

	@Override
	public <T> DataResponse<T> selectById(Class<T> root, Object id) {
		return forSelect(root).byId(id).select();
	}

	@Override
	public <T> DataResponse<T> selectById(Class<T> root, Object id, UriInfo uriInfo) {
		return forSelect(root).with(uriInfo).byId(id).select();
	}

	@Override
	public <T> DataResponse<T> select(SelectQuery<T> query, UriInfo uriInfo) {
		return forSelect(query).with(uriInfo).select();
	}

	@Override
	public abstract <T> SelectBuilder<T> forSelect(Class<T> root);

	@Override
	public abstract <T> SelectBuilder<T> forSelect(SelectQuery<T> query);

	protected abstract <T> void doDelete(Class<T> root, Object id);

	protected abstract <T> T doInsert(UpdateResponse<T> request);

	protected abstract <T> T doUpdate(UpdateResponse<T> request);

	@Override
	public <T> SimpleResponse delete(Class<T> root, Object id) {
		doDelete(root, id);
		return new SimpleResponse(true);
	}

	@Override
	public <T> DataResponse<T> insert(Class<T> root, String objectData) {
		UpdateResponse<T> response = requestParser.parseInsert(new UpdateResponse<>(root), objectData);

		T object = doInsert(response);

		return encoderService.makeEncoder(response.withObject(object));
	}

	@Override
	public <T> DataResponse<T> update(Class<T> root, Object id, String objectData) {

		UpdateResponse<T> response = requestParser.parseUpdate(new UpdateResponse<>(root), id, objectData);

		// don't bother with processing if we didn't get any changes..
		if (!response.hasChanges()) {
			return response.withMessage("No changes");
		}

		T object = doUpdate(response);

		return encoderService.makeEncoder(response.withObject(object));
	}

}
