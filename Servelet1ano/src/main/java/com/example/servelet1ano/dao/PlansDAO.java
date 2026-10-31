package com.example.servelet1ano.dao;

import com.example.servelet1ano.filter.PlansFilter;
import com.example.servelet1ano.model.Plans;
import com.example.servelet1ano.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlansDAO implements DAOI<Plans, PlansFilter>  {

    /**
     * Metodo que busca os planos ativos no banco aplicando os filtros informados.
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados.
     * O nome precisa ser igual (sem diferenciar maiusculas de minusculas) e o preco
     * aceita um minimo e/ou um maximo (inclusive).
     *
     * @param filter Objeto com os campos opcionais para filtrar a busca (se for null, busca todos)
     * @return Lista de Plans com os planos encontrados.
     * Retorna uma lista vazia caso nenhum plano seja encontrado.
     */
    @Override
    public List<Plans> searchAll(PlansFilter filter) {

        if (filter == null) {
            filter = new PlansFilter();
        }

        List<Plans> plans = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select * from plans where is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and lower(name) = lower(?)");
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

        sql.append(" order by id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    plans.add(mapPlan(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os planos no banco de dados.", e);
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
                    return mapPlan(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o plano de id " + id + " no banco de dados.", e);
        }
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

        PlansFilter filter = new PlansFilter();
        filter.setName(name);

        List<Plans> plans = searchAll(filter);

        return plans.isEmpty() ? null : plans.get(0);
    }

    /**
     * Monta um plano a partir da linha atual do ResultSet
     */
    private Plans mapPlan(ResultSet rs) throws SQLException {

        Plans plan = new Plans();

        plan.setId(rs.getInt("id"));
        plan.setName(rs.getString("name"));
        plan.setDescription(rs.getString("description"));
        plan.setPrice(rs.getBigDecimal("price"));
        plan.setDurationDays(rs.getInt("duration_days"));
        plan.setCurrency(rs.getString("currency"));
        plan.setActive(rs.getBoolean("is_active"));
        plan.setCreatedAt(rs.getTimestamp("created_at"));
        plan.setUpdatedAt(rs.getTimestamp("updated_at"));

        return plan;
    }

    /**
     * metodo que nao e permitido: os planos nao podem ser cadastrados pelo sistema
     * Existe so porque a interface DAOI exige
     * @param entity O plano (nao e usado)
     * @return Nunca retorna, sempre lanca excecao
     * @throws UnsupportedOperationException sempre
     */
    @Override
    public boolean register(Plans entity) {
        throw new UnsupportedOperationException(
                "O cadastro de planos não é permitido."
        );
    }

    /**
     * metodo que nao e permitido: os planos nao podem ser atualizados pelo sistema
     * Existe so porque a interface DAOI exige
     * @param entity O plano (nao e usado)
     * @param id O id do plano (nao e usado)
     * @return Nunca retorna, sempre lanca excecao
     * @throws UnsupportedOperationException sempre
     */
    @Override
    public boolean update(Plans entity, int id) {
        throw new UnsupportedOperationException(
                "A atualização de planos não é permitida."
        );
    }

    /**
     * metodo que nao e permitido: os planos nao podem ser excluidos (desativados) pelo sistema
     * Existe so porque a interface DAOI exige
     * @param id O id do plano (nao e usado)
     * @return Nunca retorna, sempre lanca excecao
     * @throws UnsupportedOperationException sempre
     */
    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException(
                "A exclusão de planos não é permitida."
        );
    }
}