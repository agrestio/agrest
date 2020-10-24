package io.agrest.openapi;

import io.agrest.pojo.model.P1;
import io.agrest.runtime.AgBuilder;
import io.agrest.unit.AgPojoTester;
import io.bootique.jetty.JettyModule;
import io.bootique.junit5.BQTest;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContext;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.OpenApiContextLocator;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.AfterAll;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

@BQTest
public abstract class OpenAPITest {

    private static final String OAPI_TEST_CONTEXT_ID = "ag.test.context";

    protected static AgPojoTester.Builder tester(Class<?>... resources) {
        return AgPojoTester
                .builder()
                .bqModule(b -> JettyModule.extend(b).addServlet(OapiServlet.class))
                .agCustomizer(OpenAPITest::enableTestModels)
                .resources(resources);
    }

    private static AgBuilder enableTestModels(AgBuilder builder) {
        return builder.module(b ->
                AgSwaggerModule.contributeEntityPackages(b).add(P1.class.getPackage().getName()));
    }

    @AfterAll
    static void cleanOpenAPI() {
        OpenApiContextLocator.getInstance().putOpenApiContext(OAPI_TEST_CONTEXT_ID, null);
    }

    @WebServlet(name = "oapi", urlPatterns = "/oapi/*")
    static class OapiServlet extends HttpServlet {

        private OpenAPI oapi;

        @Override
        public void init(ServletConfig config) throws ServletException {
            super.init(config);

            List<String> packages = asList(P1.class.getPackage().getName(), OpenAPITest.class.getPackage().getName());
            SwaggerConfiguration swaggerConfig = new SwaggerConfiguration().resourcePackages(new HashSet<>(packages));

            try {
                this.oapi = new JaxrsOpenApiContext()
                        .id(OAPI_TEST_CONTEXT_ID)
                        .openApiConfiguration(swaggerConfig)
                        .init()
                        .read();
            } catch (OpenApiConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setStatus(200);
            resp.setContentType("application/json");

            try (PrintWriter pw = resp.getWriter()) {
                pw.write(Json.mapper().writeValueAsString(oapi));
            }
        }
    }
}
