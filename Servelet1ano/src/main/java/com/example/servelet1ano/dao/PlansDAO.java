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
     * Metodo que busca os planos ativos no banco aplicando os filtros informados.
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * O nome é comparado inteiro, sem diferenciar maiusculas de minusculas,
     * e o preco minimo e maximo entram na consulta (inclusive os valores informados)
     *
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return Lista de Plans com os planos encontrados.
     * Retorna uma lista vazia caso nenhum plano seja encontrado
     * ou ocorra um erro na consulta.
     */
    @Override
    public List<Plans> searchAll(PlansFilter filter) {


        List<Plans> plans = new ArrayList<>();

        if (filter == null) {
            filter = new PlansFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select * " +
                        "from plans " +
                        "where is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and LOWER(name) = LOWER(?)");
            parametros.add(filter.getName().trim());
        }

        if (filter.getMinPrice() != null) {
            sql.append(" and price >= ?");
            parametros.add(filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            sql.append(" and price <= ?");
            parametros.add(filter.getMaxPrice());
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return plans;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
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