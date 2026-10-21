
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.SubscriptionsFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Subscriptions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsDAO implements DAOI<Subscriptions, SubscriptionsFilter> {

    private Connection requireConnection() {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    private Subscriptions mapSubscription(ResultSet rs) throws SQLException {
        Companies company = new Companies(
                rs.getInt("idCompany"),
                rs.getString("companyName")
        );

        Subscriptions subscription = new Subscriptions(
                rs.getInt("id"),
                rs.getInt("companyId"),
                rs.getInt("planId"),
                rs.getBoolean("is_active"),
                rs.getBoolean("installments"),
                company
        );

        subscription.setDeactivatedAt(rs.getTimestamp("deactivated_at"));
        subscription.setCreatedAt(rs.getTimestamp("created_at"));
        subscription.setUpdatedAt(rs.getTimestamp("updated_at"));

        return subscription;
    }

    private static final String SELECT_SUBSCRIPTIONS =
            "SELECT s.*, " +
                    "s.company_id AS companyId, " +
                    "s.plan_id AS planId, " +
                    "c.id AS idCompany, " +
                    "c.name AS companyName " +
                    "FROM subscriptions s " +
                    "JOIN companies c ON c.id = s.company_id ";

    /*
     * Busca assinaturas ativas aplicando os filtros informados.
     */
    @Override
    public List<Subscriptions> searchAll(SubscriptionsFilter filter) {

        List<Subscriptions> subscriptions = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                SELECT_SUBSCRIPTIONS + "WHERE s.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter != null) {
            if (filter.getId() != null) {
                sql.append(" AND s.id = ?");
                parametros.add(filter.getId());
            }

            if (filter.getCompanyId() != null) {
                sql.append(" AND s.company_id = ?");
                parametros.add(filter.getCompanyId());
            }
        }

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subscriptions.add(mapSubscription(rs));
                }
            }

            return subscriptions;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar assinaturas.", e
            );
        }
    }

    /*
     * Busca uma assinatura ativa pelo ID.
     */
    @Override
    public Subscriptions searchById(int id) {

        String sql = SELECT_SUBSCRIPTIONS
                + "WHERE s.id = ? AND s.is_active = true";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapSubscription(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar assinatura pelo ID: " + id, e
            );
        }
    }

    /*
     * Cadastra uma nova assinatura ativa.
     */
    @Override
    public boolean register(Subscriptions subscription) {

        String sql =
                "INSERT INTO subscriptions " +
                        "(company_id, plan_id, is_active, installments) " +
                        "VALUES (?, ?, true, ?)";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, subscription.isInstallments());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar assinatura.", e
            );
        }
    }

    /*
     * Desativa uma assinatura pelo ID.
     */
    @Override
    public boolean delete(int id) {

        String sql =
                "UPDATE subscriptions " +
                        "SET is_active = false, " +
                        "deactivated_at = CURRENT_TIMESTAMP, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar assinatura pelo ID: " + id, e
            );
        }
    }

    /*
     * Desativa as assinaturas de uma empresa.
     */
    public boolean deleteByCompanyId(int companyId) {

        String sql =
                "UPDATE subscriptions " +
                        "SET is_active = false, " +
                        "deactivated_at = CURRENT_TIMESTAMP, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE company_id = ? AND is_active = true";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar assinaturas da empresa: "
                            + companyId, e
            );
        }
    }

    /*
     * Atualiza os dados de uma assinatura.
     */
    @Override
    public boolean update(Subscriptions subscription, int id) {

        String sql =
                "UPDATE subscriptions SET " +
                        "company_id = ?, " +
                        "plan_id = ?, " +
                        "installments = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, subscription.isInstallments());
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar assinatura pelo ID: " + id, e
            );
        }
    }

    /*
     * Ativa uma assinatura.
     */
    public boolean activateSubscription(int id) {

        String sql =
                "UPDATE subscriptions SET " +
                        "is_active = true, " +
                        "deactivated_at = null, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao ativar assinatura pelo ID: " + id, e
            );
        }
    }

    /*
     * Desativa uma assinatura.
     */
    public boolean deactivateSubscription(int id) {

        String sql =
                "UPDATE subscriptions SET " +
                        "is_active = false, " +
                        "deactivated_at = CURRENT_TIMESTAMP, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar assinatura pelo ID: " + id, e
            );
        }
    }
}
