package com.msnovoa.jaas.autenticacion;

import javax.security.auth.callback.*;
import java.io.IOException;

public class MsnovoaCallbackHandler implements CallbackHandler {

    /*---- VARIABLES ----*/
    String name;
    String password;

    /*---- CONSTRUCTOR ----*/
    public MsnovoaCallbackHandler(String name, String password) {
        System.out.println("Callback Handler - constructor llamado");
        this.name = name;
        this.password = password;
    }

    /*---- MÉTODOS ----*/
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        System.out.println("Callback Handler - handle llamado");
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                NameCallback nameCallback = (NameCallback) callback;
                nameCallback.setName(this.name);
            } else if (callback instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) callback;
                passwordCallback.setPassword(this.password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callback, "Oh no! Este Callback no está soportado");
            }
        }
    }
}