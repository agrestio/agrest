package com.nhl.link.rest.runtime.adapter.sencha;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.runtime.ILinkRestService;

/**
 * Customizes some defaults of the stock {@link ILinkRestService} to follow
 * Sencha conventions.
 * 
 * @since 1.7
 */
public class SenchaLinkRestService extends LinkRestServiceDecorator {

	public SenchaLinkRestService(@Inject ILinkRestService delegate) {
		super(delegate);
	}

	@Override
	public <T> UpdateBuilder<T> create(Class<T> type) {
		return super.create(type).includeData();
	}

	@Override
	public <T> UpdateBuilder<T> createOrUpdate(Class<T> type) {
		return super.createOrUpdate(type).includeData();
	}

	@Override
	public <T> UpdateBuilder<T> update(Class<T> type) {
		return super.update(type).includeData();
	}

	@Override
	public <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type) {
		return super.idempotentCreateOrUpdate(type).includeData();
	}

	@Override
	public <T> UpdateBuilder<T> idempotentFullSync(Class<T> type) {
		return super.idempotentFullSync(type).includeData();
	}

}
