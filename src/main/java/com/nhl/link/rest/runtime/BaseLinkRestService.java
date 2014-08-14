package com.nhl.link.rest.runtime;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
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

	@Override
	public abstract <T> SelectBuilder<T> forSelectRelated(Class<?> root, Object rootId, Property<T> relationship);

	@Override
	public abstract SelectBuilder<?> forSelectRelated(Class<?> root, Object rootId, String relationship);

	protected abstract void doDelete(Class<?> root, Object id);

	protected abstract <T> T doInsert(UpdateResponse<T> request);

	protected abstract <T> T doUpdate(UpdateResponse<T> request);

	/**
	 * @since 1.2
	 */
	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship) {
		return unrelate(root, sourceId, relationship.getName());
	}

	/**
	 * @since 1.2
	 */
	@Override
	public abstract SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship);

	/**
	 * @since 1.2
	 */
	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship, Object targetId) {
		return unrelate(root, sourceId, relationship.getName(), targetId);
	}

	/**
	 * @since 1.2
	 */
	@Override
	public abstract SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship, Object targetId);

	@Override
	public SimpleResponse delete(Class<?> root, Object id) {
		doDelete(root, id);
		return new SimpleResponse(true);
	}

	@Override
	public <T> DataResponse<T> insert(Class<T> root, String objectData) {
		UpdateResponse<T> response = requestParser.parseInsert(new UpdateResponse<>(root), objectData);

		T object = doInsert(response);

		return encoderService.makeEncoder(response.withObject(object)).withStatus(Status.CREATED);
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

	@Override
	@Deprecated
	public <T> DataResponse<T> relate(Class<?> sourceType, Object sourceId, Property<T> relationship, Object targetId,
			String targetData) {
		return insertOrUpdateRelated(sourceType, sourceId, relationship, targetId, targetData);
	}

	@Override
	@Deprecated
	public DataResponse<?> relate(Class<?> sourceType, Object sourceId, String relationship, Object targetId,
			String targetData) {
		return insertOrUpdateRelated(sourceType, sourceId, relationship, targetId, targetData);
	}

	@Override
	@Deprecated
	public <T> DataResponse<T> relateNew(Class<?> sourceType, Object sourceId, Property<T> relationship,
			String targetData) {
		return insertRelated(sourceType, sourceId, relationship, targetData);
	}

	@Override
	@Deprecated
	public DataResponse<?> relateNew(Class<?> sourceType, Object sourceId, String relationship, String targetData) {
		return insertRelated(sourceType, sourceId, relationship, targetData);
	}

	/**
	 * @since 1.3
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> DataResponse<T> insertRelated(Class<?> sourceType, Object sourceId, Property<T> relationship,
			String targetData) {
		return (DataResponse<T>) insertRelated(sourceType, sourceId, relationship.getName(), targetData);
	}

	/**
	 * @since 1.3
	 */
	@Override
	public abstract DataResponse<?> insertRelated(Class<?> sourceType, Object sourceId, String relationship,
			String targetData);

	/**
	 * @since 1.3
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> DataResponse<T> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, Property<T> relationship,
			Object targetId, String targetData) {
		return (DataResponse<T>) insertOrUpdateRelated(sourceType, sourceId, relationship.getName(), targetId,
				targetData);
	}

	/**
	 * @since 1.3
	 */
	@Override
	public abstract DataResponse<?> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, String relationship,
			Object targetId, String targetData);

	/**
	 * @since 1.3
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> DataResponse<T> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, Property<T> relationship,
			String targetData) {
		return (DataResponse<T>) insertOrUpdateRelated(sourceType, sourceId, relationship.getName(), targetData);
	}

	/**
	 * @since 1.3
	 */
	@Override
	public abstract DataResponse<?> insertOrUpdateRelated(Class<?> sourceType, Object sourceId, String relationship,
			String targetData);
}
