package com.nhl.link.rest.meta;

import java.util.Objects;

/**
 * @since 1.12
 */
public class DefaultLrRelationship implements LrRelationship {

	private String name;
	private LrEntity<?> targetEntity;
	private boolean toMany;

	public DefaultLrRelationship(String name, LrEntity<?> targetEntity, boolean toMany) {
		this.name = name;
		this.targetEntity = Objects.requireNonNull(targetEntity);
		this.toMany = toMany;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public LrEntity<?> getTargetEntity() {
		return targetEntity;
	}

	@Override
	public boolean isToMany() {
		return toMany;
	}
}
