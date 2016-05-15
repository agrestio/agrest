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

	private volatile List<T> result;
	private Fetcher<T> fetcher;
	private Future<List<T>> future;
	private long timeout;
	private TimeUnit timeoutUnit;

	public static <T> FutureList<T> resolved(List<T> result) {

		FutureList<T> futureList = new FutureList<>();
		futureList.result = result;
		return futureList;
	}

	public static <T> FutureList<T> future(Fetcher<T> fetcher, Future<List<T>> future, long timeout,
			TimeUnit timeoutUnit) {

		FutureList<T> futureList = new FutureList<>();
		futureList.timeout = timeout;
		futureList.timeoutUnit = timeoutUnit;
		futureList.fetcher = fetcher;
		futureList.future = future;

		return futureList;
	}

	private FutureList() {
	}

	public List<T> get() {

		if (result == null) {
			result = awaitResult();
		}

		return result;
	}

	private List<T> awaitResult() {

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
