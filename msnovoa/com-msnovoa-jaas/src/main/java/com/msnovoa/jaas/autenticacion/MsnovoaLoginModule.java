package com.msnovoa.jaas.autenticacion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class MsnovoaLoginModule implements LoginModule {

	private CallbackHandler handler;
	private Subject subject;
	private MsnovoaUserPrincipal msnovoaUserPrincipal;
	private MsnovoaRolePrincipal msnovoaRolePrincipal;
	private String login;
	private List<String> userGroups;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {

		handler = callbackHandler;
		this.subject = subject;
	}

	@Override
	public boolean login() throws LoginException {

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("login");
		callbacks[1] = new PasswordCallback("password", true);

		try {
			handler.handle(callbacks);
			String name = ((NameCallback) callbacks[0]).getName();
			String password = String.valueOf(((PasswordCallback) callbacks[1])
					.getPassword());

			// Here we validate the credentials against some
			// authentication/authorization provider.
			// It can be a Database, an external LDAP, a Web Service, etc.
			// For this tutorial we are just checking if user is "user123" and
			// password is "pass123"
			if (name != null && name.equals("user123") && password != null
					&& password.equals("pass123")) {
				login = name;
				userGroups = new ArrayList<String>();
				userGroups.add("admin");
				return true;
			}

			// If credentials are NOT OK we throw a LoginException
			throw new LoginException("Authentication failed");

		} catch (IOException e) {
			throw new LoginException(e.getMessage());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException(e.getMessage());
		}

	}

	@Override
	public boolean commit() throws LoginException {

		msnovoaUserPrincipal = new MsnovoaUserPrincipal(login);
		subject.getPrincipals().add(msnovoaUserPrincipal);

		if (userGroups != null && userGroups.size() > 0) {
			for (String groupName : userGroups) {
				msnovoaRolePrincipal = new MsnovoaRolePrincipal(groupName);
				subject.getPrincipals().add(msnovoaRolePrincipal);
			}
		}

		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		return false;
	}

	@Override
	public boolean logout() throws LoginException {
		subject.getPrincipals().remove(msnovoaUserPrincipal);
		subject.getPrincipals().remove(msnovoaRolePrincipal);
		return true;
	}

}
