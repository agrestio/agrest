package com.nhl.link.rest.runtime.listener;

/**
 * @since 1.19
 */
public interface ListenerInvocationFactory {

	ListenerInvocation toInvocation(Object listener);
}
