package com.nhl.link.rest.it.fixture.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E20Pojo;
import com.nhl.link.rest.runtime.fetcher.Fetcher;
import com.nhl.link.rest.runtime.fetcher.ParentAgnosticFetcher;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

@Path("e20")
public class E20Resource {

	private static final Logger LOGGER = LoggerFactory.getLogger(E20Resource.class);

	@Context
	private Configuration config;

	@GET
	@Path("parent-agnostic-strategy")
	public DataResponse<E20> getParentAgnostic(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E20.class).uri(uriInfo).listener(new ParentAgnosticFetcherListener())
				.select();
	}

	@GET
	@Path("parent-agnostic-strategy-fetcher-error")
	public DataResponse<E20> getParentAgnosticFetcherError(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E20.class).uri(uriInfo)
				.listener(new ParentAgnosticFetcherErrorsListener()).select();
	}

	public static abstract class AbstractFetcherListener {

		@SelectRequestParsed
		public void selectRequestParsed(SelectContext<?> context) {
			traverseEntityTree(context.getEntity());
		}

		@SuppressWarnings("unchecked")
		private void traverseEntityTree(ResourceEntity<?> entity) {
			if (E20Pojo.class.equals(entity.getType())) {
				LOGGER.info("Installing fetcher for E20Pojo");
				((ResourceEntity<E20Pojo>) entity).setFetcher(createFetcher());
			} else {
				entity.getChildren().forEach((n, c) -> traverseEntityTree(c));
			}
		}

		protected abstract Fetcher<E20Pojo> createFetcher();
	}

	public static class ParentAgnosticFetcherListener extends AbstractFetcherListener {

		static class E20ParentAgnosticFetcher implements ParentAgnosticFetcher<E20Pojo, E20, ObjectId> {

			@Override
			public Iterable<E20Pojo> fetch(SelectContext<E20Pojo> context) {
				LOGGER.info("Fetching E20Pojo's");
				List<E20Pojo> pojos = new ArrayList<>();

				IntStream.range(0, 5).forEach(i -> {
					E20Pojo p = new E20Pojo();
					p.setInteger(i);
					p.setString("s_" + i);

					pojos.add(p);
				});

				return pojos;
			}
		}

		protected Fetcher<E20Pojo> createFetcher() {
			return ParentAgnosticFetcher.builder(new E20ParentAgnosticFetcher()).toOneConnector(E20::setPojo)
					.idMapper(E20::getObjectId).parentIdMapper(E20Pojo::getParentId).build();
		}
	}

	public static class ParentAgnosticFetcherErrorsListener extends AbstractFetcherListener {

		static class E20ParentAgnosticErrorsFetcher implements ParentAgnosticFetcher<E20Pojo, E20, ObjectId> {

			@Override
			public Iterable<E20Pojo> fetch(SelectContext<E20Pojo> context) {
				LOGGER.info("Will intenationally throw an exception");
				throw new UnsupportedOperationException("Intentional exception...");
			}
		}

		protected Fetcher<E20Pojo> createFetcher() {
			return ParentAgnosticFetcher.builder(new E20ParentAgnosticErrorsFetcher()).toOneConnector(E20::setPojo)
					.idMapper(E20::getObjectId).parentIdMapper(E20Pojo::getParentId).build();
		}
	}

}
