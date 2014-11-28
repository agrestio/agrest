package com.nhl.link.rest.unit.matcher;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class LRDataMatcher extends BaseMatcher<Response> {

	private static String toEntityString(String expectedData, int expectedTotal) {
		return new StringBuilder().append("{\"success\":true,\"data\":").append(expectedData).append(",\"total\":")
				.append(expectedTotal).append("}").toString();
	}

	private String expectedData;
	private int expectedTotal;
	private Matcher<Response> stringMatcher;

	public LRDataMatcher(Entity<String> expectedData, int expectedTotal) {
		this(expectedData.getEntity(), expectedTotal);
	}

	public LRDataMatcher(String expectedData, int expectedTotal) {
		this.expectedData = expectedData;
		this.expectedTotal = expectedTotal;
		this.stringMatcher = new EntityStringMatcher(toEntityString(expectedData, expectedTotal));
	}

	@Override
	public boolean matches(Object item) {
		return stringMatcher.matches(item);
	}

	@Override
	public void describeMismatch(Object item, Description description) {
		stringMatcher.describeMismatch(item, description);
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("total(" + expectedTotal + ")");
		description.appendText(" and data(" + expectedData + ")");
	}

}
