package com.nhl.link.rest.runtime.fetcher;

import java.util.Iterator;
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
public class FutureIterable<T> implements Iterable<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FutureIterable.class);

	private volatile Iterable<T> result;
	private Fetcher<T> fetcher;
	private Future<Iterable<T>> future;
	private long timeout;
	private TimeUnit timeoutUnit;

	public static <T> FutureIterable<T> resolved(List<T> result) {

		FutureIterable<T> futureList = new FutureIterable<>();
		futureList.result = result;
		return futureList;
	}

	public static <T> FutureIterable<T> future(Fetcher<T> fetcher, Future<Iterable<T>> future, long timeout,
			TimeUnit timeoutUnit) {

		FutureIterable<T> futureList = new FutureIterable<>();
		futureList.timeout = timeout;
		futureList.timeoutUnit = timeoutUnit;
		futureList.fetcher = fetcher;
		futureList.future = future;

		return futureList;
	}

	private FutureIterable() {
	}

	@Override
	public Iterator<T> iterator() {

		if (result == null) {
			result = awaitResult();
		}

		return result.iterator();
	}

	private Iterable<T> awaitResult() {

		// TODO: more descriptive exception message - which fetcher failed?

		try {
			return future.get(timeout, timeoutUnit);
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
