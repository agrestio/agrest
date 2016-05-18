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
import com.nhl.link.rest.runtime.fetcher.NoParentFetcher;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

@Path("e20")
public class E20Resource {

	private static final Logger LOGGER = LoggerFactory.getLogger(E20Resource.class);

	@Context
	private Configuration config;

	private FetcherInstallListener listener = new FetcherInstallListener();

	@GET
	public DataResponse<E20> get(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E20.class).uri(uriInfo).listener(listener).select();
	}

	public static class FetcherInstallListener {

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

		private Fetcher<E20Pojo> createFetcher() {
			return NoParentFetcher.builder(new E20Fetcher()).toOneParentConnector(E20::setPojo)
					.idMapper(E20::getObjectId).parentIdMapper(E20Pojo::getParentId).build();
		}
	}

	static class E20Fetcher implements NoParentFetcher<E20Pojo, E20, ObjectId> {

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
}
