package io.agrest.runtime.shutdown;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.cayenne.di.BeforeScopeEnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.0
 */
public class ShutdownManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownManager.class);

	private Duration timeout;
	private ConcurrentMap<AutoCloseable, Integer> shutdownHooks;

	public ShutdownManager(Duration timeout) {
		this.shutdownHooks = new ConcurrentHashMap<>();
		this.timeout = timeout;
	}

	public void addShutdownHook(AutoCloseable shutdownHook) {
		shutdownHooks.put(shutdownHook, 1);
	}

	/**
	 * A shutdown method called by Injector.
	 */
	@BeforeScopeEnd
	public void shutdown() {

		Map<?, ? extends Throwable> shutdownErrors;

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Map<?, ? extends Throwable>> future = executor.submit(() -> shutdownAll());

		try {
			shutdownErrors = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			shutdownErrors = Collections.singletonMap(this, e);
		}

		executor.shutdownNow();

		shutdownErrors.forEach((c, e) -> LOGGER.warn("Error on shutdown", e));
	}

	protected Map<?, ? extends Throwable> shutdownAll() {
		Map<Object, Throwable> errors = new HashMap<>();
		shutdownHooks.keySet().forEach(c -> shutdownOne(c).ifPresent(e -> errors.put(c, e)));
		return errors;
	}

	protected Optional<Exception> shutdownOne(AutoCloseable closeable) {
		try {
			closeable.close();
			return Optional.empty();
		} catch (Exception e) {
			return Optional.of(e);
		}
	}

}
