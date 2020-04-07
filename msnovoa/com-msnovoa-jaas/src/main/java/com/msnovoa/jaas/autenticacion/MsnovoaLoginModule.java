package com.msnovoa.jaas.autenticacion;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
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

    /**
     * Función para aplicar el Hashing SHA1, devolviéndonos la contraseña cifrada.
     *
     * @param input La contraseña con el salpimentado ya realizado previamente. Sobre está se aplicará el cifrado.
     * @return La contraseña cifrada mediante el algoritmo SHA-1
     */
    private String stringToSHA1(String input) throws LoginException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(input.getBytes());
            byte passwordSHA1Byte[] = messageDigest.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : passwordSHA1Byte) {
                stringBuffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new LoginException("Oh no, error en el cifrado SHA-1 de la contraseña.");
        }
    }

    /**
     * Función para aplicar el Hashing SHA-1, junto con Key Stretching mediante PBKDF2, devolviéndonos la contraseña cifrada.
     *
     * @param input La contraseña SIN salpimentar.
     * @param salt  El salt a aplicar, que a su vez se compondrá de salt+pepper, juntos.
     * @return La contraseña cifrada mediante el algoritmo SHA-1 y aplicando Key Stretching mediante PBKDF2
     */
    private String stringToSHA1KeyStretching(String salt, String input) throws LoginException {
        try {
            byte[] saltByte = salt.getBytes(StandardCharsets.UTF_8);
            KeySpec keySpec = new PBEKeySpec(input.toCharArray(), saltByte, 3000000, 128);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] passwordSHA1Byte = secretKeyFactory.generateSecret(keySpec).getEncoded();

            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : passwordSHA1Byte) {
                stringBuffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new LoginException("Oh no, error en el cifrado SHA-1 de la contraseña.");
        }
    }

    /**
     * Verifica las credenciales accediendo a una base de datos en un archivo de texto.
     * De no tener éxito la identificación, lanza una excepción.
     * Si tiene éxito, devuelve los datos del usuario, en un array de Strings sin procesar.
     */
    private String[] verifyCredentials(Callback[] callbacks) {
        String[] valoresUsuario = null;
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];

        this.userLogin = nameCallback.getName();
        String password = new String(passwordCallback.getPassword());

        BufferedReader bufferedReader;
        try {
            final String dir = System.getProperty("user.dir");
            bufferedReader = new BufferedReader(new FileReader(dir + "/../webapps/msnovoa/" + "DB.txt"));
            String line = bufferedReader.readLine();
            while (line != null) {
                valoresUsuario = line.split(";");
                if (valoresUsuario[0].equals(this.userLogin)) {
                    System.out.println("Login Module - Detectado usuario existente en la BD. Se procederá a comprobar su correcta identificación...");

                    //Detectado un usuario existente en el sistema, salpimentaremos la contraseña dada y aplicaremos SHA1
                    BufferedReader bufferedReaderPepper = new BufferedReader(new FileReader(dir + "/../webapps/msnovoa/" + "pepper.txt"));
                    //Leemos la primera línea de este fichero. El pepper es una cadena de caracteres creada aleatoriamente, pero siempre igual para todos los usuarios.
                    String pepper = bufferedReaderPepper.readLine();
                    bufferedReaderPepper.close();

                    //Aplicamos el algoritmo SHA-1, junto con el mecanismo de Key Stretching mediante PBKDF2.
                    password = this.stringToSHA1KeyStretching(valoresUsuario[5] + pepper, password);

                    if (valoresUsuario[1].equals(password)) {
                        System.out.println("Login Module - Contraseña correcta, realizando login en el sistema...");
                        break;
                    } else {
                        System.out.println("Login Module - Contraseña incorrecta. No conseguiste acceso, lo siento.");
                        this.succeeded = false;
						valoresUsuario = null;
						break;
                    }
                }
                line = bufferedReader.readLine();
                if (line == null) { //Legados a este punto, no se habrá localizado ningún usuario coincidente en la BD
                    System.out.println("Login Module - Usuario inexistente en la BD. No conseguiste acceso, lo siento.");
                    this.succeeded = false;
                    valoresUsuario = null;
					throw new LoginException("Oh no! Error de acceso, contraseña incorrecta.");
                }
            }
            bufferedReader.close();
        } catch (IOException | LoginException e) {
            e.printStackTrace();
        }
        return valoresUsuario;
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

        String[] valoresUsuario = this.verifyCredentials(callbacks);
        if (valoresUsuario != null) {

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