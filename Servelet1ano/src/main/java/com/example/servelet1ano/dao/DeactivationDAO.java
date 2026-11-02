package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;


public class DeactivationDAO {

    /**
     * Desativa em lote os funcionários selecionados de uma empresa.
     * Impede a desativação da própria conta e dos administradores.
     *
     * @param ids Os identificadores dos funcionários selecionados
     * @param companyId O identificador da empresa
     * @param loggedEmployeeId O identificador do funcionário autenticado
     * @return true se todos os registros forem desativados
     */

    public boolean employees(List<Integer> ids, int companyId, int loggedEmployeeId) {
        if (!valid(ids, companyId) || loggedEmployeeId <= 0) return false;
        String sql = "UPDATE employees e SET is_active=FALSE, status='INACTIVE', " +
                "updated_at=CURRENT_TIMESTAMP WHERE e.company_id=? AND e.is_active=TRUE " +
                "AND e.id <> ? AND e.id IN (" + placeholders(ids.size()) + ") " +
                "AND EXISTS (SELECT 1 FROM permission_groups pg " +
                "WHERE pg.id=e.permission_group_id AND pg.company_id=e.company_id " +
                "AND pg.is_active=TRUE AND pg.name <> 'Admin da empresa')";
        return updateAll(sql, ids, companyId, loggedEmployeeId);
    }

    /**
     * Desativa em lote setores ativos pertencentes à empresa.
     * Impede a desativação de setores com funcionários ativos.
     *
     * @param ids Os identificadores dos setores selecionados
     * @param companyId O identificador da empresa
     * @return true se todos os setores forem desativados
     */

    public boolean sectors(List<Integer> ids, int companyId) {
        if (!valid(ids, companyId)) return false;
        String sql = "UPDATE sectors s SET is_active=FALSE, updated_at=CURRENT_TIMESTAMP " +
                "WHERE s.company_id=? AND s.is_active=TRUE AND s.id IN (" + placeholders(ids.size()) + ") " +
                "AND NOT EXISTS (SELECT 1 FROM employees e WHERE e.sector_id=s.id AND e.is_active=TRUE)";
        return updateAll(sql, ids, companyId, null);
    }

    /**
     * Desativa unidades e seus setores em uma única transação.
     * Verifica se todas as unidades pertencem à empresa e se não possuem
     * funcionários ativos antes de confirmar as alterações.
     *
     * @param ids Os identificadores das unidades selecionadas
     * @param companyId O identificador da empresa
     * @return true se todas as unidades forem desativadas
     */

    public boolean units(List<Integer> ids, int companyId) {
        if (!valid(ids, companyId)) return false;
        String in = placeholders(ids.size());
        try (Connection conn = ConnectionFactory.connect()) {
            conn.setAutoCommit(false);
            try {
                // Bloqueio que evita que os registros selecionados sejam alterados simultaneamente.
                String lock = "SELECT id FROM units WHERE company_id=? AND is_active=TRUE " +
                        "AND id IN (" + in + ") FOR UPDATE";
                int encontrados = 0;
                try (PreparedStatement ps = conn.prepareStatement(lock)) {
                    bindCompanyIds(ps, companyId, ids);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) encontrados++;
                    }
                }
                if (encontrados != ids.size()) { conn.rollback(); return false; }

                String check = "SELECT 1 FROM employees WHERE company_id=? AND is_active=TRUE " +
                        "AND unit_id IN (" + in + ") LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(check)) {
                    bindCompanyIds(ps, companyId, ids);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { conn.rollback(); return false; }
                    }
                }
                String sectors = "UPDATE sectors SET is_active=FALSE, updated_at=CURRENT_TIMESTAMP " +
                        "WHERE company_id=? AND is_active=TRUE AND unit_id IN (" + in + ")";
                try (PreparedStatement ps = conn.prepareStatement(sectors)) {
                    bindCompanyIds(ps, companyId, ids);
                    ps.executeUpdate();
                }
                String units = "UPDATE units SET is_active=FALSE, updated_at=CURRENT_TIMESTAMP " +
                        "WHERE company_id=? AND is_active=TRUE AND id IN (" + in + ")";
                int count;
                try (PreparedStatement ps = conn.prepareStatement(units)) {
                    bindCompanyIds(ps, companyId, ids);
                    count = ps.executeUpdate();
                }
                if (count != ids.size()) { conn.rollback(); return false; }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao desativar unidades em lote.", e);
        }
    }


    /**
     * Executa uma atualização SQL em lote utilizando uma transação.
     * Confirma a operação somente quando todos os registros são atualizados.
     *
     * @param sql A instrução SQL parametrizada
     * @param ids Os identificadores dos registros selecionados
     * @param companyId O identificador da empresa
     * @param excludedId O identificador excluído da operação, quando aplicável
     * @return true se a quantidade de registros atualizados for a esperada
     */

    private boolean updateAll(String sql, List<Integer> ids, int companyId, Integer excludedId) {
        try (Connection conn = ConnectionFactory.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int pos = 1;
                ps.setInt(pos++, companyId);
                if (excludedId != null) ps.setInt(pos++, excludedId);
                for (int id : ids) ps.setInt(pos++, id);
                if (ps.executeUpdate() != ids.size()) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao desativar registros em lote.", e);
        }
    }


    /**
     * Verifica se a seleção de registros é válida para a operação em lote.
     * Exige empresa válida e entre 1 e 100 identificadores positivos e únicos.
     *
     * @param ids Os identificadores selecionados
     * @param companyId O identificador da empresa
     * @return true se a seleção atender às regras
     */

    private static boolean valid(List<Integer> ids, int companyId) {
        return companyId > 0 && ids != null && !ids.isEmpty() && ids.size() <= 100 &&
                ids.stream().allMatch(id -> id != null && id > 0) &&
                ids.stream().distinct().count() == ids.size();
    }

    /**
     * Monta a quantidade de marcadores de parâmetros necessária para uma consulta SQL.
     *
     * @param size A quantidade de identificadores
     * @return Uma sequência de marcadores de interrogação separados por vírgulas
     */

    private static String placeholders(int size) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < size; i++) sj.add("?");
        return sj.toString();
    }


    /**
     * Associa o identificador da empresa e os IDs selecionados ao PreparedStatement.
     *
     * @param ps O comando SQL preparado
     * @param companyId O identificador da empresa
     * @param ids Os identificadores que serão associados aos parâmetros
     * @throws SQLException Se ocorrer erro ao configurar os parâmetros
     */

    private static void bindCompanyIds(PreparedStatement ps, int companyId, List<Integer> ids)
            throws SQLException {
        ps.setInt(1, companyId);
        for (int i = 0; i < ids.size(); i++) ps.setInt(i + 2, ids.get(i));
    }
}
