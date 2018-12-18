package io.agrest.runtime.path;

import io.agrest.meta.AgEntity;

/**
 * Provides access to precompiled {@link PathDescriptor} instances for a given entity and path expression.
 */
public interface IPathDescriptorManager<P> {

    PathDescriptor<P> getPathDescriptor(AgEntity<?> entity, P path);
}
