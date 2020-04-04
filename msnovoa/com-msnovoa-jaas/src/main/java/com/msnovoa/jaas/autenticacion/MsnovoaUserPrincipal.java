package com.msnovoa.jaas.autenticacion;

import java.security.Principal;

public class MsnovoaUserPrincipal implements Principal {

	private String name;
	
	public MsnovoaUserPrincipal(String name) {
		super();
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
