package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.ChainProcessor;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.ProcessorOutcome;

import javax.ws.rs.core.Response.Status;
import java.lang.invoke.MethodHandle;

/**
 * A wrapper of an annotated listener method for a single execution stage of a
 * LinkRest chain.
 *
 * @since 1.19
 * @deprecated since 2.7 as listeners got replaced by functional stages.
 */
public abstract class ListenerInvocation {

    private static final ProcessingStage FAKE_START_STAGE = c -> null;

    protected MethodHandle methodHandle;
    protected boolean voidMethod;

    public ListenerInvocation(MethodHandle methodHandle, boolean voidMethod) {
        // passed MethodHandle must be already bound to the listener instance
        this.methodHandle = methodHandle;
        this.voidMethod = voidMethod;
    }

    // method compatible with Processor API
    public <C extends ProcessingContext<T>, T> ProcessorOutcome invoke(C context) {

        ProcessingStage<C, ? super T> next = invokeOld(context, FAKE_START_STAGE);

        if (next == null) {
            return ProcessorOutcome.STOP;
        } else if (next == FAKE_START_STAGE) {
            return ProcessorOutcome.CONTINUE;
        } else {
            // custom stage ... execute and do not proceed to the normal pipeline
            ChainProcessor.execute(next, context);
            return ProcessorOutcome.STOP;
        }
    }

    @SuppressWarnings("unchecked")
    protected <C extends ProcessingContext<T>, T> ProcessingStage<C, ? super T> invokeOld(C context, ProcessingStage<C, ? super T> next) {

        ProcessingStage<C, ? super T> processor;
        try {
            processor = doInvokeOld(context, next);

        } catch (Throwable e) {
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error invoking listener method", e);
        }

        // if result is NULL, but the method is void, we must return
        // next; otherwise NULL is treated chain terminator...
        return (ProcessingStage<C, ? super T>) (voidMethod ? next : processor);
    }


    protected abstract <C extends ProcessingContext<T>, T> ProcessingStage<C, ? super T> doInvokeOld(
            C context,
            ProcessingStage<C, ? super T> next) throws Throwable;

}
