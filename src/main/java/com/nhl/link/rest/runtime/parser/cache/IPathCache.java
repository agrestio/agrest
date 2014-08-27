package com.nhl.link.rest.runtime.parser.cache;

import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.map.ObjEntity;

/**
 * Caches parsed paths. There is a finite number of valid paths in each app
 * model, and not having to parse them every time should save a few cycles.
 * 
 * @since 1.5
 */
public interface IPathCache {

	PathDescriptor getPathDescriptor(ObjEntity entity, ASTObjPath path);
}
