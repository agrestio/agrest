package com.nhl.link.rest.unit.matcher;

import javax.ws.rs.core.Response;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class EntityStringMatcher extends BaseMatcher<Response> {

	private String expectedEntity;

	public EntityStringMatcher(String expectedEntity) {
		this.expectedEntity = expectedEntity;
	}

	@Override
	public boolean matches(Object item) {

		if (item == null) {
			return false;
		}

		String entity = readEntity((Response) item);
		return expectedEntity == null ? entity == null : expectedEntity.equals(entity);
	}

	@Override
	public void describeMismatch(Object item, Description description) {
		String entity = readEntity((Response) item);
		description.appendText("was ").appendValue(entity);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("entity(\"" + expectedEntity + "\")");
	}

	private String readEntity(Response response) {
		// must buffer entity. This allows multiple readEntity calls that
		// sometimes happen in combination matchers
		if (!response.bufferEntity()) {
			throw new IllegalStateException("Can't buffer response entity");
		}

		return response.readEntity(String.class);
	}

}
