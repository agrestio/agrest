package io.agrest.jaxrs2.junit.pojo;

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
