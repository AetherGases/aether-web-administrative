package com.example.servelet1ano.dao;

import com.example.servelet1ano.dao.DAOI;
import com.example.servelet1ano.filter.PlansFilter;
import com.example.servelet1ano.model.Plans;
import com.example.servelet1ano.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlansDAO implements DAOI<Plans, PlansFilter>  {
    /**
     * Metodo que busca todos os planos ativos no banco.
     *
     * @return Lista de Plans com os planos encontrados.
     * Retorna uma lista vazia caso nenhum plano seja encontrado
     * ou ocorra um erro na consulta.
     */
    @Override
    public List<Plans> searchAll(PlansFilter filter) {


        List<Plans> plans = new ArrayList<>();

        String sql =
                "select * " +
                        "from plans " +
                        "where is_active = true " +
                        "and (? is null or LOWER(name) = LOWER(?))";

        String name = (filter == null) ? null : filter.getName();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, name);
            pstmt.setString(2, name);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

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

                    plans.add(plan);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }


    /**
     * Metodo que busca um plano ativo pelo seu ID.
     *
     * @param id Identificador unico do plano.
     * @return Plans encontrado ou null caso nao exista.
     */

    @Override
    public Plans searchById(int id) {

        Plans plan = null;

        String sql =
                "select * " +
                        "from plans " +
                        "where id = ? and is_active = true";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    plan = new Plans();

                    plan.setId(rs.getInt("id"));
                    plan.setName(rs.getString("name"));
                    plan.setDescription(rs.getString("description"));
                    plan.setPrice(rs.getDouble("price"));
                    plan.setDurationDays(rs.getInt("duration_days"));
                    plan.setCurrency(rs.getString("currency"));
                    plan.setActive(rs.getBoolean("is_active"));
                    plan.setCreatedAt(rs.getDate("created_at"));
                    plan.setUpdatedAt(rs.getDate("updated_at"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plan;
    }


    /**
     * Metodo que busca um plano ativo pelo nome.
     *
     * A comparacao ignora diferencas entre letras maiusculas
     * e minusculas.
     *
     * @param name Nome completo do plano.
     * @return Plans encontrado ou null caso nao exista.
     */
    public Plans searchByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        Plans plan = null;

        String sql =
                "select * " +
                        "from plans " +
                        "where LOWER(name) = LOWER(?) " +
                        "and is_active = true";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, name.trim());

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    plan = new Plans();

                    plan.setId(rs.getInt("id"));
                    plan.setName(rs.getString("name"));
                    plan.setDescription(rs.getString("description"));
                    plan.setPrice(rs.getDouble("price"));
                    plan.setDurationDays(rs.getInt("duration_days"));
                    plan.setCurrency(rs.getString("currency"));
                    plan.setActive(rs.getBoolean("is_active"));
                    plan.setCreatedAt(rs.getDate("created_at"));
                    plan.setUpdatedAt(rs.getDate("updated_at"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plan;
    }
    @Override
    public boolean register(Plans entity) {
        throw new UnsupportedOperationException(
                "O cadastro de planos não é permitido."
        );
    }

    @Override
    public boolean update(Plans entity, int id) {
        throw new UnsupportedOperationException(
                "A atualização de planos não é permitida."
        );
    }

    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException(
                "A exclusão de planos não é permitida."
        );
    }
}
