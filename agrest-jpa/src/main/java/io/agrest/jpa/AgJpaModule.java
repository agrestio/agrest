package io.agrest.jpa;

import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.jpa.compiler.JpaAgEntityCompiler;
import io.agrest.jpa.exp.IJpaExpParser;
import io.agrest.jpa.exp.JpaExpParser;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.jpa.pocessor.JpaQueryAssembler;
import io.agrest.jpa.pocessor.delete.stage.JpaDeleteInDataStoreStage;
import io.agrest.jpa.pocessor.delete.stage.JpaDeleteMapChangesStage;
import io.agrest.jpa.pocessor.delete.stage.JpaDeleteStartStage;
import io.agrest.jpa.pocessor.select.stage.JpaSelectApplyServerParamsStage;
import io.agrest.jpa.pocessor.update.stage.JpaCreatedOrOkResponseStage;
import io.agrest.jpa.pocessor.update.stage.JpaCreatedResponseStage;
import io.agrest.jpa.pocessor.update.stage.JpaMapCreateOrUpdateStage;
import io.agrest.jpa.pocessor.update.stage.JpaMapCreateStage;
import io.agrest.jpa.pocessor.update.stage.JpaMapIdempotentCreateOrUpdateStage;
import io.agrest.jpa.pocessor.update.stage.JpaMapIdempotentFullSyncStage;
import io.agrest.jpa.pocessor.update.stage.JpaMapUpdateStage;
import io.agrest.jpa.pocessor.update.stage.JpaMergeChangesStage;
import io.agrest.jpa.pocessor.update.stage.JpaOkResponseStage;
import io.agrest.jpa.pocessor.update.stage.JpaUpdateApplyServerParamsStage;
import io.agrest.jpa.pocessor.update.stage.JpaUpdateCommitStage;
import io.agrest.jpa.pocessor.update.stage.JpaUpdateStartStage;
import io.agrest.runtime.processor.delete.stage.DeleteInDataStoreStage;
import io.agrest.runtime.processor.delete.stage.DeleteMapChangesStage;
import io.agrest.runtime.processor.delete.stage.DeleteStartStage;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import io.agrest.runtime.processor.update.UpdateFlavorDIKeys;
import io.agrest.runtime.processor.update.stage.UpdateApplyServerParamsStage;
import io.agrest.runtime.processor.update.stage.UpdateCommitStage;
import io.agrest.runtime.processor.update.stage.UpdateFillResponseStage;
import io.agrest.runtime.processor.update.stage.UpdateMapChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateMergeChangesStage;
import io.agrest.runtime.processor.update.stage.UpdateStartStage;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Key;
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

        binder.bind(IJpaExpParser.class).to(JpaExpParser.class);

        // Update stages
        binder.bind(UpdateStartStage.class).to(JpaUpdateStartStage.class);
        binder.bind(UpdateApplyServerParamsStage.class).to(JpaUpdateApplyServerParamsStage.class);
        binder.bind(UpdateMergeChangesStage.class).to(JpaMergeChangesStage.class);
        binder.bind(UpdateCommitStage.class).to(JpaUpdateCommitStage.class);

        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.CREATE)).to(JpaMapCreateStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.CREATE_OR_UPDATE)).to(JpaMapCreateOrUpdateStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.IDEMPOTENT_CREATE_OR_UPDATE)).to(JpaMapIdempotentCreateOrUpdateStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.IDEMPOTENT_FULL_SYNC)).to(JpaMapIdempotentFullSyncStage.class);
        binder.bind(Key.get(UpdateMapChangesStage.class, UpdateFlavorDIKeys.UPDATE)).to(JpaMapUpdateStage.class);

        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.CREATE)).to(JpaCreatedResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.CREATE_OR_UPDATE)).to(JpaCreatedOrOkResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.IDEMPOTENT_CREATE_OR_UPDATE)).to(JpaCreatedOrOkResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.IDEMPOTENT_FULL_SYNC)).to(JpaCreatedOrOkResponseStage.class);
        binder.bind(Key.get(UpdateFillResponseStage.class, UpdateFlavorDIKeys.UPDATE)).to(JpaOkResponseStage.class);

        // Delete stages
        binder.bind(DeleteStartStage.class).to(JpaDeleteStartStage.class);
        binder.bind(DeleteMapChangesStage.class).to(JpaDeleteMapChangesStage.class);
        binder.bind(DeleteInDataStoreStage.class).to(JpaDeleteInDataStoreStage.class);
    }
}
