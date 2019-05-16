package io.agrest.it.fixture;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.agrest.AgModuleProvider;
import io.agrest.it.fixture.pojo.PojoDB;
import io.agrest.it.fixture.pojo.PojoFetchStage;
import io.agrest.it.fixture.pojo.PojoSelectProcessorFactoryProvider;
import io.agrest.it.fixture.pojo.model.P1;
import io.agrest.it.fixture.pojo.model.P2;
import io.agrest.it.fixture.pojo.model.P4;
import io.agrest.it.fixture.pojo.model.P6;
import io.agrest.it.fixture.pojo.model.P8;
import io.agrest.it.fixture.pojo.model.P9;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.bootique.BQRuntime;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.JerseyModuleExtender;
import io.bootique.test.junit.BQTestFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.Before;
import org.junit.ClassRule;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BQJerseyTestOnPojo {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    // in-memory key/value "database" to store POJOs
    protected static PojoDB POJO_DB;
    private static BQRuntime TEST_RUNTIME;

    protected static void startTestRuntime(Class<?>... resources) {
        startTestRuntime(b -> b, resources);
    }

    protected static void startTestRuntime(UnaryOperator<AgBuilder> customizer, Class<?>... resources) {

        POJO_DB = new PojoDB();

        Function<AgBuilder, AgBuilder> customizerChain = customizer.compose(BQJerseyTestOnPojo::customizeForPojo);

        TEST_RUNTIME = TEST_FACTORY.app("-s", "-c", "classpath:io/agrest/it/fixture/pojoserver.yml")
                .autoLoadModules()
                .module(new AgModule(customizerChain))
                .module(b -> addResources(JerseyModule.extend(b), resources).addFeature(AgRuntime.class))
                .createRuntime();

        TEST_RUNTIME.run();
    }

    private static AgBuilder customizeForPojo(AgBuilder builder) {
        return builder.module(new PojoTestModuleProvider());
    }

    private static JerseyModuleExtender addResources(JerseyModuleExtender extender, Class<?>... resources) {
        for (Class<?> c : resources) {
            extender.addResource(c);
        }

        return extender;
    }

    @Before
    public void resetData() {
        POJO_DB.clear();
    }

    protected ResponseAssertions onSuccess(Response response) {
        return onResponse(response).wasSuccess();
    }

    protected ResponseAssertions onResponse(Response response) {
        return new ResponseAssertions(response);
    }

    protected String urlEnc(String queryParam) {
        try {
            // URLEncoder replaces spaces with "+"... Those are not decoded
            // properly by Jersey in 'uriInfo.getQueryParameters()' (TODO: why?)
            return URLEncoder.encode(queryParam, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {

            // unexpected... we know that UTF-8 is present
            throw new RuntimeException(e);
        }
    }

    protected WebTarget target(String path) {
        return ClientBuilder.newClient().target("http://127.0.0.1:8080/").path(path);
    }

    protected Map<Object, P1> p1() {
        return POJO_DB.bucketForType(P1.class);
    }

    protected Map<Object, P2> p2() {
        return POJO_DB.bucketForType(P2.class);
    }

    protected Map<Object, P4> p4() {
        return POJO_DB.bucketForType(P4.class);
    }

    protected Map<Object, P6> p6() {
        return POJO_DB.bucketForType(P6.class);
    }

    protected Map<Object, P8> p8() {
        return POJO_DB.bucketForType(P8.class);
    }

    protected Map<Object, P9> p9() {
        return POJO_DB.bucketForType(P9.class);
    }

    public static class AgModule implements Module {

        private Function<AgBuilder, AgBuilder> agCustomizer;

        public AgModule(Function<AgBuilder, AgBuilder> agCustomizer) {
            this.agCustomizer = agCustomizer;
        }

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        @Singleton
        AgRuntime createRuntime(ServerRuntime runtime) {
            return agCustomizer.apply(new AgBuilder().cayenneRuntime(runtime)).build();
        }
    }

    static class PojoTestModuleProvider implements AgModuleProvider {

        @Override
        public org.apache.cayenne.di.Module module() {
            return new PojoTestModule();
        }

        @Override
        public Class<? extends org.apache.cayenne.di.Module> moduleType() {
            return PojoTestModule.class;
        }
    }

    public static class PojoTestModule implements org.apache.cayenne.di.Module {

        @Override
        public void configure(org.apache.cayenne.di.Binder binder) {
            binder.bind(SelectProcessorFactory.class).toProvider(PojoSelectProcessorFactoryProvider.class);
            binder.bind(PojoFetchStage.class).to(PojoFetchStage.class);
            binder.bind(PojoDB.class).toInstance(POJO_DB);
        }
    }
}
