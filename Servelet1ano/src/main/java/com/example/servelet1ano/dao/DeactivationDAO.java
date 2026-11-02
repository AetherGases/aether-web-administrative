package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;

/** Operações de desativação em lote, com transação e restrição por empresa. */
public class DeactivationDAO {

    /** Não desativa outras empresas, gerentes nem a própria conta. */
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

    /** Setor deve ser ativo, pertencer à empresa e não ter empregados ativos. */
    public boolean sectors(List<Integer> ids, int companyId) {
        if (!valid(ids, companyId)) return false;
        String sql = "UPDATE sectors s SET is_active=FALSE, updated_at=CURRENT_TIMESTAMP " +
                "WHERE s.company_id=? AND s.is_active=TRUE AND s.id IN (" + placeholders(ids.size()) + ") " +
                "AND NOT EXISTS (SELECT 1 FROM employees e WHERE e.sector_id=s.id AND e.is_active=TRUE)";
        return updateAll(sql, ids, companyId, null);
    }

    /** Unidades e seus setores são desativados na mesma transação
     *
     * Nenhuma unidade com funcionário ativo pode ser aceita
     * . */
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

    private static boolean valid(List<Integer> ids, int companyId) {
        return companyId > 0 && ids != null && !ids.isEmpty() && ids.size() <= 100 &&
                ids.stream().allMatch(id -> id != null && id > 0) &&
                ids.stream().distinct().count() == ids.size();
    }

    private static String placeholders(int size) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < size; i++) sj.add("?");
        return sj.toString();
    }

    private static void bindCompanyIds(PreparedStatement ps, int companyId, List<Integer> ids)
            throws SQLException {
        ps.setInt(1, companyId);
        for (int i = 0; i < ids.size(); i++) ps.setInt(i + 2, ids.get(i));
    }
}
