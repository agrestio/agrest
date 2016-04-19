package com.nhl.link.rest.it.fixture.pojo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.cayenne.di.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.Before;
import org.junit.BeforeClass;

import com.nhl.link.rest.it.fixture.resource.PojoResource;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.processor.IProcessorFactory;

public class JerseyTestOnPojo extends JerseyTest {

	// using in-memory key/value "database" to store POJOs
	protected static PojoDB pojoDB;

	@BeforeClass
	public static void setUpClass() throws IOException, SQLException {
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

		Feature lrFeature = new LinkRestBuilder().adapter(new LinkRestAdapter() {

			@Override
			public void contributeToRuntime(Binder binder) {
				binder.bind(IProcessorFactory.class).to(PojoProcessorFactory.class);
			}

			@Override
			public void contributeToJaxRs(Collection<Feature> features) {
				// nothing to contribute...
			}
		}).build();

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
