package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByConstructor;
import com.nhl.link.rest.runtime.parser.size.ISizeConstructor;
import com.nhl.link.rest.runtime.parser.sort.ISortConstructor;
import com.nhl.link.rest.protocol.Include;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response.Status;
import java.util.List;


public class IncludeConstructor implements IIncludeConstructor {

	private ISortConstructor sortConstructor;
	private ICayenneExpConstructor expConstructor;
	private IMapByConstructor mapByConstructor;
	private ISizeConstructor sizeConstructor;

	public IncludeConstructor(
            @Inject ICayenneExpConstructor expConstructor,
			@Inject ISortConstructor sortConstructor,
			@Inject IMapByConstructor mapByConstructor,
			@Inject ISizeConstructor sizeConstructor) {

		this.sortConstructor = sortConstructor;
		this.expConstructor = expConstructor;
		this.mapByConstructor = mapByConstructor;
		this.sizeConstructor = sizeConstructor;
	}


	/**
	 * @since 2.13
	 */
	@Override
	public void construct(ResourceEntity<?> resourceEntity, List<Include> includes) {
		for (Include include : includes) {
			processOne(resourceEntity, include);
		}

		BaseRequestProcessor.processDefaultIncludes(resourceEntity);
	}

	private void processOne(ResourceEntity<?> resourceEntity, Include include) {
		processIncludeObject(resourceEntity, include);
		// processes nested includes
		if (include != null) {
			include.getIncludes().stream().forEach(i -> processIncludeObject(resourceEntity, i));
		}
	}

	private void processIncludeObject(ResourceEntity<?> rootEntity, Include include) {
		if (include != null) {
			ResourceEntity<?> includeEntity;

			final String value = include.getValue();
			if (value != null && !value.isEmpty()) {
				BaseRequestProcessor.checkTooLong(value);
				BaseRequestProcessor.processIncludePath(rootEntity, value);
			}

			final String path = include.getPath();
			if (path == null || path.isEmpty()) {
				// root node
				includeEntity = rootEntity;
			} else {
				BaseRequestProcessor.checkTooLong(path);
				includeEntity = BaseRequestProcessor.processIncludePath(rootEntity, path);
				if (includeEntity == null) {
					throw new LinkRestException(Status.BAD_REQUEST,
							"Bad include spec, non-relationship 'path' in include object: " + path);
				}
			}

			mapByConstructor.constructIncluded(includeEntity, include.getMapBy());
			sortConstructor.construct(includeEntity, include.getSort());
			expConstructor.construct(includeEntity, include.getCayenneExp());
			sizeConstructor.construct(includeEntity, include.getStart(), include.getLimit());
		}
	}
}
