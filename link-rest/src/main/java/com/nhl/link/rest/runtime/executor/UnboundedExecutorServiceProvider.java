package com.nhl.link.rest.runtime.executor;

import com.nhl.link.rest.runtime.shutdown.ShutdownManager;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @since 2.0
 */
public class UnboundedExecutorServiceProvider implements Provider<ExecutorService> {

	private ShutdownManager shutdownManager;

	public UnboundedExecutorServiceProvider(@Inject ShutdownManager shutdownManager) {
		this.shutdownManager = shutdownManager;
	}

	@Override
	public ExecutorService get() throws DIRuntimeException {
		// TODO: we are not limiting the thread pool size... this is unsafe. An
		// alternative - fixed thread pool - doesn't seem to be able to shrink
		// on light usage though.

		AtomicLong threadNumber = new AtomicLong();
		ExecutorService service = Executors
				.newCachedThreadPool(r -> new Thread(r, "link-rest-pool-" + threadNumber.getAndIncrement()));

		shutdownManager.addShutdownHook(service::shutdownNow);

		return service;
	}
}
