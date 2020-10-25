package io.agrest.openapi.unit;

import io.agrest.runtime.AgBuilder;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContext;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.HashSet;
import java.util.Set;

public class OpenAPIBuilder {

    private static final String OAPI_TEST_CONTEXT_ID = "ag.test.context";

    private final Set<String> packages;
    private final Set<String> classes;

    public OpenAPIBuilder() {
        this.packages = new HashSet<>();
        this.classes = new HashSet<>();
    }

    public OpenAPIBuilder addClass(Class<?> type) {
        classes.add(type.getName());
        return this;
    }

    public OpenAPIBuilder addPackage(Class<?> typeInPackage) {
        return addPackage(typeInPackage.getPackage().getName());
    }

    public OpenAPIBuilder addPackage(String packageName) {
        packages.add(packageName);
        return this;
    }

    public OpenAPI build() {

        // The side effect of creating AgRuntime is adding Swagger ModelConverters to the static Swagger collection
        // TODO: cleanup static vars after the test
        new AgBuilder().build();

        SwaggerConfiguration swaggerConfig = new SwaggerConfiguration()
                .resourcePackages(packages)
                .resourceClasses(classes);

        try {
            return new JaxrsOpenApiContext()
                    .id(OAPI_TEST_CONTEXT_ID)
                    .openApiConfiguration(swaggerConfig)
                    .init()
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException("Error building OpenAPI model", e);
        }
    }
}
