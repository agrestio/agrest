package io.agrest.runtime.path;

import io.agrest.meta.AgEntity;
import io.agrest.backend.exp.parser.ASTObjPath;


/**
 * Provides access to precompiled {@link PathDescriptor} instances for a given entity and path expression.
 */
public interface IPathDescriptorManager {

    PathDescriptor getPathDescriptor(AgEntity<?> entity, ASTObjPath path);
}
