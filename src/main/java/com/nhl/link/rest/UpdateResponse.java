package com.nhl.link.rest;

/**
 * @since 1.7
 */
public class UpdateResponse<T> extends DataResponse<T> {

	private boolean includeData;

	public UpdateResponse(Class<T> type) {
		super(type);
	}

	public boolean isIncludeData() {
		return includeData;
	}

	public UpdateResponse<T> includeData() {
		this.includeData = true;
		return this;
	}

	public UpdateResponse<T> excludeData() {
		this.includeData = false;
		return this;
	}
}
