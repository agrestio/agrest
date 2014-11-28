package com.nhl.link.rest.unit.matcher;

import static org.hamcrest.CoreMatchers.both;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.Matcher;

public class LRMatchers {

	public static Matcher<Response> hasStatusAndBody(Status status, String expectedBody) {
		return both(new StatusMatcher(status)).and(new EntityStringMatcher(expectedBody));
	}

	public static Matcher<Response> okAndHasBody(String expectedBody) {
		return hasStatusAndBody(Status.OK, expectedBody);
	}

	public static Matcher<Response> okAndHasData(int expectedTotal, Entity<String> expectedData) {
		return both(new StatusMatcher(Status.OK)).and(new LRDataMatcher(expectedData, expectedTotal));
	}

	public static Matcher<Response> okAndHasData(int expectedTotal, String expectedData) {
		return both(new StatusMatcher(Status.OK)).and(new LRDataMatcher(expectedData, expectedTotal));
	}

}
