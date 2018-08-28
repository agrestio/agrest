package com.nhl.link.rest.swagger.codefirst.mavenplugin;

import com.nhl.link.rest.swagger.codefirst.CayenneDataObjectConverter;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.GenericOpenApiScanner;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


/**
 * @author vyarmolovich
 * 8/13/18
 */

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public class ProtocolMojo extends AbstractMojo {

    @Parameter
    private Info info;

    /**
     * A list of ResourceSource.
     */
    @Parameter
    private List<ResourceSource> resourceSources;

    /**
     * A list of ComponentSource.
     */
    @Parameter
    private List<ComponentSource> componentSources;


    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // generates .json and .yaml both api resources and components definition
        if (resourceSources != null) {
            for (ResourceSource resourceSource : resourceSources) {

                OpenAPI openAPI = processResource(resourceSource);

                String outputName = resourceSource.getOutputName();
                if (outputName != null && !outputName.isEmpty()) {
                    try {
                        // writes result as both .json and .yaml
                        Files.write(Paths.get(outputName + ".json"),
                                Json.pretty().writeValueAsBytes(openAPI));

                        Files.write(Paths.get(outputName+ ".yaml"),
                                Yaml.pretty().writeValueAsBytes(openAPI));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // generates components definition, if defined
        if (componentSources != null) {
            for (ComponentSource componentSource : componentSources) {

                OpenAPI openAPI = processComponent(componentSource);

                String outputName = componentSource.getOutputName();
                if (outputName != null && !outputName.isEmpty()) {
                    try {
                        // writes result as both .json and .yaml
                        Files.write(Paths.get(outputName + ".json"),
                                Json.pretty().writeValueAsBytes(openAPI));

                        Files.write(Paths.get(outputName + ".yaml"),
                                Yaml.pretty().writeValueAsBytes(openAPI));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ComponentSource componentSource = componentSources.iterator().next();

    }

    private OpenAPI processResource(ResourceSource resourceSource) throws MojoFailureException {

        validateResourceSource(resourceSource);

        OpenAPI oas = new OpenAPI();
        oas.info(info);

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourceClasses(resourceSource.getSrcClasses())
                .resourcePackages(resourceSource.getSrcPackages())
                .readerClass(Reader.class.getName());

        try {
            OpenAPI openAPI = new GenericOpenApiContextBuilder()
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();

            System.out.println("###### ");

            Json.prettyPrint(openAPI);

            return openAPI;

        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private OpenAPI processComponent(ComponentSource componentSource) throws MojoFailureException {
        validateComponentSource(componentSource);

        Components components = new Components();

        OpenAPI openAPI = new OpenAPI();
        openAPI.info(info);

        // scan and parse Parameters
        SwaggerConfiguration oasConfigParameters = new SwaggerConfiguration()
                .openAPI(openAPI)
                .prettyPrint(true)
                .resourceClasses(componentSource.getSrcParameters());

        GenericOpenApiScanner scannerParameters = new GenericOpenApiScanner();
        scannerParameters.setConfiguration(oasConfigParameters);


        for (Class cls : scannerParameters.classes()) {
            Map<String, Schema> parameters = ModelConverters.getInstance().readAll(cls);
            for (String key : parameters.keySet()) {

                io.swagger.v3.oas.models.parameters.Parameter parameter =
                        new io.swagger.v3.oas.models.parameters.Parameter()
                                .schema(parameters.get(key));

                components.addParameters(key, parameter);
            }
        }

        // scan and parse Schemas
        SwaggerConfiguration oasConfigSchemas = new SwaggerConfiguration()
                .openAPI(openAPI)
                .prettyPrint(true)
                .resourceClasses(componentSource.getSrcSchemas());

        GenericOpenApiScanner scannerSchemas = new GenericOpenApiScanner();
        scannerSchemas.setConfiguration(oasConfigSchemas);

        ModelConverters modelConverters = ModelConverters.getInstance();
        modelConverters.addConverter(new CayenneDataObjectConverter(Json.mapper()));

        for (Class cls : scannerSchemas.classes()) {
            Map<String, Schema> schemas = modelConverters.readAll(cls);
            for (String key : schemas.keySet()) {
                components.addSchemas(key, schemas.get(key));
            }
        }

        openAPI.components(components);

        return openAPI;
    }

    private void validateResourceSource(ResourceSource resourceSource) throws MojoFailureException {

//        if ((componentSource.getSrcParameters() == null || componentSource.getSrcParameters().isEmpty())
//                && (componentSource.getSrcSchemas() == null || componentSource.getSrcSchemas().isEmpty())) {
//
//            throw new MojoFailureException("Must be configured at least one scrParameter element or scrSchema element");
//        }
    }

    private void validateComponentSource(ComponentSource componentSource) throws MojoFailureException {

        if ((componentSource.getSrcParameters() == null || componentSource.getSrcParameters().isEmpty())
                && (componentSource.getSrcSchemas() == null || componentSource.getSrcSchemas().isEmpty())) {

            throw new MojoFailureException("Must be configured at least one scrParameter element or scrSchema element");
        }
    }

}
