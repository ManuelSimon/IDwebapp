package com.msnovoa.jaas.autenticacion;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class MsnovoaCredentialManager {

    public MsnovoaCredentialManager(){
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
    protected String[] verifyCredentials(Callback[] callbacks) {
        String[] valoresUsuario = null;
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];

        String userLogin = nameCallback.getName();
        String password = new String(passwordCallback.getPassword());

        BufferedReader bufferedReader;
        try {
            final String dir = System.getProperty("user.dir");
            bufferedReader = new BufferedReader(new FileReader(dir + "/../webapps/msnovoa/" + "DB.txt"));
            String line = bufferedReader.readLine();
            while (line != null) {
                valoresUsuario = line.split(";");
                if (valoresUsuario[0].equals(userLogin)) {
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
                        valoresUsuario = null;
                        break;
                    }
                }
                line = bufferedReader.readLine();
                if (line == null) { //Legados a este punto, no se habrá localizado ningún usuario coincidente en la BD
                    System.out.println("Login Module - Usuario inexistente en la BD. No conseguiste acceso, lo siento.");
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

}
