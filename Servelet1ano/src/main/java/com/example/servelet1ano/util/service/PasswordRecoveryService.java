
package com.example.servelet1ano.util.service;

import com.example.servelet1ano.connection.DatabaseConfig;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.dao.PasswordRecoveryDAO;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PasswordRecovery;
import com.example.servelet1ano.filter.EmployeesFilter;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PasswordRecoveryService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 10;

    /**
     * Gera um código numérico aleatório de seis dígitos.
     * Mantém os zeros à esquerda quando necessário.
     **/
    public String generateCode() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    /**
     * Converte o código em um HMAC-SHA-256 utilizando uma chave secreta.
     * Retorna o resultado em hexadecimal para armazenamento no banco.
     **/
    public String hashCode(String code, String codeSecret) {
        if (code == null || !code.matches("\\d{" + CODE_LENGTH + "}")) {
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

    /*
     * Solicita a recuperação de senha de um funcionário.
     * Busca a conta pelo e-mail, gera o código, calcula seu HMAC
     * e registra a solicitação com prazo de dez minutos.
     * Após o registro, envia o código pela EmailService.
     * Invalida a solicitação se ocorrer uma falha no envio.
     */

    public Long requestRecovery(
            String email,
            EmployeesDAO employeesDAO,
            PasswordRecoveryDAO recoveryDAO,
            EmailService emailService
    ) throws IOException, InterruptedException {

        if (email == null || email.isBlank()) {
            return null;
        }

        String codeSecret = DatabaseConfig.getPasswordRecoverySecret();

        if (codeSecret == null || codeSecret.isBlank()) {
            throw new IllegalStateException(
                    "A chave de recuperação não foi configurada."
            );
        }

        EmployeesFilter filter = new EmployeesFilter();
        filter.setEmail(email.trim());

        List<Employees> employees = employeesDAO.searchAll(filter);

        if (employees.isEmpty()) {
            return null;
        }

        Employees employee = employees.get(0);

        String code = generateCode();
        String codeHash = hashCode(code, codeSecret);

        PasswordRecovery recovery = new PasswordRecovery(
                0L,
                employee.getId(),
                codeHash,
                new Timestamp(getExpirationMillis()),
                0,
                null,
                null
        );

        if (!recoveryDAO.register(recovery)) {
            return null;
        }

        try {
            emailService.sendRecoveryCode(
                    employee.getEmail(),
                    code
            );

            return recovery.getId();

        } catch (IOException | InterruptedException | RuntimeException e) {

            try {
                recoveryDAO.invalidate(recovery.getId());
            } catch (RuntimeException invalidationError) {
                e.addSuppressed(invalidationError);
            }

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

        /**
         * Calcula o instante de expiração do código em milissegundos.
         * O código permanece válido por dez minutos após a geração.
         **/
        public long getExpirationMillis () {
            return System.currentTimeMillis()
                    + EXPIRATION_MINUTES * 60_000L;
        }
}
