package io.agrest.runtime.meta;

import io.agrest.compiler.AgEntityCompiler;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.List;


/**
 * @since 5.0
 */
public class LazySchemaProvider implements Provider<AgSchema> {

    private List<AgEntityCompiler> compilers;

    public LazySchemaProvider(@Inject List<AgEntityCompiler> compilers) {
        this.compilers = compilers;
    }

    @Override
    public AgSchema get() throws DIRuntimeException {
        return new LazySchema(compilers);
    }
}
