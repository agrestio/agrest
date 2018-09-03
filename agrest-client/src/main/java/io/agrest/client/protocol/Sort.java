package io.agrest.client.protocol;

/**
 * @since 2.0
 */
public class Sort {

	public enum SortDirection {
		ASCENDING("ASC"), DESCENDING("DESC");

		private String abbrev;

		SortDirection(String abbrev) {
			this.abbrev = abbrev;
		}

		public String abbrev() {
			return abbrev;
		}
	}

	public static Sort property(String propertyName) {
		return new Sort(propertyName, SortDirection.ASCENDING);
	}

	private String propertyName;
	private SortDirection direction;

	private Sort(String propertyName, SortDirection direction) {
		this.propertyName = propertyName;
		this.direction = direction;
	}

	public Sort asc() {
		direction = SortDirection.ASCENDING;
		return this;
	}

	public Sort desc() {
		direction = SortDirection.DESCENDING;
		return this;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public SortDirection getDirection() {
		return direction;
	}
}
