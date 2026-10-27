package com.example.servelet1ano.util.service;

import com.example.servelet1ano.connection.DatabaseConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PasswordRecoveryService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 10;

    public String generateCode() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    public String hashCode(String code, String codeSecret) {
        if (code == null || !code.matches("\\d{"+CODE_LENGTH+"}")) {
            throw new IllegalArgumentException("Código inválido.");
        }

        if (codeSecret == null || codeSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "A chave de recuperação não foi configurada."
            );
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(
                    codeSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(key);

            byte[] hash = mac.doFinal(
                    code.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível gerar o hash do código.", e
            );
        }
    }

    public long getExpirationMillis() {
        return System.currentTimeMillis()
                + EXPIRATION_MINUTES * 60_000L;
    }

}
