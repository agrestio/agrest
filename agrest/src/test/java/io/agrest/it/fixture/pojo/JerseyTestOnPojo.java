package io.agrest.it.fixture.pojo;

import io.agrest.AgModuleProvider;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;
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

public abstract class JerseyTestOnPojo extends JerseyTest {

    // using in-memory key/value "database" to store POJOs
    protected static PojoDB pojoDB;

    public JerseyTestOnPojo() throws TestContainerException {
        super(new InMemoryTestContainerFactory());
    }

    @BeforeClass
    public static void setUpClass() throws IOException, SQLException {
        pojoDB = new PojoDB();
    }

    @Before
    public void resetData() {
        pojoDB.clear();
    }

    @Override
    public Application configure() {

        Feature agFeature = new AgBuilder().module(new PojoTestModuleProvider()).build();

        Feature unitFeature = context -> {
            doAddResources(context);
            return true;
        };

        return new ResourceConfig().register(unitFeature).register(agFeature);
    }

    protected abstract void doAddResources(FeatureContext context);

    static class PojoTestModuleProvider implements AgModuleProvider {

        @Override
        public Module module() {
            return new PojoTestModule();
        }

        @Override
        public Class<? extends Module> moduleType() {
            return PojoTestModule.class;
        }
    }

    public static class PojoTestModule implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(SelectProcessorFactory.class).toProvider(PojoSelectProcessorFactoryProvider.class);
            binder.bind(PojoFetchStage.class).to(PojoFetchStage.class);
            binder.bind(PojoDB.class).toInstance(pojoDB);
        }
    }
}
