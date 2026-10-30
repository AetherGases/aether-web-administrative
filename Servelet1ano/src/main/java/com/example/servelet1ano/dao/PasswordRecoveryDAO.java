package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.PasswordRecovery;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Classe que cuida das solicitacoes de recuperacao de senha no banco (tabela password_recoveries)
 * Guarda so o hash do codigo enviado por e-mail, nunca o codigo em si
 */
public class PasswordRecoveryDAO {

    /**
     * Quantidade maxima de tentativas de validar o codigo de uma solicitacao
     * Depois disso a solicitacao deixa de aceitar o codigo
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * metodo que registra uma nova solicitacao de recuperacao de senha
     * Invalida as solicitacoes anteriores do funcionario e cadastra o novo codigo somente se a conta estiver ativa
     * As duas operacoes acontecem na mesma transacao: ou as duas dao certo, ou nada muda
     * @param recovery A solicitacao que sera cadastrada (o id gerado e preenchido nela)
     * @return true se foi registrada e false se os dados forem invalidos (ex: hash fora do formato) ou a conta do funcionario nao estiver ativa
     */
    public boolean register(PasswordRecovery recovery) {

        if (recovery == null
                || recovery.getEmployeeId() <= 0
                || recovery.getCodeHash() == null
                || !recovery.getCodeHash().matches("[a-fA-F0-9]{64}")
                || recovery.getExpiresAt() == null) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            conn.setAutoCommit(false);

            try {
                String invalidateSql =
                        "UPDATE password_recoveries " +
                                "SET used_at = CURRENT_TIMESTAMP " +
                                "WHERE employee_id = ? AND used_at IS NULL";

                try (PreparedStatement pstmt =
                             conn.prepareStatement(invalidateSql)) {

                    pstmt.setInt(1, recovery.getEmployeeId());
                    pstmt.executeUpdate();
                }

                String insertSql =
                        "INSERT INTO password_recoveries " +
                                "(employee_id, code_hash, expires_at) " +
                                "SELECT ?, ?, ? " +
                                "WHERE EXISTS (" +
                                "SELECT 1 FROM employees " +
                                "WHERE id = ? AND is_active = TRUE" +
                                ") " +
                                "RETURNING id";

                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

                    pstmt.setInt(1, recovery.getEmployeeId());
                    pstmt.setString(2, recovery.getCodeHash());
                    pstmt.setTimestamp(3, recovery.getExpiresAt());
                    pstmt.setInt(4, recovery.getEmployeeId());

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            recovery.setId(rs.getLong("id"));
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao registrar a recuperação de senha.", e);
        }
    }

    /**
     * metodo para buscar uma solicitacao de recuperacao pelo id
     * @param id Numero unico da solicitacao
     * @return A solicitacao encontrada, ou null se o id for invalido ou ela nao existir
     */
    public PasswordRecovery searchById(long id) {

        if (id <= 0) {
            return null;
        }

        String sql =
                "SELECT id, employee_id, code_hash, expires_at, " +
                        "attempts, used_at, created_at " +
                        "FROM password_recoveries WHERE id = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return new PasswordRecovery(
                            rs.getLong("id"),
                            rs.getInt("employee_id"),
                            rs.getString("code_hash"),
                            rs.getTimestamp("expires_at"),
                            rs.getInt("attempts"),
                            rs.getTimestamp("used_at"),
                            rs.getTimestamp("created_at")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao consultar a recuperação de senha.", e);
        }

