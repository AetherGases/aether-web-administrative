package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.SubscriptionsFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Subscriptions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsDAO implements DAOI<Subscriptions, SubscriptionsFilter> {

    /**
     * Busca as assinaturas ativas aplicando os filtros informados.
     * Os campos nulos do filtro são ignorados.
     *
     * @param filter filtros opcionais da consulta
     * @return lista de assinaturas encontradas
     */
    @Override
    public List<Subscriptions> searchAll(SubscriptionsFilter filter) {
        List<Subscriptions> subscriptions = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT s.*, s.company_id AS companyId, s.plan_id AS planId, " +
                        "c.id AS idCompany, c.name AS companyName " +
                        "FROM subscriptions s " +
                        "JOIN companies c ON c.id = s.company_id " +
                        "WHERE s.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter != null && filter.getId() != null) {
            sql.append(" AND s.id = ?");
            parametros.add(filter.getId());
        }

        if (filter != null && filter.getCompanyId() != null) {
            sql.append(" AND s.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        try (
                Connection conn = ConnectionFactory.connect();
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
                    "Erro ao consultar as assinaturas.", e
            );
        }
    }

    /**
     * Busca uma assinatura ativa pelo ID.
     *
     * @param id identificador da assinatura
     * @return assinatura encontrada ou null se não existir
     */
    @Override
    public Subscriptions searchById(int id) {
        String sql =
                "SELECT s.*, s.company_id AS companyId, s.plan_id AS planId, " +
                        "c.id AS idCompany, c.name AS companyName " +
                        "FROM subscriptions s " +
                        "JOIN companies c ON c.id = s.company_id " +
                        "WHERE s.id = ? AND s.is_active = true";

        try (
                Connection conn = ConnectionFactory.connect();
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
                    "Erro ao buscar a assinatura pelo ID.", e
            );
        }
    }

    /**
     * Cadastra uma assinatura utilizando uma conexão própria.
     *
     * @param subscription assinatura que será cadastrada
     * @return true se o cadastro foi realizado
     */
    @Override
    public boolean register(Subscriptions subscription) {
        try (Connection conn = ConnectionFactory.connect()) {
            return registerConn(subscription, conn);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar a assinatura.", e
            );
        }
    }

    /**
     * Cadastra uma assinatura utilizando uma conexão compartilhada.
     *
     * Não abre nem fecha a conexão recebida e não confirma a transação.
     * O service controla o commit e o rollback.
     *
     * @param subscription assinatura que será cadastrada
     * @param conn conexão utilizada na transação externa
     * @return true se o cadastro foi realizado
     * @throws SQLException se ocorrer um erro durante a operação SQL
     */
    public boolean registerConn(
            Subscriptions subscription,
            Connection conn
    ) throws SQLException {

        if (subscription == null) {
            throw new IllegalArgumentException(
                    "A assinatura não pode ser nula."
            );
        }

        if (subscription.getCompanyId() <= 0) {
            throw new IllegalArgumentException(
                    "A empresa da assinatura deve ser válida."
            );
        }

        if (subscription.getPlanId() <= 0) {
            throw new IllegalArgumentException(
                    "O plano da assinatura deve ser válido."
            );
        }

        if (conn == null || conn.isClosed()) {
            throw new IllegalArgumentException(
                    "A conexão com o banco é inválida."
            );
        }

        String sql =
                "INSERT INTO subscriptions " +
                        "(company_id, plan_id, is_active, installments) " +
                        "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, subscription.isInstallments());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Desativa uma assinatura pelo ID.
     *
     * @param id identificador da assinatura
     * @return true se algum registro foi atualizado
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
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar a assinatura.", e
            );
        }
    }

    /**
     * Desativa todas as assinaturas de uma empresa.
     *
     * @param companyId identificador da empresa
     * @return true se algum registro foi atualizado
     */
    public boolean deleteByCompanyId(int companyId) {
        String sql =
                "UPDATE subscriptions " +
                        "SET is_active = false, " +
                        "deactivated_at = CURRENT_TIMESTAMP, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE company_id = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, companyId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar as assinaturas da empresa.", e
            );
        }
    }

    /**
     * Atualiza os dados de uma assinatura.
     *
     * @param subscription novos dados da assinatura
     * @param id identificador da assinatura a atualizar
     * @return true se algum registro foi atualizado
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
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, subscription.isInstallments());
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar a assinatura.", e
            );
        }
    }

    /**
     * Ativa uma assinatura pelo ID.
     *
     * @param id identificador da assinatura
     * @return true se algum registro foi atualizado
     */
    public boolean activateSubscription(int id) {
        String sql =
                "UPDATE subscriptions " +
                        "SET is_active = true, " +
                        "deactivated_at = NULL, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao ativar a assinatura.", e
            );
        }
    }

    /**
     * Desativa uma assinatura pelo ID.
     *
     * @param id identificador da assinatura
     * @return true se algum registro foi atualizado
     */
    public boolean deactivateSubscription(int id) {
        return delete(id);
    }

    /**
     * Converte o resultado SQL em um objeto Subscriptions.
     *
     * @param rs resultado da consulta
     * @return assinatura preenchida com os dados do banco
     * @throws SQLException se ocorrer um erro ao ler os dados
     */
    private Subscriptions mapSubscription(ResultSet rs)
            throws SQLException {

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

        subscription.setDeactivatedAt(
                rs.getTimestamp("deactivated_at")
        );
        subscription.setCreatedAt(
                rs.getTimestamp("created_at")
        );
        subscription.setUpdatedAt(
                rs.getTimestamp("updated_at")
        );

        return subscription;
    }
}