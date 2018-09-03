package io.agrest.it.fixture.pojo.model;

import io.agrest.annotation.LrAttribute;

public class P1 {

	private String name;

	@LrAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
