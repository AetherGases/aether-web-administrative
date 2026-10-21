
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.UnitsFilter;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Units;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitsDAO implements DAOI<Units, UnitsFilter> {

    private Connection requireConnection() {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    private static final String SELECT_UNITS =
            "SELECT u.*, " +
                    "u.company_id AS companyId, " +
                    "u.address_id AS addressId, " +
                    "c.name AS companyName, " +
                    "c.id AS idCompany, " +
                    "a.id AS idAddress, " +
                    "a.zip_code AS addressZipCode, " +
                    "a.street AS addressStreet, " +
                    "a.number AS addressNumber, " +
                    "a.complement AS addressComplement, " +
                    "a.neighborhood AS addressNeighborhood, " +
                    "a.city AS addressCity, " +
                    "a.state AS addressState, " +
                    "a.country AS addressCountry " +
                    "FROM units u " +
                    "JOIN companies c ON u.company_id = c.id " +
                    "JOIN addresses a ON a.id = u.address_id ";

    private Units mapUnit(ResultSet rs) throws SQLException {

        Companies company = new Companies(
                rs.getInt("idCompany"),
                rs.getString("companyName")
        );

        Addresses address = new Addresses(
                rs.getInt("idAddress"),
                rs.getString("addressZipCode"),
                rs.getString("addressStreet"),
                rs.getString("addressNumber"),
                rs.getString("addressComplement"),
                rs.getString("addressNeighborhood"),
                rs.getString("addressCity"),
                rs.getString("addressState"),
                rs.getString("addressCountry")
        );

        Units unit = new Units(
                rs.getInt("id"),
                rs.getInt("companyId"),
                rs.getInt("addressId"),
                rs.getString("name"),
                rs.getString("cnpj"),
                rs.getString("cnae"),
                rs.getBoolean("is_active"),
                company,
                address
        );

        unit.setCreatedAt(rs.getTimestamp("created_at"));
        unit.setUpdatedAt(rs.getTimestamp("updated_at"));

        return unit;
    }

    /*
     * Busca unidades ativas aplicando os filtros informados.
     */
    @Override
    public List<Units> searchAll(UnitsFilter filter) {

        List<Units> units = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                SELECT_UNITS + "WHERE u.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter != null) {

            if (filter.getId() != null) {
                sql.append(" AND u.id = ?");
                parametros.add(filter.getId());
            }

            if (filter.getCompanyId() != null) {
                sql.append(" AND u.company_id = ?");
                parametros.add(filter.getCompanyId());
            }

            if (filter.getCompanyName() != null
                    && !filter.getCompanyName().isBlank()) {
                sql.append(" AND c.name ILIKE ?");
                parametros.add("%" + filter.getCompanyName() + "%");
            }

            if (filter.getName() != null
                    && !filter.getName().isBlank()) {
                sql.append(" AND u.name ILIKE ?");
                parametros.add("%" + filter.getName() + "%");
            }

            if (filter.getCnpj() != null
                    && !filter.getCnpj().isBlank()) {
                sql.append(" AND u.cnpj = ?");
                parametros.add(filter.getCnpj());
            }

            if (filter.getCnae() != null
                    && !filter.getCnae().isBlank()) {
                sql.append(" AND u.cnae = ?");
                parametros.add(filter.getCnae());
            }
        }

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    units.add(mapUnit(rs));
                }
            }

            return units;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar unidades.", e
            );
        }
    }

    /*
     * Busca uma unidade ativa pelo ID.
     */
    @Override
    public Units searchById(int id) {

        String sql = SELECT_UNITS
                + "WHERE u.id = ? AND u.is_active = true";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUnit(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar unidade pelo ID: " + id, e
            );
        }
    }

    /*
     * Cadastra uma unidade ativa.
     */
    @Override
    public boolean register(Units unit) {

        if (unit == null) {
            return false;
        }

        String sql =
                "INSERT INTO units " +
                        "(company_id, address_id, name, cnpj, cnae, is_active) " +
                        "VALUES (?, ?, ?, ?, ?, true)";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar unidade.", e
            );
        }
    }

    /*
     * Cadastra várias unidades em uma única transação.
     */
    public boolean registerLot(List<Units> units) {

        if (units == null || units.isEmpty()) {
            return false;
        }

        String sql =
                "INSERT INTO units " +
                        "(company_id, address_id, name, cnpj, cnae, is_active) " +
                        "VALUES (?, ?, ?, ?, ?, true)";

        try (Connection conn = requireConnection()) {

            boolean originalAutoCommit = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    for (Units unit : units) {

                        if (unit == null) {
                            throw new IllegalArgumentException(
                                    "A lista contém uma unidade nula."
                            );
                        }

                        pstmt.setInt(1, unit.getCompanyId());
                        pstmt.setInt(2, unit.getAddressId());
                        pstmt.setString(3, unit.getName());
                        pstmt.setString(4, unit.getCnpj());
                        pstmt.setString(5, unit.getCnae());

                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                if (e instanceof SQLException) {
                    throw new RuntimeException(
                            "Erro ao cadastrar unidades em lote.", e
                    );
                }

                throw e;

            } finally {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException e) {
                    throw new RuntimeException(
                            "Erro ao restaurar a configuração da conexão.", e
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro na conexão ao cadastrar unidades em lote.", e
            );
        }
    }

    /*
     * Desativa a unidade e solicita a desativação
     * de seus setores e funcionários.
     */
    @Override
    public boolean delete(int id) {

        // Se qualquer DAO lançar uma exceção, ela será propagada.
        new SectorsDAO().deleteByUnitId(id);
        new EmployeesDAO().deleteByIdUnit(id);

        String sql =
                "UPDATE units SET " +
                        "is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND is_active = true";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar unidade pelo ID: " + id, e
            );
        }
    }

    /*
     * Desativa as unidades ativas de uma empresa.
     */
    public boolean deleteByCompanyId(int companyId) {
        boolean allSuccess;
        UnitsFilter filter = new UnitsFilter();
        filter.setCompanyId(companyId);

        List<Units> units = searchAll(filter);
        if (units.isEmpty()){
            allSuccess = false;
        } else {
            allSuccess = true;
        }
        for (Units unit : units) {
            if (!delete(unit.getId())) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /*
     * Desativa unidades ativas pelo nome.
     */
    public boolean deleteByName(String name) {
        boolean allSuccess;

        UnitsFilter filter = new UnitsFilter();
        filter.setName(name);


        List<Units> units = searchAll(filter);
        if (units.isEmpty()){
            allSuccess = false;
        } else {
            allSuccess = true;
        }
        for (Units unit : units) {
            if (!delete(unit.getId())) {
                allSuccess = false;
            }
        } return allSuccess;
    }

    /*
     * Atualiza uma unidade pelo ID.
     */
    @Override
    public boolean update(Units unit, int id) {

        if (unit == null) {
            return false;
        }

        String sql =
                "UPDATE units SET " +
                        "company_id = ?, " +
                        "address_id = ?, " +
                        "name = ?, " +
                        "cnpj = ?, " +
                        "cnae = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar unidade pelo ID: " + id, e
            );
        }
    }

    /*
     * Atualiza unidades encontradas pelo nome.
     */
    public boolean updateByName(Units unit, String name) {

        if (unit == null || name == null || name.isBlank()) {
            return false;
        }

        String sql =
                "UPDATE units SET " +
                        "company_id = ?, " +
                        "address_id = ?, " +
                        "name = ?, " +
                        "cnpj = ?, " +
                        "cnae = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ?";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setString(6, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar unidades pelo nome: " + name, e
            );
        }
    }
}
