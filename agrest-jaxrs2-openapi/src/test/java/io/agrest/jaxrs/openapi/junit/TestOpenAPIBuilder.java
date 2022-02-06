package io.agrest.jaxrs.openapi.junit;

import io.agrest.jaxrs2.AgJaxrsFeature;
import io.agrest.jaxrs.openapi.AgSwaggerModule;
import io.agrest.runtime.AgRuntimeBuilder;
import io.agrest.runtime.AgRuntime;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContext;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.HashSet;
import java.util.Set;

public class TestOpenAPIBuilder {

    private static final String OAPI_TEST_CONTEXT_ID = "ag.test.context";

    private final Set<String> packages;
    private final Set<String> classes;

    public TestOpenAPIBuilder() {
        this.packages = new HashSet<>();
        this.classes = new HashSet<>();
    }

    public TestOpenAPIBuilder addClass(Class<?> type) {
        classes.add(type.getName());
        return this;
    }

    public TestOpenAPIBuilder addPackage(Class<?> typeInPackage) {
        return addPackage(typeInPackage.getPackage().getName());
    }

    public TestOpenAPIBuilder addPackage(String packageName) {
        packages.add(packageName);
        return this;
    }

    public OpenAPI build() {

        // The side effect of creating AgRuntime is adding Swagger ModelConverters to the static Swagger collection
        // and with the right set of packages
        // TODO: cleanup static vars after the test
        AgRuntimeBuilder builder = AgRuntime.builder();
        packages.forEach(p -> builder.module(b -> AgSwaggerModule.contributeEntityPackages(b).add(p)));
        AgRuntime runtime = builder.build();

        // even though we don't start a JAX-RS runtime, the side effect of initializing AgJaxrsFeature is invoking
        // AgSwaggerModuleInstaller
        AgJaxrsFeature.builder().runtime(runtime).build();

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
