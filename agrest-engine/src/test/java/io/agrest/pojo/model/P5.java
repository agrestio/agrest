package io.agrest.pojo.model;

import java.util.List;

import io.agrest.annotation.AgRelationship;

public class P5 {

	private List<P4> p4s;

	@AgRelationship
	public List<P4> getP4s() {
		return p4s;
	}
}
