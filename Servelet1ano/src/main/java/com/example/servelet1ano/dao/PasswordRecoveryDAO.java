
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.PasswordRecovery;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class PasswordRecoveryDAO {

    private static final int MAX_ATTEMPTS = 5;


    public boolean register(PasswordRecovery recovery) {

        if (recovery == null
                || recovery.getEmployeeId() <= 0
                || recovery.getCodeHash() == null
                || !recovery.getCodeHash().matches("[a-fA-F0-9]{64}")
                || recovery.getExpiresAt() == null) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (conn) {

            conn.setAutoCommit(false);

            try {
                String invalidateSql =
                        "UPDATE password_recoveries" +
                                " SET used_at = CURRENT_TIMESTAMP " +
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
                                ")";

                try (PreparedStatement pstmt = conn.prepareStatement(
                        insertSql, Statement.RETURN_GENERATED_KEYS)) {

                    pstmt.setInt(1, recovery.getEmployeeId());
                    pstmt.setString(2, recovery.getCodeHash());
                    pstmt.setTimestamp(3, recovery.getExpiresAt());
                    pstmt.setInt(4, recovery.getEmployeeId());

                    if (pstmt.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            recovery.setId(rs.getLong(1));
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao registrar a recuperação de senha.", e);
        }
    }


     /*
      Busca uma solicitação pelo seu ID.
      */

    public PasswordRecovery searchById(long id) {

        if (id <= 0) {
            return null;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        String sql =
                "SELECT id, employee_id, code_hash, expires_at, " +
                        "attempts, used_at, created_at " +
                        "FROM password_recoveries WHERE id = ?";

        try (
                conn;
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


     /*
     Registra uma tentativa incorreta, respeitando o limite.
      */


    public boolean incrementAttempts(long id) {

        if (id <= 0) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
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
                conn;
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

     /*
     Valida o código e atualiza a senha na mesma transação.
     *O código recebido deve estar convertido em HMAC-SHA-256.
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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (conn) {

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

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao concluir a recuperação de senha.", e);
        }
    }
}
