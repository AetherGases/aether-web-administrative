
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.SectorsFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.Units;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectorsDAO implements DAOI<Sectors, SectorsFilter> {

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

    private Sectors mapSector(ResultSet rs) throws SQLException {
        Companies company = new Companies(
                rs.getInt("idCompany"),
                rs.getString("companyName")
        );

        Units unit = new Units(
                rs.getInt("idUnit"),
                rs.getString("unitName")
        );

        Sectors sector = new Sectors(
                rs.getInt("id"),
                rs.getInt("unitId"),
                rs.getInt("companyId"),
                rs.getString("name"),
                rs.getString("description"),
                company,
                unit
        );

        sector.setCreatedAt(rs.getTimestamp("created_at"));
        sector.setUpdatedAt(rs.getTimestamp("updated_at"));

        return sector;
    }

    private static final String SELECT_SECTORS =
            "SELECT s.*, " +
                    "s.company_id AS companyId, " +
                    "s.unit_id AS unitId, " +
                    "c.id AS idCompany, c.name AS companyName, " +
                    "u.id AS idUnit, u.name AS unitName " +
                    "FROM sectors s " +
                    "JOIN companies c ON c.id = s.company_id " +
                    "JOIN units u ON u.id = s.unit_id ";

    private void bindSector(
            PreparedStatement pstmt,
            Sectors sector
    ) throws SQLException {
        pstmt.setInt(1, sector.getUnitId());
        pstmt.setInt(2, sector.getCompanyId());
        pstmt.setString(3, sector.getName());
        pstmt.setString(4, sector.getDescription());
    }

    private boolean belongsToUnit(Sectors sector, int unitId) {
        return sector != null && sector.getUnitId() == unitId;
    }

    @Override
    public List<Sectors> searchAll(SectorsFilter filter) {
        List<Sectors> sectors = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                SELECT_SECTORS + "WHERE s.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter != null && filter.getId() != null) {
            sql.append(" AND s.id = ?");
            parametros.add(filter.getId());
        }

        if (filter != null
                && filter.getName() != null
                && !filter.getName().isBlank()) {
            sql.append(" AND s.name ILIKE ?");
            parametros.add("%" + filter.getName() + "%");
        }

        if (filter != null
                && filter.getDescription() != null
                && !filter.getDescription().isBlank()) {
            sql.append(" AND s.description ILIKE ?");
            parametros.add("%" + filter.getDescription() + "%");
        }

        if (filter != null && filter.getUnitId() != null) {
            sql.append(" AND u.id = ?");
            parametros.add(filter.getUnitId());
        }

        if (filter != null && filter.getCompanyId() != null) {
            sql.append(" AND c.id = ?");
            parametros.add(filter.getCompanyId());
        }

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sectors.add(mapSector(rs));
                }
            }

            return sectors;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar setores.",
                    e
            );
        }
    }

    @Override
    public Sectors searchById(int id) {
        String sql =
                SELECT_SECTORS +
                        "WHERE s.id = ? AND s.is_active = true";

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapSector(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar setor por ID.",
                    e
            );
        }
    }

    @Override
    public boolean register(Sectors sector) {
        if (sector == null) {
            return false;
        }

        String sql =
                "INSERT INTO sectors (unit_id, company_id, name, description) " +
                        "VALUES (?, ?, ?, ?)";

        return executeUpdate(
                sql,
                pstmt -> bindSector(pstmt, sector),
                "Erro ao cadastrar setor."
        );
    }

    public boolean registerLot(List<Sectors> sectors) {
        if (sectors == null || sectors.isEmpty()
                || sectors.stream().anyMatch(s -> s == null)) {
            return false;
        }

        return executeBatch(sectors, null, "Erro ao cadastrar setores em lote.");
    }

    public boolean delete(int id) {
        return executeUpdate(
                "UPDATE sectors SET is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                pstmt -> pstmt.setInt(1, id),
                "Erro ao desativar setor."
        );
    }

    public boolean deleteByUnitId(int unitId) {
        return executeUpdate(
                "UPDATE sectors SET is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE unit_id = ?",
                pstmt -> pstmt.setInt(1, unitId),
                "Erro ao desativar setores da unidade."
        );
    }

    public boolean deleteByCompanyId(int companyId) {
        return executeUpdate(
                "UPDATE sectors SET is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE company_id = ?",
                pstmt -> pstmt.setInt(1, companyId),
                "Erro ao desativar setores da empresa."
        );
    }

    public boolean deleteByName(String name) {
        return executeUpdate(
                "UPDATE sectors SET is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE name ILIKE ?",
                pstmt -> pstmt.setString(1, name),
                "Erro ao desativar setor por nome."
        );
    }

    @Override
    public boolean update(Sectors sector, int id) {
        if (sector == null) {
            return false;
        }

        String sql =
                "UPDATE sectors SET unit_id = ?, company_id = ?, " +
                        "name = ?, description = ?, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        return executeUpdate(
                sql,
                pstmt -> {
                    bindSector(pstmt, sector);
                    pstmt.setInt(5, id);
                },
                "Erro ao atualizar setor."
        );
    }

    public boolean updateByName(Sectors sector, String name) {
        if (sector == null) {
            return false;
        }

        String sql =
                "UPDATE sectors SET unit_id = ?, company_id = ?, " +
                        "name = ?, description = ?, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE name ILIKE ?";

        return executeUpdate(
                sql,
                pstmt -> {
                    bindSector(pstmt, sector);
                    pstmt.setString(5, name);
                },
                "Erro ao atualizar setor por nome."
        );
    }

    // Métodos específicos para administradores de unidades

    public boolean registerPerUnit(Sectors sector, int unitId) {
        if (!belongsToUnit(sector, unitId)) {
            return false;
        }

        return register(sector);
    }

    public boolean registerLotPerUnit(List<Sectors> sectors, int unitId) {
        if (sectors == null || sectors.isEmpty()) {
            return false;
        }

        for (Sectors sector : sectors) {
            if (!belongsToUnit(sector, unitId)) {
                return false;
            }
        }

        return executeBatch(
                sectors,
                unitId,
                "Erro ao cadastrar setores da unidade em lote."
        );
    }

    public boolean deleteByIdPerUnit(int id, int unitId) {
        Sectors sector = searchById(id);

        if (!belongsToUnit(sector, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE sectors SET is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE id = ? AND unit_id = ?",
                pstmt -> {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, unitId);
                },
                "Erro ao desativar setor da unidade."
        );
    }

    public boolean updateByIdPerUnit(Sectors sector, int id, int unitId) {
        if (!belongsToUnit(sector, unitId)) {
            return false;
        }

        Sectors existing = searchById(id);

        if (!belongsToUnit(existing, unitId)) {
            return false;
        }

        String sql =
                "UPDATE sectors SET unit_id = ?, company_id = ?, " +
                        "name = ?, description = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND unit_id = ?";

        return executeUpdate(
                sql,
                pstmt -> {
                    bindSector(pstmt, sector);
                    pstmt.setInt(5, id);
                    pstmt.setInt(6, unitId);
                },
                "Erro ao atualizar setor da unidade."
        );
    }

    public boolean updateByNamePerUnit(
            Sectors sector,
            String name,
            int unitId
    ) {
        if (!belongsToUnit(sector, unitId)) {
            return false;
        }

        String sql =
                "UPDATE sectors SET unit_id = ?, company_id = ?, " +
                        "name = ?, description = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ? AND unit_id = ?";

        return executeUpdate(
                sql,
                pstmt -> {
                    bindSector(pstmt, sector);
                    pstmt.setString(5, name);
                    pstmt.setInt(6, unitId);
                },
                "Erro ao atualizar setor por nome na unidade."
        );
    }

    private boolean executeBatch(
            List<Sectors> sectors,
            Integer unitId,
            String errorMessage
    ) {
        String sql =
                "INSERT INTO sectors (unit_id, company_id, name, description) " +
                        "VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = requireConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Sectors sector : sectors) {
                    if (sector == null
                            || (unitId != null
                            && sector.getUnitId() != unitId)) {
                        conn.rollback();
                        return false;
                    }

                    bindSector(pstmt, sector);
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

                throw new RuntimeException(errorMessage, e);
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }

            throw new RuntimeException(errorMessage, e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(
                            "Erro ao fechar conexão do cadastro em lote.",
                            e
                    );
                }
            }
        }
    }
}
