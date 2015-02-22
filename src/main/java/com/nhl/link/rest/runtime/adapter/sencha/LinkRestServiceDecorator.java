package com.nhl.link.rest.runtime.adapter.sencha;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.runtime.ILinkRestService;

/**
 * @since 1.7
 */
public class LinkRestServiceDecorator implements ILinkRestService {

	private ILinkRestService delegate;

	public LinkRestServiceDecorator(ILinkRestService delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T> DataResponse<T> selectById(Class<T> root, Object id) {
		return delegate.selectById(root, id);
	}

	@Override
	public <T> DataResponse<T> selectById(Class<T> root, Object id, UriInfo uriInfo) {
		return delegate.selectById(root, id, uriInfo);
	}

	@Override
	public <T> DataResponse<T> select(SelectQuery<T> query, UriInfo uriInfo) {
		return delegate.select(query, uriInfo);
	}

	@Override
	public <T> SelectBuilder<T> select(Class<T> root) {
		return delegate.select(root);
	}

	@Deprecated
	@Override
	public <T> SelectBuilder<T> forSelect(Class<T> root) {
		// not delegating deprecated method as it will make interception harder
		return select(root);
	}

	@Deprecated
	@Override
	public <T> SelectBuilder<T> forSelect(SelectQuery<T> query) {
		// not delegating deprecated method as it will make interception harder
		return select(query);
	}

	@Override
	public <T> SelectBuilder<T> select(SelectQuery<T> query) {
		return delegate.select(query);
	}

	@Override
	public SimpleResponse delete(Class<?> root, Object id) {
		return delegate.delete(root, id);
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship) {
		return delegate.unrelate(root, sourceId, relationship);
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship) {
		return delegate.unrelate(root, sourceId, relationship);
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, String relationship, Object targetId) {
		return delegate.unrelate(root, sourceId, relationship, targetId);
	}

	@Override
	public SimpleResponse unrelate(Class<?> root, Object sourceId, Property<?> relationship, Object targetId) {
		return delegate.unrelate(root, sourceId, relationship, targetId);
	}

	@Override
	public <T> UpdateBuilder<T> update(Class<T> type) {
		return delegate.update(type);
	}

	@Override
	public <T> UpdateBuilder<T> create(Class<T> type) {
		return delegate.create(type);
	}

	@Override
	public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
		return delegate.createOrUpdate(type);
	}

	@Override
	public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
		return delegate.idempotentCreateOrUpdate(type);
	}

	@Override
	public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
		return delegate.idempotentFullSync(type);
	}

	@Override
	public <T> DeleteBuilder<T> delete(Class<T> root) {
		return delegate.delete(root);
	}

}
