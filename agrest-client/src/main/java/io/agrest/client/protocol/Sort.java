package io.agrest.client.protocol;

import io.agrest.base.protocol.Dir;

/**
 * @since 2.0
 */
public class Sort {

	public static Sort property(String propertyName) {
		return new Sort(propertyName, Dir.ASC);
	}

	private String propertyName;
	private Dir direction;

	private Sort(String propertyName, Dir direction) {
		this.propertyName = propertyName;
		this.direction = direction;
	}

	public Sort asc() {
		direction = Dir.ASC;
		return this;
	}

	public Sort desc() {
		direction = Dir.DESC;
		return this;
	}

    /**
     * @since 3.4
     */
	public Sort ascCi() {
		direction = Dir.ASC_CI;
		return this;
	}

    /**
     * @since 3.4
     */
	public Sort descCi() {
		direction = Dir.DESC_CI;
		return this;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Dir getDirection() {
		return direction;
	}
}
