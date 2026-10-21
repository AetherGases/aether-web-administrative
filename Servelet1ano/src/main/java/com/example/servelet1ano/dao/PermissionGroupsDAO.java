
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.PermissionGroupsFilter;
import com.example.servelet1ano.model.PermissionGroups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionGroupsDAO implements DAOI<PermissionGroups, PermissionGroupsFilter> {

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }

    private Connection requireConnection() throws SQLException {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    private boolean executeUpdate(
            String sql,
            StatementBinder binder,
            String errorMessage
    ) {
        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            binder.bind(pstmt);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    private PermissionGroups mapPermissionGroup(ResultSet rs)
            throws SQLException {

        return new PermissionGroups(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getObject("company_id", Integer.class)
        );
    }

    @Override
    public List<PermissionGroups> searchAll(PermissionGroupsFilter filter) {

        List<PermissionGroups> permissionGroups = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM permission_groups WHERE is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        Integer companyId = filter == null ? null : filter.getCompanyId();

        if (companyId == null) {
            sql.append(" AND company_id IS NULL");
        } else {
            sql.append(" AND company_id = ?");
            parametros.add(companyId);
        }

        if (filter != null && filter.getId() != null) {
            sql.append(" AND id = ?");
            parametros.add(filter.getId());
        }

        if (filter != null
                && filter.getName() != null
                && !filter.getName().isBlank()) {

            sql.append(" AND name ILIKE ?");
            parametros.add("%" + filter.getName() + "%");
        }

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    permissionGroups.add(mapPermissionGroup(rs));
                }
            }

            return permissionGroups;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar grupos de permissão.",
                    e
            );
        }
    }

    @Override
    public PermissionGroups searchById(int id) {

        String sql =
                "SELECT * FROM permission_groups " +
                        "WHERE id = ? AND is_active = true";

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapPermissionGroup(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar grupo de permissão por ID.",
                    e
            );
        }
    }

    @Override
    public boolean register(PermissionGroups permissionGroup) {

        if (permissionGroup == null) {
            return false;
        }

        String sql =
                "INSERT INTO permission_groups (name, company_id) " +
                        "VALUES (?, ?)";

        return executeUpdate(
                sql,
                pstmt -> {
                    pstmt.setString(1, permissionGroup.getName());
                    pstmt.setObject(2, permissionGroup.getCompanyId());
                },
                "Erro ao cadastrar grupo de permissão."
        );
    }

    public boolean registerLot(List<PermissionGroups> permissionGroups) {

        if (permissionGroups == null || permissionGroups.isEmpty()) {
            return false;
        }

        for (PermissionGroups group : permissionGroups) {
            if (group == null) {
                return false;
            }
        }

        String sql =
                "INSERT INTO permission_groups (name, company_id) " +
                        "VALUES (?, ?)";

        Connection conn = null;

        try {
            conn = requireConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (PermissionGroups group : permissionGroups) {
                    pstmt.setString(1, group.getName());
                    pstmt.setObject(2, group.getCompanyId());
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw new RuntimeException(
                        "Erro ao cadastrar grupos de permissão em lote.",
                        e
                );
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }

            throw new RuntimeException(
                    "Erro ao executar cadastro em lote.",
                    e
            );

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(
                            "Erro ao fechar conexão após cadastro em lote.",
                            e
                    );
                }
            }
        }
    }

    @Override
    public boolean delete(int id) {

        String sql =
                "UPDATE permission_groups " +
                        "SET is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        return executeUpdate(
                sql,
                pstmt -> pstmt.setInt(1, id),
                "Erro ao desativar grupo de permissão por ID."
        );
    }

    public boolean deleteByName(String name) {

        String sql =
                "UPDATE permission_groups " +
                        "SET is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ?";

        return executeUpdate(
                sql,
                pstmt -> pstmt.setString(1, name),
                "Erro ao desativar grupo de permissão por nome."
        );
    }

    public boolean deleteByCompanyId(int companyId) {

        String sql =
                "UPDATE permission_groups " +
                        "SET is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE company_id = ?";

        return executeUpdate(
                sql,
                pstmt -> pstmt.setInt(1, companyId),
                "Erro ao desativar grupos de permissão da empresa."
        );
    }

    @Override
    public boolean update(PermissionGroups permissionGroup, int id) {

        if (permissionGroup == null) {
            return false;
        }

        String sql =
                "UPDATE permission_groups SET " +
                        "name = ?, company_id = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        return executeUpdate(
                sql,
                pstmt -> {
                    pstmt.setString(1, permissionGroup.getName());
                    pstmt.setObject(2, permissionGroup.getCompanyId());
                    pstmt.setInt(3, id);
                },
                "Erro ao atualizar grupo de permissão por ID."
        );
    }

    public boolean updateByName(
            PermissionGroups permissionGroup,
            String name
    ) {

        if (permissionGroup == null) {
            return false;
        }

        String sql =
                "UPDATE permission_groups SET " +
                        "name = ?, company_id = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ?";

        return executeUpdate(
                sql,
                pstmt -> {
                    pstmt.setString(1, permissionGroup.getName());
                    pstmt.setObject(2, permissionGroup.getCompanyId());
                    pstmt.setString(3, name);
                },
                "Erro ao atualizar grupo de permissão por nome."
        );
    }
}
