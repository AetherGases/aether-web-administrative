package com.example.servelet1ano.dao;

import com.example.servelet1ano.filter.PlansFilter;
import com.example.servelet1ano.model.Plans;
import com.example.servelet1ano.connection.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlansDAO implements DAOI<Plans, PlansFilter> {

    /**
     * Abre a conexao com o banco
     * @return A conexao aberta
     * @throws IllegalStateException se nao for possivel conectar
     */
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
     * Preenche um objeto Plans com os dados da linha atual do ResultSet
     * @param rs O ResultSet posicionado na linha que sera lida
     * @return O Plans com os dados da linha
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
     * Metodo que busca os planos ativos no banco aplicando os filtros informados.
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * O nome é comparado inteiro, sem diferenciar maiusculas de minusculas,
     * e o preco minimo e maximo entram na consulta (inclusive os valores informados)
     *
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return Lista de Plans com os planos encontrados.
     * Retorna uma lista vazia caso nenhum plano seja encontrado
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

        try (
                Connection conn = getConnection();
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

            return plans;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar planos ativos.",
                    e
            );
        }
    }


    /**
     * Metodo que busca um plano ativo pelo seu ID.
     *
     * @param id Identificador unico do plano.
     * @return Plans encontrado ou null caso nao exista.
     */
    @Override
    public Plans searchById(int id) {

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * " +
                                "from plans " +
                                "where id = ? and is_active = true"
                )
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

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * " +
                                "from plans " +
                                "where LOWER(name) = LOWER(?) " +
                                "and is_active = true"
                )
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
     * O cadastro de planos nao é permitido.
     */
    @Override
    public boolean register(Plans entity) {
        throw new UnsupportedOperationException(
                "O cadastro de planos não é permitido."
        );
    }

    /**
     * A atualizacao de planos nao é permitida.
     */
    @Override
    public boolean update(Plans entity, int id) {
        throw new UnsupportedOperationException(
                "A atualização de planos não é permitida."
        );
    }

    /**
     * A exclusao de planos nao é permitida.
     */
    @Override
    public boolean delete(int id) {
        throw new UnsupportedOperationException(
                "A exclusão de planos não é permitida."
        );
    }
}