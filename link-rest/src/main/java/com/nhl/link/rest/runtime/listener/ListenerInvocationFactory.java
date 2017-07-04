package com.nhl.link.rest.runtime.listener;

/**
 * @since 1.19
 * @deprecated since 2.7 as listeners got replaced by functional stages.
 */
public interface ListenerInvocationFactory {

	ListenerInvocation toInvocation(Object listener);
}
