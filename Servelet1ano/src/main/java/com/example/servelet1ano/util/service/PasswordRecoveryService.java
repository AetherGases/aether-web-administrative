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

/**
 * Classe com as regras da recuperacao de senha
 * Gera o codigo, guarda so o hash dele no banco e manda o codigo original por e-mail
 */
public class PasswordRecoveryService {

    /**
     * Gerador de numeros aleatorios seguro, usado para criar o codigo
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Quantidade de digitos do codigo de recuperacao
     */
    private static final int CODE_LENGTH = 6;

    /**
     * Tempo (em minutos) que o codigo continua valido depois de gerado
     */
    private static final int EXPIRATION_MINUTES = 10;

    /**
     * metodo que gera um codigo numerico aleatorio de seis digitos
     * Mantem os zeros a esquerda quando necessario
     * @return O codigo gerado (ex: "004821")
     */
    public String generateCode() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    /**
     * metodo que converte o codigo em um HMAC-SHA-256 usando uma chave secreta
     * O resultado fica em hexadecimal, pronto para guardar no banco
     * @param code O codigo de seis digitos
     * @param codeSecret A chave secreta usada para gerar o hash
     * @return O hash do codigo em hexadecimal
     * @throws IllegalArgumentException se o codigo for invalido ou a chave estiver vazia
     * @throws IllegalStateException se nao for possivel gerar o hash
     */
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

    /**
     * metodo que solicita a recuperacao de senha de um funcionario
     * Busca a conta pelo e-mail, gera o codigo, calcula o HMAC dele
     * e registra a solicitacao com prazo de dez minutos
     * Depois de registrar, envia o codigo pelo EmailService
     * Se o envio falhar, invalida a solicitacao
     * @param email O e-mail da conta que quer recuperar a senha
     * @param employeesDAO O DAO usado para buscar o funcionario pelo e-mail
     * @param recoveryDAO O DAO usado para registrar (e invalidar) a solicitacao
     * @param emailService O servico usado para enviar o codigo por e-mail
     * @return O id da solicitacao registrada, ou null se o e-mail estiver vazio, nao existir conta com ele ou nao for possivel registrar
     * @throws IllegalStateException se a chave de recuperacao nao estiver configurada no .env
     * @throws IOException se der erro de comunicacao ao enviar o e-mail
     * @throws InterruptedException se o envio do e-mail for interrompido
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
     * metodo que calcula o instante em que o codigo expira, em milissegundos
     * O codigo continua valido por dez minutos depois de gerado
     * @return O momento de expiracao do codigo, em milissegundos
     */
    public long getExpirationMillis () {
        return System.currentTimeMillis()
                + EXPIRATION_MINUTES * 60_000L;
    }
}