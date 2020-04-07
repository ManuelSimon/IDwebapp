package com.msnovoa.jaas.autenticacion;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MsnovoaLoginModule implements javax.security.auth.spi.LoginModule {

    /*---- VARIABLES ----*/
    private Subject subject;
    private CallbackHandler callbackHandler;
    private MsnovoaUserPrincipal msnovoaUserPrincipal;
    private String userLogin;
    private List<String> userRoles;
    private Map sharedState;
    private Map options;

    private boolean succeeded = false;

    /*---- CONSTRUCTOR ----*/
    public MsnovoaLoginModule() {
        System.out.println("Login Module - constructor llamado");
    }

    /*---- MÉTODOS ----*/

    /**
     * Sería utilizado en caso de que el Login requiriera de una segunda fase para su funcionamiento.
     * Como solo requiere una, omitimos su uso y simplemente damos acceso directo.
     */
    @Override
    public boolean abort() throws LoginException {
        System.out.println("Login Module - abort llamado");
        return false;
    }

    /**
     * Sería utilizado en caso de que el Login requiriera de una segunda fase para su funcionamiento.
     * Como solo requiere una, omitimos su uso y simplemente damos acceso directo.
     */
    @Override
    public boolean commit() throws LoginException {
        System.out.println("Login Module - commit llamado");
        return this.succeeded;
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        this.succeeded = false;
        //System.out.println("Valor de testOption: " + (String) options.get("testOption"));
    }

    @Override
    public boolean login() throws LoginException {
        System.out.println("Login Module - login llamado");

        if (this.callbackHandler == null) {
            throw new LoginException("Oh no, callbackHandler null");
        }

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("name:");
        callbacks[1] = new PasswordCallback("password:", false);

        try {
            this.callbackHandler.handle(callbacks);
        } catch (IOException e) {
            throw new LoginException("Oh no, IOException en callbackHandler");
        } catch (UnsupportedCallbackException e) {
            throw new LoginException("Oh no, UnsupportedCallbackException en callbackHandler");
        }

        MsnovoaCredentialManager msnovoaCredentialManager = new MsnovoaCredentialManager();
        String[] valoresUsuario = msnovoaCredentialManager.verifyCredentials(callbacks);
        if (valoresUsuario != null) {
            this.userLogin = valoresUsuario[0];

            System.out.println("    Acceso conseguido, ¡bienvenido, " + this.userLogin + "!");
            this.succeeded = true;

            //Almacenamos la información correspondiente a este usuario

            //this.userLogin ya fue establecido en la función de verificación
            this.userRoles = new ArrayList<String>();
            for (String role : valoresUsuario[4].split(",")) {
                this.userRoles.add(role);
            }

            //Añadimos los Principals correspondientes a este Subject.

            //Añadimos Principal del nombre de usuario e info extra
            this.msnovoaUserPrincipal = new MsnovoaUserPrincipal(this.userLogin);
            msnovoaUserPrincipal.setRealName(valoresUsuario[2]);
            msnovoaUserPrincipal.setPosition(valoresUsuario[3]);
            this.subject.getPrincipals().add(this.msnovoaUserPrincipal);

            //Añadimos Principal(s) de los roles que posee
            if (this.userRoles.size() > 0) {
                for (String role : this.userRoles) {
                    MsnovoaRolePrincipal roleIterate = new MsnovoaRolePrincipal(role);
                    this.subject.getPrincipals().add(roleIterate);
                }
            }
        }
        return this.succeeded;
    }

    @Override
    public boolean logout() {
        System.out.println("Login Module - logout llamado");
        this.succeeded = false;

        //Eliminamos Principal del nombre de usuario
        this.subject.getPrincipals().remove(this.msnovoaUserPrincipal);

        //Eliminamos Principal(s) de los roles que posee
        if (this.userRoles != null && this.userRoles.size() > 0) {
            for (String role : this.userRoles) {
                MsnovoaRolePrincipal roleIterate = new MsnovoaRolePrincipal(role);
                this.subject.getPrincipals().remove(roleIterate);
            }
        }
        return true;
    }
}