        return null;
    }

    /**
     * metodo que soma uma tentativa de validacao na solicitacao
     * So soma se o codigo ainda nao foi usado, nao expirou e nao atingiu o limite de tentativas
     * @param id Numero unico da solicitacao
     * @return true se a tentativa foi registrada e false se o id for invalido ou a solicitacao ja foi usada, expirou ou atingiu o limite
     */
    public boolean incrementAttempts(long id) {

        if (id <= 0) {
            return false;
        }

        String sql =
                "UPDATE password_recoveries " +
                        "SET attempts = attempts + 1 " +
                        "WHERE id = ? " +
                        "AND used_at IS NULL " +
                        "AND expires_at > CURRENT_TIMESTAMP " +
                        "AND attempts < ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setLong(1, id);
            pstmt.setInt(2, MAX_ATTEMPTS);

            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao registrar tentativa de recuperação.", e);
        }
    }

    /**
     * metodo que valida o codigo informado e troca a senha do funcionario
     * Tudo acontece na mesma transacao: ou o codigo e conferido, a senha e trocada e o codigo e invalidado, ou nada muda
     * Recusa solicitacoes expiradas, ja usadas ou que atingiram o limite de tentativas
     * Se o codigo estiver errado, soma uma tentativa (e a soma fica salva)
     * @param recoveryId Numero unico da solicitacao
     * @param submittedCodeHash O HMAC-SHA-256 do codigo digitado pelo usuario (64 caracteres hexadecimais)
     * @param newPasswordHash O hash da nova senha (gerado pelo PasswordHasher)
     * @return true se a senha foi trocada e false se os dados forem invalidos, a solicitacao nao existir, estiver expirada, usada ou sem tentativas, ou o codigo estiver errado
     */
    public boolean completeRecovery(
            long recoveryId,
            String submittedCodeHash,
            String newPasswordHash) {

        if (recoveryId <= 0
                || submittedCodeHash == null
                || !submittedCodeHash.matches("[a-fA-F0-9]{64}")
                || newPasswordHash == null
                || newPasswordHash.isBlank()) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            conn.setAutoCommit(false);

            try {
                String selectSql =
                        "SELECT pr.employee_id, pr.code_hash, " +
                                "pr.expires_at, pr.attempts, pr.used_at " +
                                "FROM password_recoveries pr " +
                                "JOIN employees e ON e.id = pr.employee_id " +
                                "WHERE pr.id = ? AND e.is_active = TRUE " +
                                "FOR UPDATE OF pr";

                int employeeId;
                String storedCodeHash;
                Timestamp expiresAt;
                int attempts;
                Timestamp usedAt;

                try (PreparedStatement pstmt =
                             conn.prepareStatement(selectSql)) {

                    pstmt.setLong(1, recoveryId);

                    try (ResultSet rs = pstmt.executeQuery()) {

                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        employeeId = rs.getInt("employee_id");
                        storedCodeHash = rs.getString("code_hash");
                        expiresAt = rs.getTimestamp("expires_at");
                        attempts = rs.getInt("attempts");
                        usedAt = rs.getTimestamp("used_at");
                    }
                }

                if (usedAt != null
                        || expiresAt == null
                        || expiresAt.getTime() <= System.currentTimeMillis()
                        || attempts >= MAX_ATTEMPTS) {
                    conn.rollback();
                    return false;
                }

                boolean codeMatches = MessageDigest.isEqual(
                        storedCodeHash.getBytes(
                                java.nio.charset.StandardCharsets.US_ASCII),
                        submittedCodeHash.getBytes(
                                java.nio.charset.StandardCharsets.US_ASCII)
                );

                if (!codeMatches) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE password_recoveries " +
                                    "SET attempts = attempts + 1 " +
                                    "WHERE id = ? AND attempts < ?")) {

                        pstmt.setLong(1, recoveryId);
                        pstmt.setInt(2, MAX_ATTEMPTS);
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                    return false;
                }

                String updatePasswordSql =
                        "UPDATE employees " +
                                "SET password_hash = ?, " +
                                "updated_at = CURRENT_TIMESTAMP " +
                                "WHERE id = ? AND is_active = TRUE";

                try (PreparedStatement pstmt =
                             conn.prepareStatement(updatePasswordSql)) {

                    pstmt.setString(1, newPasswordHash);
                    pstmt.setInt(2, employeeId);

                    if (pstmt.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                String consumeSql =
                        "UPDATE password_recoveries " +
                                "SET used_at = CURRENT_TIMESTAMP " +
                                "WHERE id = ? AND used_at IS NULL";

                try (PreparedStatement pstmt =
                             conn.prepareStatement(consumeSql)) {

                    pstmt.setLong(1, recoveryId);

                    if (pstmt.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao concluir a recuperação de senha.", e);
        }
    }

    /**
     * metodo que invalida uma solicitacao de recuperacao de senha (marca como usada)
     * Util quando o envio do e-mail com o codigo falha
     * @param id Numero unico da solicitacao
     * @return true se foi invalidada e false se o id for invalido ou a solicitacao nao existe ou ja estava usada
     */
    public boolean invalidate(long id) {

        if (id <= 0) {
            return false;
        }

        String sql =
                "UPDATE password_recoveries " +
                        "SET used_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND used_at IS NULL";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setLong(1, id);

            return pstmt.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao invalidar a recuperação de senha.", e
            );
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Desfaz a transacao sem esconder o erro original
     */
    private void rollbackQuietly(Connection conn, Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }

}