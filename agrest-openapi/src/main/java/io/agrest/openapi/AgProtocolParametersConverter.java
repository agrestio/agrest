package io.agrest.openapi;

import com.fasterxml.jackson.annotation.JsonView;
import io.agrest.base.protocol.Dir;
import io.swagger.v3.jaxrs2.ResolvedParameter;
import io.swagger.v3.jaxrs2.ext.AbstractOpenAPIExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A Swagger extension that expands {@link UriInfo} to a set of Agrest protocol parameter models.
 */
public class AgProtocolParametersConverter extends AbstractOpenAPIExtension {

    // TODO: Agrest should define these publicly somewhere
    private static final String PROTOCOL_CAYENNE_EXP = "cayenneExp";
    private static final String PROTOCOL_DIR = "dir";
    private static final String PROTOCOL_EXCLUDE = "exclude";
    private static final String PROTOCOL_INCLUDE = "include";
    private static final String PROTOCOL_LIMIT = "limit";
    private static final String PROTOCOL_MAP_BY = "mapBy";
    private static final String PROTOCOL_SORT = "sort";
    private static final String PROTOCOL_START = "start";

    @Override
    public ResolvedParameter extractParameters(
            List<Annotation> annotations,
            Type type,
            Set<Type> typesToSkip,
            Components components,
            Consumes classConsumes,
            Consumes methodConsumes,
            boolean includeRequestBody,
            JsonView jsonViewAnnotation,
            Iterator<OpenAPIExtension> chain) {

        TypeWrapper wrapper = TypeWrapper.forType(type);

        if (wrapper != null && wrapper.getRawClass() == UriInfo.class) {
            for (Annotation a : annotations) {
                if (a instanceof Context) {
                    // "@Context UriInfo" resolves to the full set of Agrest protocol keys
                    return parametersForUriInfo();
                }
            }
        }

        return super.extractParameters(
                annotations,
                type,
                typesToSkip,
                components,
                classConsumes,
                methodConsumes,
                includeRequestBody,
                jsonViewAnnotation,
                chain);
    }

    protected ResolvedParameter parametersForUriInfo() {

        ResolvedParameter resolved = new ResolvedParameter();

        resolved.parameters.add(createIncludeParam());
        resolved.parameters.add(createExcludeParam());
        resolved.parameters.add(createSortParam());
        resolved.parameters.add(createDirParam());
        resolved.parameters.add(createCayenneExpParam());
        resolved.parameters.add(createMapByParam());
        resolved.parameters.add(createStartParam());
        resolved.parameters.add(createLimitParam());

        return resolved;
    }

    protected Parameter createIncludeParam() {
        // TODO: detailed schema
        return new QueryParameter()
                .name(PROTOCOL_INCLUDE)
                .description("Property path to include in the response");
    }

    protected Parameter createExcludeParam() {
        return new QueryParameter()
                .name(PROTOCOL_EXCLUDE)
                .description("Property path to exclude from the response");
    }

    protected Parameter createSortParam() {
        // TODO: detailed schema
        return new QueryParameter()
                .name(PROTOCOL_SORT)
                .description("Defines result sorting. May be used in conjunction with 'dir' parameter");
    }

    protected Parameter createDirParam() {
        Schema<String> dirSchema = new StringSchema()
                .addEnumItem(Dir.ASC.name())
                .addEnumItem(Dir.ASC_CI.name())
                .addEnumItem(Dir.DESC.name())
                .addEnumItem(Dir.DESC_CI.name());

        return new QueryParameter()
                .name(PROTOCOL_DIR)
                .description("Defines result sort direction. Must be used in conjunction with 'sort' parameter")
                .schema(dirSchema);
    }

    protected Parameter createCayenneExpParam() {
        // TODO: detailed schema
        return new QueryParameter()
                .name(PROTOCOL_CAYENNE_EXP)
                .description("Expression used to filter the result");
    }

    protected Parameter createMapByParam() {
        return new QueryParameter()
                .name(PROTOCOL_MAP_BY)
                .description("Property path to use as a key, turning the result into a map");
    }

    protected Parameter createStartParam() {
        return new QueryParameter()
                .name(PROTOCOL_START)
                .description("How many objects to skip from the beginning of the result list. Used to control pagination");
    }

    protected Parameter createLimitParam() {
        return new QueryParameter()
                .name(PROTOCOL_LIMIT)
                .description("Max objects to include in the result list. Used to control pagination");
    }
}
