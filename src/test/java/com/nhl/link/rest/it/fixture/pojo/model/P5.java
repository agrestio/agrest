package com.nhl.link.rest.it.fixture.pojo.model;

import java.util.List;

import com.nhl.link.rest.annotation.LrRelationship;

public class P5 {

	private List<P4> p4s;

	@LrRelationship
	public List<P4> getP4s() {
		return p4s;
	}
}
