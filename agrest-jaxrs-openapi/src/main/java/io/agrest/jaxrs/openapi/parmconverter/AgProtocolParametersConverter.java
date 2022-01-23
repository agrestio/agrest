package io.agrest.jaxrs.openapi.parmconverter;

import com.fasterxml.jackson.annotation.JsonView;
import io.agrest.jaxrs.openapi.TypeWrapper;
import io.swagger.v3.jaxrs2.ResolvedParameter;
import io.swagger.v3.jaxrs2.ext.AbstractOpenAPIExtension;
import io.swagger.v3.jaxrs2.ext.OpenAPIExtension;
import io.swagger.v3.oas.models.Components;

import javax.ws.rs.Consumes;
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

    private final UriInfoResolver uriInfoResolver;

    public AgProtocolParametersConverter() {
        this.uriInfoResolver = new UriInfoResolver();
    }

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

        if (uriInfoResolver.willResolve(wrapper, annotations)) {
            return uriInfoResolver.resolve();
        } else {
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
    }
}
