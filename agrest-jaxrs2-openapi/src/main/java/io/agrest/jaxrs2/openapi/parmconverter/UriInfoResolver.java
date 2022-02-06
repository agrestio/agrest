package io.agrest.jaxrs2.openapi.parmconverter;

import io.agrest.jaxrs2.openapi.TypeWrapper;
import io.agrest.protocol.AgProtocol;
import io.agrest.protocol.Dir;
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
        resolved.parameters.add(createExpParam());
        resolved.parameters.add(createCayenneExpParam());
        resolved.parameters.add(createMapByParam());
        resolved.parameters.add(createStartParam());
        resolved.parameters.add(createLimitParam());

        return resolved;
    }

    protected Parameter createIncludeParam() {
        // TODO: detailed schema
        return queryParam(AgProtocol.include);
    }

    protected Parameter createExcludeParam() {
        return queryParam(AgProtocol.exclude);
    }

    protected Parameter createSortParam() {
        // TODO: detailed schema
        return queryParam(AgProtocol.sort);
    }

    protected Parameter createDirParam() {
        Schema<String> dirSchema = new StringSchema()
                .addEnumItem(Dir.ASC.name())
                .addEnumItem(Dir.ASC_CI.name())
                .addEnumItem(Dir.DESC.name())
                .addEnumItem(Dir.DESC_CI.name());

        return queryParam(AgProtocol.dir).schema(dirSchema);
    }

    protected Parameter createExpParam() {
        // TODO: detailed schema
        return queryParam(AgProtocol.exp);
    }

    /**
     * @deprecated since 4.1 in favor of {@link AgProtocol#exp}, but will be supported indefinitely for backwards compatibility.
     */
    @Deprecated
    protected Parameter createCayenneExpParam() {
        // TODO: detailed schema
        return queryParam(AgProtocol.cayenneExp).deprecated(true);
    }

    protected Parameter createMapByParam() {
        return queryParam(AgProtocol.mapBy);
    }

    protected Parameter createStartParam() {
        return queryParam(AgProtocol.start);
    }

    protected Parameter createLimitParam() {
        return queryParam(AgProtocol.limit);
    }

    protected Parameter queryParam(AgProtocol param) {
        return new QueryParameter()
                .name(param.name())
                .description(param.description);
    }
}
