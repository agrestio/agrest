package com.nhl.link.rest.it.fixture.pojo.model;

import com.nhl.link.rest.annotation.LrAttribute;

public class P3 {

	private String name;

	@LrAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
