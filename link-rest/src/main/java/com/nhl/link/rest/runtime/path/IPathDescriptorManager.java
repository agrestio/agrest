package com.nhl.link.rest.runtime.path;

import org.apache.cayenne.exp.parser.ASTObjPath;

import com.nhl.link.rest.meta.LrEntity;

/**
 * Provides access to precompiled {@link PathDescriptor} instances for a given entity and path expression.
 */
public interface IPathDescriptorManager {

    PathDescriptor getPathDescriptor(LrEntity<?> entity, ASTObjPath path);
}
