
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.StatusEmployee;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeesDAO implements DAOI<Employees, EmployeesFilter> {

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }

    private Connection getConnection() {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    private boolean isValid(Employees employee) {
        return employee != null
                && Validators.isCpf(employee.getCpf())
                && Validators.isEmail(employee.getEmail());
    }

    private boolean belongsToUnit(Employees employee, int unitId) {
        return employee != null && employee.getUnitId() == unitId;
    }

    private boolean isValidForUnit(Employees employee, int unitId) {
        return isValid(employee) && employee.getUnitId() == unitId;
    }

    private boolean executeUpdate(
            String sql,
            StatementBinder binder,
            String errorMessage
    ) {
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            binder.bind(pstmt);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    private Employees mapEmployee(ResultSet rs) throws SQLException {
        Companies company = new Companies(
                rs.getInt("idCompany"),
                rs.getString("companyName")
        );

        PermissionGroups permissionGroup = new PermissionGroups(
                rs.getInt("idPermissionGroup"),
                rs.getString("permissionGroup")
        );

        Units unit = new Units(
                rs.getInt("idUnit"),
                rs.getString("unitName")
        );

        Sectors sector = new Sectors(
                rs.getInt("idSector"),
                rs.getString("sectorName")
        );

        Employees employee = new Employees(
                rs.getInt("id"),
                rs.getInt("companyId"),
                rs.getInt("permissionGroupId"),
                rs.getInt("unitId"),
                rs.getInt("sectorId"),
                rs.getString("cpf"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                StatusEmployee.fromValor(rs.getString("status")),
                permissionGroup,
                company,
                unit,
                sector
        );

        employee.setPasswordHash(rs.getString("password_hash"));
        employee.setStorageFileId(rs.getInt("storage_file_id"));
        employee.setCreatedAt(rs.getTimestamp("created_at"));
        employee.setUpdatedAt(rs.getTimestamp("updated_at"));

        return employee;
    }

    private String employeeSelectSql() {
        return "SELECT e.*, " +
                "e.company_id AS companyId, " +
                "e.permission_group_id AS permissionGroupId, " +
                "e.unit_id AS unitId, " +
                "e.sector_id AS sectorId, " +
                "c.name AS companyName, " +
                "c.id AS idCompany, " +
                "pg.id AS idPermissionGroup, " +
                "pg.name AS permissionGroup, " +
                "u.name AS unitName, " +
                "u.id AS idUnit, " +
                "sec.id AS idSector, " +
                "sec.name AS sectorName " +
                "FROM employees e " +
                "JOIN companies c ON e.company_id = c.id " +
                "JOIN permission_groups pg ON e.permission_group_id = pg.id " +
                "JOIN units u ON u.id = e.unit_id " +
                "JOIN sectors sec ON sec.id = e.sector_id ";
    }

    private String insertEmployeeSql() {
        return "INSERT INTO employees " +
                "(company_id, permission_group_id, unit_id, sector_id, " +
                "cpf, name, email, phone, password_hash, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)";
    }

    private void bindEmployee(
            PreparedStatement pstmt,
            Employees employee
    ) throws SQLException {
        pstmt.setInt(1, employee.getCompanyId());
        pstmt.setInt(2, employee.getPermissionGroupId());
        pstmt.setInt(3, employee.getUnitId());
        pstmt.setInt(4, employee.getSectorId());
        pstmt.setString(5, employee.getCpf());
        pstmt.setString(6, employee.getName());
        pstmt.setString(7, employee.getEmail());
        pstmt.setString(8, employee.getPhone());
        pstmt.setString(9, employee.getPasswordHash());
        pstmt.setString(10, StatusEmployee.ACTIVE.getValor());
    }

    @Override
    public List<Employees> searchAll(EmployeesFilter filter) {
        List<Employees> employees = new ArrayList<>();

        if (filter == null) {
            filter = new EmployeesFilter();
        }

        StringBuilder sql = new StringBuilder(
                employeeSelectSql() + "WHERE e.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" AND e.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" AND e.name ILIKE ?");
            parametros.add("%" + filter.getName() + "%");
        }

        if (filter.getCpf() != null && !filter.getCpf().isBlank()) {
            sql.append(" AND e.cpf = ?");
            parametros.add(filter.getCpf());
        }

        if (filter.getStatus() != null) {
            sql.append(" AND e.status = ?::status_employee");
            parametros.add(filter.getStatus().getValor());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" AND e.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getCompanyName() != null
                && !filter.getCompanyName().isBlank()) {
            sql.append(" AND c.name ILIKE ?");
            parametros.add("%" + filter.getCompanyName() + "%");
        }

        if (filter.getUnitId() != null) {
            sql.append(" AND u.id = ?");
            parametros.add(filter.getUnitId());
        }

        if (filter.getUnitName() != null
                && !filter.getUnitName().isBlank()) {
            sql.append(" AND u.name ILIKE ?");
            parametros.add("%" + filter.getUnitName() + "%");
        }

        if (filter.getSectorId() != null) {
            sql.append(" AND sec.id = ?");
            parametros.add(filter.getSectorId());
        }

        if (filter.getSectorName() != null
                && !filter.getSectorName().isBlank()) {
            sql.append(" AND sec.name ILIKE ?");
            parametros.add("%" + filter.getSectorName() + "%");
        }

        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            sql.append(" AND e.email = ?");
            parametros.add(filter.getEmail().trim());
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
                    employees.add(mapEmployee(rs));
                }
            }

            return employees;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar funcionários.",
                    e
            );
        }
    }

    @Override
    public Employees searchById(int id) {
        String sql = employeeSelectSql()
                + "WHERE e.id = ? AND e.is_active = true";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar funcionário pelo ID.",
                    e
            );
        }
    }

    public Employees searchByCpf(String cpf) {
        String sql = employeeSelectSql()
                + "WHERE e.cpf = ? AND e.is_active = true";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, cpf);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar funcionário pelo CPF.",
                    e
            );
        }
    }

    @Override
    public boolean register(Employees employee) {
        if (!isValid(employee)) {
            return false;
        }

        return executeUpdate(
                insertEmployeeSql(),
                pstmt -> bindEmployee(pstmt, employee),
                "Erro ao cadastrar funcionário."
        );
    }

    public boolean registerLot(List<Employees> employees) {
        if (employees == null || employees.isEmpty()) {
            return false;
        }

        for (Employees employee : employees) {
            if (!isValid(employee)) {
                return false;
            }
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt =
                         conn.prepareStatement(insertEmployeeSql())) {

                for (Employees employee : employees) {
                    bindEmployee(pstmt, employee);
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
                        "Erro ao cadastrar a lista de funcionários.",
                        e
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro na transação de cadastro dos funcionários.",
                    e
            );
        }
    }

    @Override
    public boolean delete(int id) {
        return executeUpdate(
                "UPDATE employees SET is_active = false, " +
                        "status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?",
                pstmt -> pstmt.setInt(1, id),
                "Erro ao desativar funcionário pelo ID."
        );
    }

    public boolean deleteByIdCompany(int idCompany) {
        return executeUpdate(
                "UPDATE employees SET is_active = false, " +
                        "status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP " +
                        "WHERE company_id = ?",
                pstmt -> pstmt.setInt(1, idCompany),
                "Erro ao desativar funcionários da empresa."
        );
    }

    public boolean deleteByIdUnit(int unitId) {
        return executeUpdate(
                "UPDATE employees SET status = 'INACTIVE', " +
                        "is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE unit_id = ?",
                pstmt -> pstmt.setInt(1, unitId),
                "Erro ao desativar funcionários da unidade."
        );
    }

    public boolean deleteByNameUnit(String name) {
        return executeUpdate(
                "UPDATE employees e SET status = 'INACTIVE', " +
                        "is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "FROM units u WHERE e.unit_id = u.id AND u.name ILIKE ?",
                pstmt -> pstmt.setString(1, name),
                "Erro ao desativar funcionários pelo nome da unidade."
        );
    }

    public boolean deleteByName(String name) {
        return executeUpdate(
                "UPDATE employees SET status = 'INACTIVE', " +
                        "is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ?",
                pstmt -> pstmt.setString(1, name),
                "Erro ao desativar funcionário pelo nome."
        );
    }

    public boolean deleteByCpf(String cpf) {
        return executeUpdate(
                "UPDATE employees SET status = 'INACTIVE', " +
                        "is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE cpf = ?",
                pstmt -> pstmt.setString(1, cpf),
                "Erro ao desativar funcionário pelo CPF."
        );
    }

    @Override
    public boolean update(Employees employee, int id) {
        if (!isValid(employee)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET company_id = ?, " +
                        "permission_group_id = ?, unit_id = ?, sector_id = ?, " +
                        "cpf = ?, name = ?, email = ?, phone = ?, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                pstmt -> {
                    bindUpdateEmployee(pstmt, employee);
                    pstmt.setInt(9, id);
                },
                "Erro ao atualizar funcionário pelo ID."
        );
    }

    public boolean updateByName(Employees employee, String name) {
        if (!isValid(employee)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET company_id = ?, " +
                        "permission_group_id = ?, unit_id = ?, sector_id = ?, " +
                        "cpf = ?, name = ?, email = ?, phone = ?, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE name ILIKE ?",
                pstmt -> {
                    bindUpdateEmployee(pstmt, employee);
                    pstmt.setString(9, name);
                },
                "Erro ao atualizar funcionário pelo nome."
        );
    }

    public boolean updateByCpf(Employees employee, String cpf) {
        if (!isValid(employee)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET company_id = ?, " +
                        "permission_group_id = ?, unit_id = ?, sector_id = ?, " +
                        "cpf = ?, name = ?, email = ?, phone = ?, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE cpf = ?",
                pstmt -> {
                    bindUpdateEmployee(pstmt, employee);
                    pstmt.setString(9, cpf);
                },
                "Erro ao atualizar funcionário pelo CPF."
        );
    }

    private void bindUpdateEmployee(
            PreparedStatement pstmt,
            Employees employee
    ) throws SQLException {
        pstmt.setInt(1, employee.getCompanyId());
        pstmt.setInt(2, employee.getPermissionGroupId());
        pstmt.setInt(3, employee.getUnitId());
        pstmt.setInt(4, employee.getSectorId());
        pstmt.setString(5, employee.getCpf());
        pstmt.setString(6, employee.getName());
        pstmt.setString(7, employee.getEmail());
        pstmt.setString(8, employee.getPhone());
    }

    public boolean updatePasswordHash(int id, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET password_hash = ?, " +
                        "updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                pstmt -> {
                    pstmt.setString(1, passwordHash);
                    pstmt.setInt(2, id);
                },
                "Erro ao atualizar a senha do funcionário."
        );
    }

    public boolean updateStatusOnVacation(int id) {
        return executeUpdate(
                "UPDATE employees SET status = 'IN_VACATION', " +
                        "updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                pstmt -> pstmt.setInt(1, id),
                "Erro ao alterar o status do funcionário para férias."
        );
    }

    public boolean updateStatusActive(int id) {
        return executeUpdate(
                "UPDATE employees SET status = 'ACTIVE', " +
                        "updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                pstmt -> pstmt.setInt(1, id),
                "Erro ao ativar o status do funcionário."
        );
    }

    // Métodos específicos para administradores de unidades.

    public boolean registerPerUnit(Employees employee, int unitId) {
        if (!isValidForUnit(employee, unitId)) {
            return false;
        }

        return register(employee);
    }

    public boolean registerLotPerUnit(
            List<Employees> employees,
            int unitId
    ) {
        if (employees == null || employees.isEmpty()) {
            return false;
        }

        for (Employees employee : employees) {
            if (!isValidForUnit(employee, unitId)) {
                return false;
            }
        }

        return registerLot(employees);
    }

    public boolean deleteByIdPerUnit(int id, int unitId) {
        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET status = 'INACTIVE', " +
                        "is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND unit_id = ?",
                pstmt -> {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, unitId);
                },
                "Erro ao desativar funcionário da unidade."
        );
    }

    public boolean deleteByCpfPerUnit(String cpf, int unitId) {
        Employees employee = searchByCpf(cpf);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET status = 'INACTIVE', " +
                        "is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE cpf = ? AND unit_id = ?",
                pstmt -> {
                    pstmt.setString(1, cpf);
                    pstmt.setInt(2, unitId);
                },
                "Erro ao desativar funcionário pelo CPF na unidade."
        );
    }

    public boolean updateByIdPerUnit(
            Employees employee,
            int id,
            int unitId
    ) {
        if (!isValidForUnit(employee, unitId)) {
            return false;
        }

        Employees found = searchById(id);

        if (!belongsToUnit(found, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET company_id = ?, " +
                        "permission_group_id = ?, unit_id = ?, sector_id = ?, " +
                        "cpf = ?, name = ?, email = ?, phone = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND unit_id = ?",
                pstmt -> {
                    bindUpdateEmployee(pstmt, employee);
                    pstmt.setInt(9, id);
                    pstmt.setInt(10, unitId);
                },
                "Erro ao atualizar funcionário da unidade pelo ID."
        );
    }

    public boolean updateByNamePerUnit(
            Employees employee,
            String name,
            int unitId
    ) {
        if (!isValidForUnit(employee, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET company_id = ?, " +
                        "permission_group_id = ?, unit_id = ?, sector_id = ?, " +
                        "cpf = ?, name = ?, email = ?, phone = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ? AND unit_id = ?",
                pstmt -> {
                    bindUpdateEmployee(pstmt, employee);
                    pstmt.setString(9, name);
                    pstmt.setInt(10, unitId);
                },
                "Erro ao atualizar funcionário pelo nome na unidade."
        );
    }

    public boolean updateByCpfPerUnit(
            Employees employee,
            String cpf,
            int unitId
    ) {
        if (!isValidForUnit(employee, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET company_id = ?, " +
                        "permission_group_id = ?, unit_id = ?, sector_id = ?, " +
                        "cpf = ?, name = ?, email = ?, phone = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE cpf = ? AND unit_id = ?",
                pstmt -> {
                    bindUpdateEmployee(pstmt, employee);
                    pstmt.setString(9, cpf);
                    pstmt.setInt(10, unitId);
                },
                "Erro ao atualizar funcionário pelo CPF na unidade."
        );
    }

    public boolean updatePasswordHashPerUnit(
            int id,
            String passwordHash,
            int unitId
    ) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return false;
        }

        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET password_hash = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND unit_id = ?",
                pstmt -> {
                    pstmt.setString(1, passwordHash);
                    pstmt.setInt(2, id);
                    pstmt.setInt(3, unitId);
                },
                "Erro ao atualizar a senha do funcionário da unidade."
        );
    }

    public boolean updateStatusOnVacationPerUnit(int id, int unitId) {
        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET status = 'IN_VACATION', " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND unit_id = ?",
                pstmt -> {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, unitId);
                },
                "Erro ao alterar o status do funcionário da unidade."
        );
    }

    public boolean updateStatusActivePerUnit(int id, int unitId) {
        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        return executeUpdate(
                "UPDATE employees SET status = 'ACTIVE', " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND unit_id = ?",
                pstmt -> {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, unitId);
                },
                "Erro ao ativar o funcionário da unidade."
        );
    }
}
