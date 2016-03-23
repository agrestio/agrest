package com.nhl.link.rest.it.fixture.pojo.model;

import com.nhl.link.rest.annotation.LrRelationship;

public class P4 {

	private P3 p3;

	@LrRelationship
	public P3 getP3() {
		return p3;
	}

	public void setP3(P3 p3) {
		this.p3 = p3;
	}
}
