package com.nhl.link.rest.runtime;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
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
		return select(root).byId(id).select();
	}

	@Override
	public <T> DataResponse<T> selectById(Class<T> root, Object id, UriInfo uriInfo) {
		return select(root).uri(uriInfo).byId(id).select();
	}

	@Override
	public <T> DataResponse<T> select(SelectQuery<T> query, UriInfo uriInfo) {
		return select(query).uri(uriInfo).select();
	}

	@Override
	public abstract <T> SelectBuilder<T> select(Class<T> root);

	@Override
	public abstract <T> SelectBuilder<T> select(SelectQuery<T> query);

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
		return delete(root).id(id).delete();
	}

	/**
	 * @since 1.3
	 */
	@Override
	public abstract <T> UpdateBuilder<T> create(Class<T> type);

	/**
	 * @since 1.3
	 */
	@Override
	public abstract <T> UpdateBuilder<T> createOrUpdate(Class<T> type);

	/**
	 * @since 1.3
	 */
	@Override
	public abstract <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type);

	/**
	 * @since 1.7
	 */
	@Override
	public abstract <T> UpdateBuilder<T> idempotentFullSync(Class<T> type);

	/**
	 * @since 1.3
	 */
	@Override
	public abstract <T> UpdateBuilder<T> update(Class<T> type);

	/**
	 * @since 1.4
	 */
	@Override
	public abstract <T> DeleteBuilder<T> delete(Class<T> root);
}
