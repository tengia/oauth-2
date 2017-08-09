package net.oauth2.client;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import net.oauth2.AccessToken;

/**
 * Functional interface for resumable, {@link TokenProviderJob}s
 * providers. The provider is resumed with its former settings, and based on
 * arguments used to synchronize its execution schedule with the current moment
 * in time.
 *
 * @param <T>
 */
@FunctionalInterface
public interface Resumable<T extends AccessToken>  {

	/**
	 * Resumes a suspended refreshing task.
	 */
	ScheduledFuture<?> resume(T token, Instant fetchMoment, boolean refetchIfExpired);

}