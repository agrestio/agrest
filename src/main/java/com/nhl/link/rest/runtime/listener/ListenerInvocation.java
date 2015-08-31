package com.nhl.link.rest.runtime.listener;

import java.lang.invoke.MethodHandle;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.Processor;

/**
 * A wrapper of an annotated listener method for a single execution stage of a
 * LinkRest chain.
 * 
 * @since 1.19
 */
public abstract class ListenerInvocation {

	protected MethodHandle methodHandle;
	protected boolean voidMethod;

	public ListenerInvocation(MethodHandle methodHandle, boolean voidMethod) {
		// passed MethodHandle must be already bound to the listener instance
		this.methodHandle = methodHandle;
		this.voidMethod = voidMethod;
	}

	protected abstract <C extends ProcessingContext<T>, T> Processor<C, ? super T> doInvoke(C context,
			Processor<C, ? super T> next) throws Throwable;

	@SuppressWarnings("unchecked")
	public <C extends ProcessingContext<T>, T> Processor<C, ? super T> invoke(C context, Processor<C, ? super T> next) {

		Processor<C, ? super T> processor;
		try {
			processor = doInvoke(context, next);

		} catch (Throwable e) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error invoking listener method", e);
		}

		// if result is NULL, but the method is void, we must return
		// next; otherwise NULL is treated chain terminator...
		return (Processor<C, ? super T>) (voidMethod ? next : processor);
	}

}
