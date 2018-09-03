package io.agrest.it.fixture.pojo.model;

import io.agrest.annotation.LrAttribute;
import io.agrest.annotation.LrId;

public class P7 {

	int id;
	String string;

	@LrId
	public int getId() {
		return id;
	}

	@LrAttribute
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
