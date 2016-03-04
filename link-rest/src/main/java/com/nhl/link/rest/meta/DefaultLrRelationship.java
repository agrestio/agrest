package com.nhl.link.rest.meta;

/**
 * @since 1.12
 */
public class DefaultLrRelationship implements LrRelationship {

	private String name;
	private Class<?> targetEntityType;
	private boolean toMany;

	public DefaultLrRelationship(String name, Class<?> targetEntityType, boolean toMany) {
		this.name = name;
		this.targetEntityType = targetEntityType;
		this.toMany = toMany;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getTargetEntityType() {
		return targetEntityType;
	}

	@Override
	public boolean isToMany() {
		return toMany;
	}
}
