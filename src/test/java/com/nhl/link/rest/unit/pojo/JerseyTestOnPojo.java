package com.nhl.link.rest.unit.pojo;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.map.DataMap;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.meta.DataMapBuilder;
import com.nhl.link.rest.unit.DerbyManager;
import com.nhl.link.rest.unit.pojo.model.P1;
import com.nhl.link.rest.unit.pojo.model.P2;
import com.nhl.link.rest.unit.pojo.model.P3;
import com.nhl.link.rest.unit.pojo.model.P4;
import com.nhl.link.rest.unit.pojo.model.P6;
import com.nhl.link.rest.unit.resource.PojoResource;

public class JerseyTestOnPojo extends JerseyTest {
	protected static DerbyManager derbyAssembly;
	protected static ServerRuntime runtime;
	protected static DataMap pojosMap;

	// using in-memory key/value "database" to store POJOs
	protected static PojoDB pojoDB;

	@BeforeClass
	public static void setUpClass() throws IOException, SQLException {
		derbyAssembly = new DerbyManager("target/derby");

		runtime = new ServerRuntime("cayenne-linkrest-tests.xml");
		pojosMap = DataMapBuilder.newBuilder("__").addEntities(P1.class, P2.class, P3.class, P4.class)
				.addEntity(P6.class).withId("stringId").toDataMap();
		pojoDB = new PojoDB();
	}

	@AfterClass
	public static void tearDownClass() throws IOException, SQLException {
		runtime.shutdown();
		runtime = null;

		derbyAssembly.shutdown();
		derbyAssembly = null;
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

		Feature lrFeature = new LinkRestBuilder().linkRestService(PojoLinkRestService.class)
				.nonPersistentEntities(pojosMap).build().getFeature();

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
