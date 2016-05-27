package com.nhl.link.rest.it.fixture.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.it.fixture.cayenne.E22;
import com.nhl.link.rest.it.fixture.cayenne.E22Pojo;
import com.nhl.link.rest.runtime.fetcher.Fetcher;
import com.nhl.link.rest.runtime.fetcher.FetcherBuilder;
import com.nhl.link.rest.runtime.fetcher.PerParentFetcher;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

@Path("e22")
public class E22Resource {

	private static final Logger LOGGER = LoggerFactory.getLogger(E22Resource.class);

	@Context
	private Configuration config;

	@GET
	@Path("parent-aware-strategy")
	public DataResponse<E22> getParentAware(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E22.class).uri(uriInfo).listener(new ParentAwareFetcherListener())
				.select();
	}

	@GET
	@Path("parent-agnostic-strategy")
	public DataResponse<E22> getParentAgnostic(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E22.class).uri(uriInfo).listener(new ParentAgnosticFetcherListener())
				.select();
	}

	@GET
	@Path("parent-agnostic-strategy-fetcher-error")
	public DataResponse<E22> getParentAgnosticFetcherError(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E22.class).uri(uriInfo)
				.listener(new ParentAgnosticFetcherErrorsListener()).select();
	}

	@GET
	@Path("per-parent-strategy")
	public DataResponse<E22> getPerParent(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E22.class).uri(uriInfo).listener(new PerParentFetcherListener())
				.select();
	}

	@GET
	@Path("per-parent-strategy-fetcher-error")
	public DataResponse<E22> getPerParentFetcherError(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E22.class).uri(uriInfo).listener(new PerParentFetcherErrorsListener())
				.select();
	}

	public static abstract class AbstractFetcherListener {

		@SelectRequestParsed
		public void selectRequestParsed(SelectContext<?> context) {
			traverseEntityTree(context.getEntity());
		}

		@SuppressWarnings("unchecked")
		private void traverseEntityTree(ResourceEntity<?> entity) {
			if (E22Pojo.class.equals(entity.getType())) {
				LOGGER.info("Installing fetcher for E22Pojo");
				((ResourceEntity<E22Pojo>) entity).setFetcher(createFetcher());
			} else {
				entity.getChildren().forEach((n, c) -> traverseEntityTree(c));
			}
		}

		protected abstract Fetcher<E22Pojo, E22> createFetcher();
	}

	public static class ParentAwareFetcherListener extends AbstractFetcherListener {

		static class E22ParentAwareFetcher implements Fetcher<E22Pojo, E22> {

			@Override
			public Iterable<E22Pojo> fetch(SelectContext<E22Pojo> context, Iterable<E22> parents) {
				LOGGER.info("Fetching E22Pojo's");
				List<E22Pojo> pojos = new ArrayList<>();

				parents.forEach(parent -> {

					int pk = Cayenne.intPKForObject(parent);

					E22Pojo p = new E22Pojo();
					p.setInteger(pk);
					p.setString("s_" + pk);

					pojos.add(p);
				});

				return pojos;
			}
		}

		protected Fetcher<E22Pojo, E22> createFetcher() {
			return FetcherBuilder.batch(new E22ParentAwareFetcher(), ObjectId.class).toOneConnector(E22::setPojo)
					.parentKeyMapper(E22::getObjectId).childKeyMapper(E22Pojo::getParentId).build();
		}
	}

	public static class ParentAgnosticFetcherListener extends AbstractFetcherListener {

		static class E22ParentAgnosticFetcher implements Fetcher<E22Pojo, E22> {

			@Override
			public Iterable<E22Pojo> fetch(SelectContext<E22Pojo> context, Iterable<E22> parents) {
				LOGGER.info("Fetching E22Pojo's");
				List<E22Pojo> pojos = new ArrayList<>();

				IntStream.range(0, 5).forEach(i -> {
					E22Pojo p = new E22Pojo();
					p.setInteger(i);
					p.setString("s_" + i);

					pojos.add(p);
				});

				return pojos;
			}
		}

		protected Fetcher<E22Pojo, E22> createFetcher() {
			return FetcherBuilder.batch(new E22ParentAgnosticFetcher(), ObjectId.class).toOneConnector(E22::setPojo)
					.parentKeyMapper(E22::getObjectId).childKeyMapper(E22Pojo::getParentId).build();
		}
	}

	public static class ParentAgnosticFetcherErrorsListener extends AbstractFetcherListener {

		static class E22ParentAgnosticErrorsFetcher implements Fetcher<E22Pojo, E22> {

			@Override
			public Iterable<E22Pojo> fetch(SelectContext<E22Pojo> context, Iterable<E22> parents) {
				LOGGER.info("Will intenationally throw an exception");
				throw new UnsupportedOperationException("Intentional exception...");
			}
		}

		protected Fetcher<E22Pojo, E22> createFetcher() {
			return FetcherBuilder.batch(new E22ParentAgnosticErrorsFetcher(), ObjectId.class)
					.toOneConnector(E22::setPojo).parentKeyMapper(E22::getObjectId).childKeyMapper(E22Pojo::getParentId)
					.build();
		}
	}

	public static class PerParentFetcherListener extends AbstractFetcherListener {

		static class E22PerParentFetcher implements PerParentFetcher<E22Pojo, E22> {

			@Override
			public Iterable<E22Pojo> fetch(SelectContext<E22Pojo> context, E22 parent) {

				int id = Cayenne.intPKForObject(parent);
				LOGGER.info("Fetching E22Pojo's for parent: " + id);
				List<E22Pojo> pojos = new ArrayList<>();

				E22Pojo p = new E22Pojo();
				p.setInteger(id);
				p.setString("s_" + id);

				pojos.add(p);

				return pojos;
			}
		}

		protected Fetcher<E22Pojo, E22> createFetcher() {
			return FetcherBuilder.perParent(new E22PerParentFetcher()).toOneConnector(E22::setPojo).build();
		}
	}

	public static class PerParentFetcherErrorsListener extends AbstractFetcherListener {

		static class E22PerParentFetcherErrors implements PerParentFetcher<E22Pojo, E22> {

			@Override
			public Iterable<E22Pojo> fetch(SelectContext<E22Pojo> context, E22 parent) {
				LOGGER.info("Will intenationally throw an exception");
				throw new UnsupportedOperationException("Intentional exception...");
			}
		}

		protected Fetcher<E22Pojo, E22> createFetcher() {
			return FetcherBuilder.perParent(new E22PerParentFetcherErrors()).toOneConnector(E22::setPojo).build();
		}
	}

}
