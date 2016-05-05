package com.nhl.link.rest.it.fixture.pojo.model;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;

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
