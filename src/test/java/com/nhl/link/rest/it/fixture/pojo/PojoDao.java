package com.nhl.link.rest.it.fixture.pojo;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DeleteBuilder;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.DefaultSelectBuilder;
import com.nhl.link.rest.runtime.dao.EntityDao;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class PojoDao<T> implements EntityDao<T> {

	private Processor<SelectContext<?>> selectProcessor;

	private Class<T> type;

	public PojoDao(Class<T> type, Processor<SelectContext<?>> selectProcessor) {
		this.type = type;
		this.selectProcessor = selectProcessor;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public SelectBuilder<T> forSelect() {
		return new DefaultSelectBuilder<T>(type, selectProcessor);
	}

	@Override
	public SelectBuilder<T> forSelect(SelectQuery<T> query) {
		throw new UnsupportedOperationException();
	}

	@Override
	public UpdateBuilder<T> update() {
		throw new UnsupportedOperationException();
	}

	@Override
	public UpdateBuilder<T> create() {
		throw new UnsupportedOperationException();
	}

	@Override
	public UpdateBuilder<T> createOrUpdate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public UpdateBuilder<T> idempotentCreateOrUpdate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public UpdateBuilder<T> idempotentFullSync() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DeleteBuilder<T> delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SimpleResponse unrelate(Object sourceId, String relationship) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SimpleResponse unrelate(Object sourceId, String relationship, Object targetId) {
		throw new UnsupportedOperationException();
	}

}
