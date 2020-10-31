package io.agrest.runtime.meta;

import io.agrest.compiler.AgEntityCompiler;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.LazyAgDataMap;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.List;


/**
 * @since 4.1
 */
public class LazyAgDataMapProvider implements Provider<AgDataMap> {

    private List<AgEntityCompiler> compilers;

    public LazyAgDataMapProvider(@Inject List<AgEntityCompiler> compilers) {
        this.compilers = compilers;
    }

    @Override
    public AgDataMap get() throws DIRuntimeException {
        return new LazyAgDataMap(compilers);
    }
}
