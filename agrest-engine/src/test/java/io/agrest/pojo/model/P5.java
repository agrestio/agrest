package io.agrest.pojo.model;

import io.agrest.annotation.AgRelationship;

import java.util.List;

public class P5 {

	private List<P4> p4s;

	@AgRelationship
	public List<P4> getP4s() {
		return p4s;
	}

	public void setP4s(List<P4> p4s) {
		this.p4s = p4s;
	}
}
