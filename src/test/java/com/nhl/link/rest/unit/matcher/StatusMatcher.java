package com.nhl.link.rest.unit.matcher;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class StatusMatcher extends BaseMatcher<Response> {

	private Status expectedStatus;

	public StatusMatcher(Status status) {
		this.expectedStatus = status;
	}

	@Override
	public boolean matches(Object item) {
		return item != null && ((Response) item).getStatus() == expectedStatus.getStatusCode();
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("status(\"" + expectedStatus.getStatusCode() + "\")");
	}

}
