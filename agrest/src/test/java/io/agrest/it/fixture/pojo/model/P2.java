package io.agrest.it.fixture.pojo.model;

import io.agrest.annotation.LrAttribute;
import io.agrest.annotation.LrRelationship;

public class P2 {

	private String name;
	private P1 p1;

	@LrAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@LrRelationship
	public P1 getP1() {
		return p1;
	}

	public void setP1(P1 p1) {
		this.p1 = p1;
	}
}
