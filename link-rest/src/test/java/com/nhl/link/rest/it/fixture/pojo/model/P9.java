package com.nhl.link.rest.it.fixture.pojo.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.nhl.link.rest.annotation.LrAttribute;

public class P9 {

	private String name;
	
	private OffsetDateTime created;
	
	private LocalDateTime createdLocal;

	@LrAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@LrAttribute
	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	@LrAttribute
	public LocalDateTime getCreatedLocal() {
		return createdLocal;
	}

	public void setCreatedLocal(LocalDateTime createdLocal) {
		this.createdLocal = createdLocal;
	}
}