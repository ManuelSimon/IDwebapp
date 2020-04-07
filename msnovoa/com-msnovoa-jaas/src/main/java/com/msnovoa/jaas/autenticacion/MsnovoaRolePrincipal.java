package com.msnovoa.jaas.autenticacion;

import java.security.Principal;

public class MsnovoaRolePrincipal implements Principal {

	/*---- ATRIBUTOS ----*/
	private final String groupName;

	/*---- CONSTRUCTOR ----*/
	public MsnovoaRolePrincipal(String groupName) {
		super();
		this.groupName = groupName;
	}

	/*---- GETTETS - SETTERS ----*/
	@Override
	public String getName(){
		return this.groupName;
	}
}
