package io.agrest.jpa;

import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.jpa.compiler.JpaAgEntityCompiler;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaQueryAssembler;
import io.agrest.jpa.pocessor.select.stage.JpaSelectApplyServerParamsStage;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 */
public class AgJpaModule implements Module {

    private final IAgJpaPersister persister;

    public AgJpaModule(IAgJpaPersister persister) {
        this.persister = persister;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(IAgJpaPersister.class).toInstance(persister);
        binder.bind(JpaAgEntityCompiler.class).to(JpaAgEntityCompiler.class);
        binder.bindList(AgEntityCompiler.class).insertBefore(JpaAgEntityCompiler.class, AnnotationsAgEntityCompiler.class);

        binder.bind(IJpaQueryAssembler.class).to(JpaQueryAssembler.class);
        binder.bind(SelectApplyServerParamsStage.class).to(JpaSelectApplyServerParamsStage.class);


    }
}
