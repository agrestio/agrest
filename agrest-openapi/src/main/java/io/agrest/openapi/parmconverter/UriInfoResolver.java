package io.agrest.openapi.parmconverter;

import io.agrest.base.protocol.AgProtocol;
import io.agrest.base.protocol.Dir;
import io.agrest.openapi.TypeWrapper;
import io.swagger.v3.jaxrs2.ResolvedParameter;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
import java.util.List;

public class UriInfoResolver {

    public boolean willResolve(TypeWrapper wrapper, List<Annotation> annotations) {
        if (wrapper != null && wrapper.getRawClass() == UriInfo.class) {
            for (Annotation a : annotations) {
                if (a instanceof Context) {
                    return true;
                }
            }
        }

        return false;
    }

    public ResolvedParameter resolve() {

        // "@Context UriInfo" resolves to the full set of Agrest protocol keys

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
                .name(AgProtocol.include.name())
                .description("Either a property path or a JSON object defining rules for including entity properties in response");
    }

    protected Parameter createExcludeParam() {
        return new QueryParameter()
                .name(AgProtocol.exclude.name())
                .description("Property path to exclude from the response");
    }

    protected Parameter createSortParam() {
        // TODO: detailed schema
        return new QueryParameter()
                .name(AgProtocol.sort.name())
                .description("Either a property path or a JSON object that defines result sorting. May be used in conjunction with 'dir' parameter.");
    }

    protected Parameter createDirParam() {
        Schema<String> dirSchema = new StringSchema()
                .addEnumItem(Dir.ASC.name())
                .addEnumItem(Dir.ASC_CI.name())
                .addEnumItem(Dir.DESC.name())
                .addEnumItem(Dir.DESC_CI.name());

        return new QueryParameter()
                .name(AgProtocol.dir.name())
                .description("Defines result sort direction. Must be used in conjunction with 'sort' parameter")
                .schema(dirSchema);
    }

    protected Parameter createCayenneExpParam() {
        // TODO: detailed schema
        return new QueryParameter()
                .name(AgProtocol.cayenneExp.name())
                .description("Expression used to filter the result");
    }

    protected Parameter createMapByParam() {
        return new QueryParameter()
                .name(AgProtocol.mapBy.name())
                .description("Property path to use as a result map key. When present a result \"data\" is rendered as a map instead of a list.");
    }

    protected Parameter createStartParam() {
        return new QueryParameter()
                .name(AgProtocol.start.name())
                .description("Defines how many objects to skip from the beginning of a result list. Used to control pagination");
    }

    protected Parameter createLimitParam() {
        return new QueryParameter()
                .name(AgProtocol.limit.name())
                .description("Max objects to include in the result list. Used to control pagination");
    }
}
