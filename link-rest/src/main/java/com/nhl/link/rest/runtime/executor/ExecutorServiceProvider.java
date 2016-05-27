package com.nhl.link.rest.runtime.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.cayenne.di.Provider;

/**
 * @since 2.0
 */
public class ExecutorServiceProvider implements Provider<ExecutorService> {

	@Override
	public ExecutorService get() {

		ThreadFactory factory = new ThreadFactory() {

			private AtomicLong threadCounter = new AtomicLong();

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "link-rest-exec-" + threadCounter.getAndIncrement());
			}
		};

		return Executors.newCachedThreadPool(factory);
	}
}
