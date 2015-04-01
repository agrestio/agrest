package com.nhl.link.rest.it.fixture.pojo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.Before;
import org.junit.BeforeClass;

import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P2;
import com.nhl.link.rest.it.fixture.pojo.model.P3;
import com.nhl.link.rest.it.fixture.pojo.model.P4;
import com.nhl.link.rest.it.fixture.pojo.model.P6;
import com.nhl.link.rest.it.fixture.resource.PojoResource;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.runtime.LinkRestBuilder;

public class JerseyTestOnPojo extends JerseyTest {

	protected static List<LrEntity<?>> pojoModels;

	// using in-memory key/value "database" to store POJOs
	protected static PojoDB pojoDB;

	@BeforeClass
	public static void setUpClass() throws IOException, SQLException {

		pojoModels = new ArrayList<>();
		pojoModels = Arrays.asList(LrEntityBuilder.build(P1.class), LrEntityBuilder.build(P2.class), LrEntityBuilder
				.build(P3.class), LrEntityBuilder.build(P4.class), LrEntityBuilder.builder(P6.class).build());
		pojoDB = new PojoDB();
	}

	public JerseyTestOnPojo() throws TestContainerException {
		super(new InMemoryTestContainerFactory());
	}

	@Before
	public void resetData() {
		pojoDB.clear();
	}

	@Override
	public Application configure() {

		Feature lrFeature = new LinkRestBuilder().linkRestService(PojoLinkRestService.class).extraEntities(pojoModels)
				.build();

		Feature unitFeature = new Feature() {

			@Override
			public boolean configure(FeatureContext context) {
				context.register(PojoResource.class);
				return true;
			}
		};

		return new ResourceConfig().register(unitFeature).register(lrFeature);
	}

}
