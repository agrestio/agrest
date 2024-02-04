package io.agrest.jaxrs3.openapi.parmconverter;

import io.agrest.jaxrs3.openapi.TypeWrapper;
import io.agrest.protocol.ControlParams;
import io.agrest.protocol.Direction;
import io.swagger.v3.jaxrs2.ResolvedParameter;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import jakarta.ws.rs.core.UriInfo;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UriInfoResolver {

    /**
     * Remove UriInfo parameters from the Operation if they were also explicitly added.
     */
    static void removeDuplicatedParams(Operation operation) {

        List<Parameter> params = operation.getParameters();
        if (params == null) {
            return;
        }

        // remove duplicate parameters that may have been added via UriInfo
        Set<String> nonUriInfoParams = new HashSet<>();
        List<Parameter> uriInfoParams = new ArrayList<>();

        for (Parameter p : params) {
            if (p instanceof UriInfoQueryParameter) {
                uriInfoParams.add(p);
            } else {
                nonUriInfoParams.add(p.getName());
            }
        }

        for (Parameter p : uriInfoParams) {
            if (nonUriInfoParams.contains(p.getName())) {
                params.remove(p);
            }
        }
    }

    public boolean willResolve(TypeWrapper wrapper, List<Annotation> annotations) {
        if (wrapper != null && wrapper.getRawClass() == UriInfo.class) {

            for (Annotation a : annotations) {
                if (a instanceof io.swagger.v3.oas.annotations.Parameter) {
                    io.swagger.v3.oas.annotations.Parameter pa = (io.swagger.v3.oas.annotations.Parameter) a;
                    return !pa.hidden();
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
        resolved.parameters.add(createDirectionParam());
        resolved.parameters.add(createExpParam());
        resolved.parameters.add(createMapByParam());
        resolved.parameters.add(createStartParam());
        resolved.parameters.add(createLimitParam());

        return resolved;
    }

    protected Parameter createIncludeParam() {
        // TODO: detailed schema
        return queryParam(ControlParams.include);
    }

    protected Parameter createExcludeParam() {
        return queryParam(ControlParams.exclude);
    }

    protected Parameter createSortParam() {
        // TODO: detailed schema
        return queryParam(ControlParams.sort);
    }

    protected Parameter createDirectionParam() {
        Schema<String> dirSchema = new StringSchema()
                .addEnumItem(Direction.asc.name())
                .addEnumItem(Direction.asc_ci.name())
                .addEnumItem(Direction.desc.name())
                .addEnumItem(Direction.desc_ci.name());

        return queryParam(ControlParams.direction).schema(dirSchema);
    }

    protected Parameter createExpParam() {
        // TODO: detailed schema
        return queryParam(ControlParams.exp);
    }

    protected Parameter createMapByParam() {
        return queryParam(ControlParams.mapBy);
    }

    protected Parameter createStartParam() {
        return queryParam(ControlParams.start);
    }

    protected Parameter createLimitParam() {
        return queryParam(ControlParams.limit);
    }

    protected Parameter queryParam(ControlParams param) {
        // returning our own subclass of the QueryParameter to be able to analyze duplicate parameters
        // downstream
        return new UriInfoQueryParameter()
                .name(param.name())
                .description(param.description);
    }

    /**
     * A subclass indicating that this is an implicit parameters added via UriInfo
     */
    static class UriInfoQueryParameter extends QueryParameter {
    }
}
