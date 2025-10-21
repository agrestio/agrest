package io.agrest.spring.it.pojo.model;

import io.agrest.annotation.AgAttribute;

public class P3 {

	private String name;

	@AgAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
