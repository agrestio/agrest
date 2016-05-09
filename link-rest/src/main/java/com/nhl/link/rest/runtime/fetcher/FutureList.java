package com.nhl.link.rest.runtime.fetcher;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.0
 */
public class FutureList<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FutureList.class);

	private Fetcher<T> fetcher;
	private Future<List<T>> futureList;
	private long timeout;
	private TimeUnit timeoutUnit;

	public FutureList(Fetcher<T> fetcher, Future<List<T>> futureList, long timeout, TimeUnit timeoutUnit) {

		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		this.fetcher = fetcher;
		this.futureList = futureList;
	}

	public List<T> get() {

		// TODO: more descriptive exception message - which fetcher failed?

		try {
			return futureList.get(timeout, timeoutUnit);
		} catch (InterruptedException e) {
			LOGGER.warn("fetcher interrupted: " + e.getMessage());
			return fetcher.onError(e);
		} catch (ExecutionException e) {
			LOGGER.warn("fetcher error: " + e.getMessage());
			return fetcher.onError(e);
		} catch (TimeoutException e) {
			LOGGER.warn("fetcher timed out");
			return fetcher.onError(e);
		}
	}
}
