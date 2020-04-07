package com.msnovoa.jaas.autenticacion;

import java.security.Principal;

public class MsnovoaUserPrincipal implements Principal {

	/*---- ATRIBUTOS ----*/
	//Implements
	private final String name;
	//Sobrecargados
	private String position;
	private String realName;

	/*---- CONSTRUCTOR ----*/
	public MsnovoaUserPrincipal(String name) {
		super();
		this.name = name;
	}

	/*---- GETTETS - SETTERS ----*/
	@Override
	public String getName(){
		return this.name;
	}

	public String getPosition() {
		return position;
	}

	public String getRealName() {
		return realName;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}
}
