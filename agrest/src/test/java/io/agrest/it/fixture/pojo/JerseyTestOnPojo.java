package io.agrest.it.fixture.pojo;

import io.agrest.runtime.AgRESTBuilder;
import io.agrest.runtime.adapter.AgRESTAdapter;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import org.apache.cayenne.di.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

public abstract class JerseyTestOnPojo extends JerseyTest {

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

        Feature lrFeature = new AgRESTBuilder().adapter(new AgRESTAdapter() {

            @Override
            public void contributeToRuntime(Binder binder) {
                binder.bind(SelectProcessorFactory.class).toProvider(PojoSelectProcessorFactoryProvider.class);
                binder.bind(PojoFetchStage.class).to(PojoFetchStage.class);
                binder.bind(PojoDB.class).toInstance(pojoDB);
            }

            @Override
            public void contributeToJaxRs(Collection<Feature> features) {
                // nothing to contribute...
            }
        }).build();

        Feature unitFeature = context -> {
            doAddResources(context);
            return true;
        };

        return new ResourceConfig().register(unitFeature).register(lrFeature);
    }

    protected abstract void doAddResources(FeatureContext context);
}
