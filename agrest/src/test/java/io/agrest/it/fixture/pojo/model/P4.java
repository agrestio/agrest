package io.agrest.it.fixture.pojo.model;

import io.agrest.annotation.LrRelationship;

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
