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

    /**
     * Valida os dados do funcionario antes de gravar (cpf e email)
     * Se algum dado estiver errado, avisa qual foi
     * @param employee O funcionario a validar
     * @return true se cpf e email forem validos
     */
    private boolean isValid(Employees employee) {

        if (employee == null) {
            System.out.println("Error: no employee was informed. Fill in the employee data before saving.");
            return false;
        }

        if (!Validators.isCpf(employee.getCpf())) {
            System.out.println("Error: the CPF is not valid. It must have 11 digits and correct check digits.");
            return false;
        }

        if (!Validators.isEmail(employee.getEmail())) {
            System.out.println("Error: the email is not valid. Use the format name@domain.com.");
            return false;
        }

        return true;
    }

    /**
     * Confere se o funcionario existe e se pertence a unidade informada (usado nos metodos PerUnit)
     * @param employee O funcionario encontrado na busca (pode ser null)
     * @param unitId O id da unidade do admin
     * @return true se o funcionario existe e e da unidade
     */
    private boolean belongsToUnit(Employees employee, int unitId) {

        if (employee == null) {
            System.out.println("Error: no active employee was found with the data informed.");
            return false;
        }

        if (employee.getUnitId() != unitId) {
            System.out.println("Error: this employee does not belong to your unit, so you cannot change it.");
            return false;
        }

        return true;
    }

    /**
     * Explica no console o erro que veio do banco, em linguagem clara
     * Usa o codigo SQLSTATE do PostgreSQL pra saber o que aconteceu
     * @param action O que estava sendo feito (ex: "register the employee")
     * @param e O erro que o banco devolveu
     */
    private void printSqlError(String action, SQLException e) {

        String state = e.getSQLState();

        if ("23505".equals(state)) {
            System.out.println("Error: could not " + action + " because some data that must be unique (like the CPF) already exists in the database.");
        } else if ("23503".equals(state)) {
            System.out.println("Error: could not " + action + " because the company, permission group, unit or sector informed does not exist.");
        } else if ("23502".equals(state)) {
            System.out.println("Error: could not " + action + " because a required field was left empty.");
        } else {
            System.out.println("Error: could not " + action + ". Check the database connection and try again.");
        }

        e.printStackTrace();
    }


    /**
     * Metodo que busca os funcionarios ativos aplicando os filtros informados
     * Os campos do filter que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Employees> com os empregados encontrados
     */
    @Override
    public List<Employees> searchAll(EmployeesFilter filter) {

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;
        Sectors sector = null;

        if (filter == null) {
            filter = new EmployeesFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, e.sector_id as sectorId, " +
                        "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                        "u.name as unitName, u.id as idUnit, sec.id as idSector, sec.name as sectorName " +
                        "from employees e " +
                        "join companies c on e.company_id = c.id " +
                        "join permission_groups pg on e.permission_group_id = pg.id " +
                        "join units u on u.id = e.unit_id " +
                        "join sectors sec on sec.id = e.sector_id " +
                        "where e.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and e.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and e.name ilike ?");
            parametros.add("%" + filter.getName() + "%");
        }

        if (filter.getCpf() != null && !filter.getCpf().isBlank()) {
            sql.append(" and e.cpf = ?");
            parametros.add(filter.getCpf());
        }

        if (filter.getStatus() != null) {
            sql.append(" and e.status = ?::status_employee");
            parametros.add(filter.getStatus().getValor());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and e.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getCompanyName() != null && !filter.getCompanyName().isBlank()) {
            sql.append(" and c.name ilike ?");
            parametros.add("%" + filter.getCompanyName() + "%");
        }

        if (filter.getUnitId() != null) {
            sql.append(" and u.id = ?");
            parametros.add(filter.getUnitId());
        }

        if (filter.getUnitName() != null && !filter.getUnitName().isBlank()) {
            sql.append(" and u.name ilike ?");
            parametros.add("%" + filter.getUnitName() + "%");
        }

        if (filter.getSectorId() != null) {
            sql.append(" and sec.id = ?");
            parametros.add(filter.getSectorId());
        }

        if (filter.getSectorName() != null && !filter.getSectorName().isBlank()) {
            sql.append(" and sec.name ilike ?");
            parametros.add("%" + filter.getSectorName() + "%");
        }

        if (filter.getEmail() != null
                && !filter.getEmail().isBlank()) {

            sql.append(" and e.email = ?");
            parametros.add(filter.getEmail().trim());
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return employees;
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

                    company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

                    permissionGroup = new PermissionGroups(
                            rs.getInt("idPermissionGroup"),
                            rs.getString("permissionGroup")
                    );

                    unit = new Units(
                            rs.getInt("idUnit"),
                            rs.getString("unitName")
                    );

                    sector = new Sectors(
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

                    employees.add(employee);
                }
            }

        } catch (SQLException e) {
            printSqlError("search the employees", e);
        } finally {
            return employees;
        }
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do empregado
     * @return Retorna o empregado encontrado ou null caso nao exista
     */
    @Override
    public Employees searchById(int id) {

        Employees employee = null;
        PermissionGroups permissionGroup = null;
        Companies company = null;
        Units unit = null;
        Sectors sector = null;

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, e.sector_id as sectorId, " +
                                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                                "u.name as unitName, u.id as idUnit, sec.id as idSector, sec.name as sectorName " +
                                "from employees e " +
                                "join permission_groups pg on pg.id = e.permission_group_id " +
                                "join companies c on e.company_id = c.id " +
                                "join units u on u.id = e.unit_id " +
                                "join sectors sec on sec.id = e.sector_id " +
                                "where e.id = ? and e.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    permissionGroup = new PermissionGroups(
                            rs.getInt("idPermissionGroup"),
                            rs.getString("permissionGroup")
                    );

                    company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

                    unit = new Units(
                            rs.getInt("idUnit"),
                            rs.getString("unitName")
                    );

                    sector = new Sectors(
                            rs.getInt("idSector"),
                            rs.getString("sectorName")
                    );

                    employee = new Employees(
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
                }
            }

        } catch (SQLException e) {
            printSqlError("search the employee by id", e);
        } finally {
            return employee;
        }
    }

    /**
     * metodo para buscar o empregado pelo cpf (somente ativos)
     * O cpf é unico no banco, entao devolve só um registro
     * @param cpf O cpf do empregado, somente com os digitos
     * @return Retorna o empregado encontrado ou null caso nao exista
     */
    public Employees searchByCpf(String cpf) {

        Employees employee = null;
        PermissionGroups permissionGroup = null;
        Companies company = null;
        Units unit = null;
        Sectors sector = null;

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, e.sector_id as sectorId, " +
                                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                                "u.name as unitName, u.id as idUnit, sec.id as idSector, sec.name as sectorName " +
                                "from employees e " +
                                "join permission_groups pg on pg.id = e.permission_group_id " +
                                "join companies c on e.company_id = c.id " +
                                "join units u on u.id = e.unit_id " +
                                "join sectors sec on sec.id = e.sector_id " +
                                "where e.cpf = ? and e.is_active = true"
                )
        ) {

            pstmt.setString(1, cpf);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    permissionGroup = new PermissionGroups(
                            rs.getInt("idPermissionGroup"),
                            rs.getString("permissionGroup")
                    );

                    company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

                    unit = new Units(
                            rs.getInt("idUnit"),
                            rs.getString("unitName")
                    );

                    sector = new Sectors(
                            rs.getInt("idSector"),
                            rs.getString("sectorName")
                    );

                    employee = new Employees(
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
                }
            }

        } catch (SQLException e) {
            printSqlError("search the employee by CPF", e);
        } finally {
            return employee;
        }
    }

    /**
     * metodo para cadastrar novos empregados no banco (sempre ativo na criacao)
     * A senha ja deve vir como hash no passwordHash (gerado pelo PasswordHasher)
     * @param employee O funcionario que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(Employees employee) {

        if (!isValid(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {

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

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("register the employee", e);
            return false;
        }
    }

    /**
     * metodo para registrar mais de um empregado
     * @param employees Uma lista de empregados
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Employees> employees) {

        if (employees == null || employees.isEmpty()) {
            System.out.println("Error: the list of employees is empty. Add at least one employee to register.");
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {

            for (int i = 0; i < employees.size(); i++) {
                Employees employee = employees.get(i);

                if (!isValid(employee)) {
                    System.out.println("Error: the employee at position " + (i + 1) + " of the list has invalid data, so no employee from the list was registered.");
                    return false;
                }

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

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

        } catch (SQLException e) {
            printSqlError("register the list of employees", e);
            return false;
        }
    }

    /**
     * metodo para desativar o empregado por id (is_active = false, status = 'INACTIVE')
     * @param id Valor unico de cada empregado
     * @return true se foi desativado e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set is_active = false, status = 'INACTIVE', updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the id " + id + ", so nothing was deactivated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employee", e);
            return false;
        }
    }

    /**
     * metodo que desativa por id da empresa (para quando uma empresa for desativada)
     * Se a empresa nao tiver funcionarios devolve false, e isso nao e um erro
     * @param idCompany
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByIdCompany(int idCompany) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set is_active = false, status = 'INACTIVE', updated_at = current_timestamp where company_id = ?"
                )
        ) {

            pstmt.setInt(1, idCompany);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employees of the company", e);
            return false;
        }
    }

    /**
     * metodo que desativa os empregados quando uma unidade for desativada
     * Se a unidade nao tiver funcionarios devolve false, e isso nao e um erro
     * @param unitId numero unico da unidade
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByIdUnit(int unitId) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where unit_id = ?"
                )
        ) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employees of the unit", e);
            return false;
        }
    }

    /**
     * metodo que desativa os empregados de uma unidade pelo nome
     * @param name O nome da unidade
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByNameUnit(String name) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees e set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                                "from units u " +
                                "where e.unit_id = u.id and u.name ilike ?"
                )
        ) {

            pstmt.setString(1, name);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found in a unit with the name informed, so nothing was deactivated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employees of the unit", e);
            return false;
        }
    }

    /**
     * metodo que desativa pelo nome
     * @param name O nome do funcionario
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByName(String name) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where name ilike ?"
                )
        ) {

            pstmt.setString(1, name);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the name informed, so nothing was deactivated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employee", e);
            return false;
        }
    }

    /**
     * metodo que desativa pelo cpf
     * @param cpf O cpf do funcionario, somente com os digitos
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByCpf(String cpf) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where cpf = ?"
                )
        ) {

            pstmt.setString(1, cpf);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the CPF informed, so nothing was deactivated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employee", e);
            return false;
        }
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id
     * Nao altera a senha (use updatePasswordHash) nem o status (use os metodos de status)
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(Employees employee, int id) {

        if (!isValid(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setInt(9, id);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the id " + id + ", so nothing was updated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("update the employee", e);
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo nome
     * @param employee Os valores do empregado para ser atualizado
     * @param name O nome para achar o funcionario
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByName(Employees employee, String name) {

        if (!isValid(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, name);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the name informed, so nothing was updated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("update the employee", e);
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario, somente com os digitos
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByCpf(Employees employee, String cpf) {

        if (!isValid(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where cpf = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, cpf);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the CPF informed, so nothing was updated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("update the employee", e);
            return false;
        }
    }

    /**
     * Metodo que atualiza a senha (hash) do empregado pelo id
     * O passwordHash ja deve vir pronto (gerado pelo PasswordHasher)
     * @param id O id do empregado
     * @param passwordHash O hash da nova senha
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updatePasswordHash(int id, String passwordHash) {

        if (passwordHash == null || passwordHash.isBlank()) {
            System.out.println("Error: the new password is empty. Generate the hash with the PasswordHasher before saving.");
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set password_hash = ?, updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setString(1, passwordHash);
            pstmt.setInt(2, id);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the id " + id + ", so the password was not changed.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("update the password of the employee", e);
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnVacation(int id) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'IN_VACATION', updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the id " + id + ", so the status was not changed.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("change the status of the employee", e);
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'ACTIVE'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActive(int id) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'ACTIVE', updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the id " + id + ", so the status was not changed.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("change the status of the employee", e);
            return false;
        }
    }


//    ------------- Metodos específicos para admins das units --------------

    /**
     * metodo para cadastrar novos empregados no banco
     * @param employee O funcionario que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerPerUnit(Employees employee, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        if (unitId != employee.getUnitId()) {
            System.out.println("Error: the unit of the employee is different from your unit, so you cannot register them here.");
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {

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

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("register the employee", e);
            return false;
        }
    }

    /**
     * metodo para registrar mais de um empregado numa unidade especifica
     * @param employees Uma lista de empregados
     * @param unitId O id da unidade para validacao
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLotPerUnit(List<Employees> employees, int unitId) {

        if (employees == null || employees.isEmpty()) {
            System.out.println("Error: the list of employees is empty. Add at least one employee to register.");
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {

            for (int i = 0; i < employees.size(); i++) {
                Employees employee = employees.get(i);

                if (!isValid(employee)) {
                    System.out.println("Error: the employee at position " + (i + 1) + " of the list has invalid data, so no employee from the list was registered.");
                    return false;
                }

                if (unitId != employee.getUnitId()) {
                    System.out.println("Error: the employee at position " + (i + 1) + " of the list is from another unit, so no employee from the list was registered.");
                    return false;
                }

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

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

        } catch (SQLException e) {
            printSqlError("register the list of employees", e);
            return false;
        }
    }

    /**
     * metodo para desativar o empregado por id que esta na unidade
     * @param id Valor unico de cada empregado
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByIdPerUnit(int id, int unitId) {

        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employee", e);
            return false;
        }
    }

    /**
     * metodo para desativar o empregado pelo cpf que esta na unidade
     * @param cpf O cpf do empregado, somente com os digitos
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByCpfPerUnit(String cpf, int unitId) {

        Employees employee = searchByCpf(cpf);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where cpf = ?"
                )
        ) {

            pstmt.setString(1, cpf);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("deactivate the employee", e);
            return false;
        }
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id numa unidade especifica
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false caso tenha dado erro
     */
    public boolean updateByIdPerUnit(Employees employee, int id, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        Employees employeeFound = searchById(id);

        if (!belongsToUnit(employeeFound, unitId)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setInt(9, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("update the employee", e);
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo nome numa unidade especifica
     * @param employee Os valores do empregado para ser atualizado
     * @param name O nome para achar o funcionario
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByNamePerUnit(Employees employee, String name, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ? and unit_id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, name);
            pstmt.setInt(10, unitId);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the name informed in your unit, so nothing was updated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("update the employee", e);
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf numa unidade especifica
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario, somente com os digitos
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByCpfPerUnit(Employees employee, String cpf, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where cpf = ? and unit_id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, cpf);
            pstmt.setInt(10, unitId);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Error: no employee was found with the CPF informed in your unit, so nothing was updated.");
            }

            return rows > 0;

        } catch (SQLException e) {
            printSqlError("update the employee", e);
            return false;
        }
    }

    /**
     * Metodo que atualiza a senha (hash) do empregado pelo id numa unidade especifica
     * @param id O id do empregado
     * @param passwordHash O hash da nova senha
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updatePasswordHashPerUnit(int id, String passwordHash, int unitId) {

        if (passwordHash == null || passwordHash.isBlank()) {
            System.out.println("Error: the new password is empty. Generate the hash with the PasswordHasher before saving.");
            return false;
        }

        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set password_hash = ?, updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setString(1, passwordHash);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("update the password of the employee", e);
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnVacationPerUnit(int id, int unitId) {

        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'IN_VACATION', updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("change the status of the employee", e);
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'ACTIVE' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActivePerUnit(int id, int unitId) {

        Employees employee = searchById(id);

        if (!belongsToUnit(employee, unitId)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'ACTIVE', updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            printSqlError("change the status of the employee", e);
            return false;
        }
    }

}