package io.agrest.jaxrs2.openapi.parmconverter;

import io.agrest.jaxrs2.openapi.TypeWrapper;
import io.agrest.protocol.ControlParams;
import io.agrest.protocol.Direction;
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
        resolved.parameters.add(createDirectionParam());
        resolved.parameters.add(createDirParam());
        resolved.parameters.add(createExpParam());
        resolved.parameters.add(createCayenneExpParam());
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

    /**
     * @deprecated since 5.0 {@link ControlParams#dir} was deprecated in favor of {@link ControlParams#direction}
     */
    @Deprecated
    protected Parameter createDirParam() {
        Schema<String> dirSchema = new StringSchema()
                .addEnumItem(Direction.asc.name())
                .addEnumItem(Direction.asc_ci.name())
                .addEnumItem(Direction.desc.name())
                .addEnumItem(Direction.desc_ci.name());

        return queryParam(ControlParams.dir).schema(dirSchema).deprecated(true);
    }

    protected Parameter createExpParam() {
        // TODO: detailed schema
        return queryParam(ControlParams.exp);
    }

    /**
     * @deprecated since 4.1 {@link ControlParams#cayenneExp} was deprecated in favor of {@link ControlParams#exp}
     */
    @Deprecated
    protected Parameter createCayenneExpParam() {
        // TODO: detailed schema
        return queryParam(ControlParams.cayenneExp).deprecated(true);
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
        return new QueryParameter()
                .name(param.name())
                .description(param.description);
    }
}
