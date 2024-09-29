package org.example.backend.Utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private static final int M = 3; // Nombre de bits pour l'espace d'identifiants

    public static String hashKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(key.getBytes(StandardCharsets.UTF_8));
            BigInteger hashInt = new BigInteger(1, hashBytes);
            BigInteger mod = BigInteger.valueOf(2).pow(M);
            return hashInt.mod(mod).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
