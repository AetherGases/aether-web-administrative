
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.PlansFilter;
import com.example.servelet1ano.model.Plans;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlansDAO implements DAOI<Plans, PlansFilter> {

    private Connection getConnection() {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    /**
     * Preenche um objeto Plans com os dados do ResultSet.
     */
    private Plans mapPlan(ResultSet rs) throws SQLException {
        Plans plan = new Plans();

        plan.setId(rs.getInt("id"));
        plan.setName(rs.getString("name"));
        plan.setDescription(rs.getString("description"));
        plan.setPrice(rs.getDouble("price"));
        plan.setDurationDays(rs.getInt("duration_days"));
        plan.setCurrency(rs.getString("currency"));
        plan.setActive(rs.getBoolean("is_active"));
        plan.setCreatedAt(rs.getDate("created_at"));
        plan.setUpdatedAt(rs.getDate("updated_at"));

        return plan;
    }

    /**
     * Busca os planos ativos, filtrando pelo nome quando informado.
     */
    @Override
    public List<Plans> searchAll(PlansFilter filter) {

        List<Plans> plans = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM plans WHERE is_active = true"
        );

        String name = filter == null ? null : filter.getName();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(name) = LOWER(?)");
        }

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {
            if (name != null && !name.isBlank()) {
                pstmt.setString(1, name.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    plans.add(mapPlan(rs));
                }
            }

            return plans;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar planos ativos.",
                    e
            );
        }
    }

    /**
     * Busca um plano ativo pelo ID.
     */
    @Override
    public Plans searchById(int id) {

        String sql =
                "SELECT * FROM plans " +
                        "WHERE id = ? AND is_active = true";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapPlan(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar plano pelo ID.",
                    e
            );
        }
    }

    /**
     * Busca um plano ativo pelo nome, ignorando maiúsculas e minúsculas.
     */
    public Plans searchByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String sql =
                "SELECT * FROM plans " +
                        "WHERE LOWER(name) = LOWER(?) " +
                        "AND is_active = true";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, name.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapPlan(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar plano pelo nome.",
                    e
            );
        }
    }

    /**
     * O cadastro de planos não é permitido.
     */
    @Override
    public boolean register(Plans entity) {
        throw new UnsupportedOperationException(
                "O cadastro de planos não é permitido."
        );
    }

    /**
     * A atualização de planos não é permitida.
     */
    @Override
    public boolean update(Plans entity, int id) {
        throw new UnsupportedOperationException(
                "A atualização de planos não é permitida."
        );
    }

    /**
     * A exclusão de planos não é permitida.
     */
    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException(
                "A exclusão de planos não é permitida."
        );
    }
}
