package com.nhl.link.rest;

import javax.ws.rs.core.Configuration;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;

/**
 * Defines static methods to start LinkRest request processor builders. Users of
 * this class must inject {@link Configuration} instance to pass to the static
 * methods.
 * 
 * @since 1.14
 */
public class LinkRest {

	public static <T> SelectBuilder<T> select(Class<T> root, Configuration config) {
		return service(config).select(root);
	}

	public static <T> SelectBuilder<T> select(SelectQuery<T> query, Configuration config) {
		return service(config).select(query);
	}

	public static <T> UpdateBuilder<T> create(Class<T> type, Configuration config) {
		return service(config).create(type);
	}

	public static <T> UpdateBuilder<T> createOrUpdate(Class<T> type, Configuration config) {
		return service(config).createOrUpdate(type);
	}

	public static <T> DeleteBuilder<T> delete(Class<T> root, Configuration config) {
		return service(config).delete(root);
	}

	public static <T> UpdateBuilder<T> idempotentCreateOrUpdate(Class<T> type, Configuration config) {
		return service(config).idempotentCreateOrUpdate(type);
	}

	public static <T> UpdateBuilder<T> idempotentFullSync(Class<T> type, Configuration config) {
		return service(config).idempotentFullSync(type);
	}

	public static <T> UpdateBuilder<T> update(Class<T> type, Configuration config) {
		return service(config).update(type);
	}

	public static <T> MetadataBuilder<T> metadata(Class<T> entityClass, Configuration config) {
		return service(config).metadata(entityClass);
	}

	/**
	 * Returns {@link ILinkRestService} bound to a given JAX RS configuration.
	 * ILinkRestService is the main engine behind all the operations in
	 * LinkRest, however you would rarely need to use it directly. Instead use
	 * other static methods defined in this class to start processor chains for
	 * LinkRest requests.
	 */
	public static ILinkRestService service(Configuration config) {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}
}
