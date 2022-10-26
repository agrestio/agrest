package io.agrest.spring.starter.it.pojo.model;

import io.agrest.annotation.AgAttribute;

public class P1 {

	private String name;

	@AgAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
