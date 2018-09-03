package io.agrest.it.fixture.pojo.model;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;

public class P7 {

	int id;
	String string;

	@AgId
	public int getId() {
		return id;
	}

	@AgAttribute
	public String getString() {
		return string;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setString(String string) {
		this.string = string;
	}
}
