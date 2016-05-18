package com.nhl.link.rest.it.fixture.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E20Pojo;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneIdMapper;
import com.nhl.link.rest.runtime.fetcher.Fetcher;
import com.nhl.link.rest.runtime.fetcher.TreeConnectingFetcher;
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

		private void traverseEntityTree(ResourceEntity<?> entity) {
			if (E20Pojo.class.equals(entity.getType())) {
				LOGGER.info("Installing fetcher for E20Pojo");
				entity.setFetcher(createConnectingFetcher());
			} else {
				entity.getChildren().forEach((n, c) -> traverseEntityTree(c));
			}
		}

		private Fetcher createConnectingFetcher() {
			return TreeConnectingFetcher.builder().dataFetcher(new E20Fetcher()).idMapper(CayenneIdMapper.instance())
					.toOneParentConnector(this::connectE20ToE20Pojo).build();
		}

		private void connectE20ToE20Pojo(Object parent, Object child) {
			((E20) parent).setPojo((E20Pojo) child);
		}
	}

	static class E20Fetcher implements Fetcher {

		@SuppressWarnings("unchecked")
		@Override
		public <T> Iterable<T> fetch(SelectContext<T> context, Iterable<?> parentResult) {
			LOGGER.info("Fetching E20Pojo's");
			List<E20Pojo> pojos = new ArrayList<>();
			return (Iterable<T>) pojos;
		}
	}
}
