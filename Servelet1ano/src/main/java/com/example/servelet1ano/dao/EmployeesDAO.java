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
     * @param employee O funcionario a validar
     * @return true se cpf e email forem validos
     */
    private boolean isValid(Employees employee) {

        if (employee == null) {
            return false;
        }

        if (!Validators.isCpf(employee.getCpf())) {
            return false;
        }

        if (!Validators.isEmail(employee.getEmail())) {
            return false;
        }

        return true;
    }

    /**
     * Deixa so os digitos do cpf (tira pontos, traco e espacos)
     * Usado antes de gravar e antes de buscar, pra o cpf ficar sempre no mesmo formato no banco
     * @param cpf O cpf como foi digitado
     * @return O cpf so com os digitos, ou null se vier null
     */
    private String cleanCpf(String cpf) {

        if (cpf == null) {
            return null;
        }

        return cpf.replaceAll("\\D", "");
    }

    /**
     * Confere se o funcionario existe e se pertence a unidade informada (usado nos metodos PerUnit)
     * @param employee O funcionario encontrado na busca (pode ser null)
     * @param unitId O id da unidade do admin
     * @return true se o funcionario existe e eh da unidade
     */
    private boolean belongsToUnit(Employees employee, int unitId) {

        if (employee == null) {
            return false;
        }

        if (employee.getUnitId() != unitId) {
            return false;
        }

        return true;
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
            parametros.add(cleanCpf(filter.getCpf()));
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
        return employees;
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
        return employee;
    }

    /**
     * metodo para buscar o empregado pelo cpf (somente ativos)
     * O cpf é unico no banco, entao devolve só um registro
     * @param cpf O cpf do empregado (pode vir com ou sem pontuacao)
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

            pstmt.setString(1, cleanCpf(cpf));

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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
        return employee;
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
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, employee.getPasswordHash());
            pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * metodo para registrar mais de um empregado
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * @param employees Uma lista de empregados
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Employees> employees) {

        if (employees == null || employees.isEmpty()) {
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

            try {
                // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
                conn.setAutoCommit(false);

                for (int i = 0; i < employees.size(); i++) {
                    Employees employee = employees.get(i);

                    if (!isValid(employee)) {
                        return false;
                    }

                    pstmt.setInt(1, employee.getCompanyId());
                    pstmt.setInt(2, employee.getPermissionGroupId());
                    pstmt.setInt(3, employee.getUnitId());
                    pstmt.setInt(4, employee.getSectorId());
                    pstmt.setString(5, cleanCpf(employee.getCpf()));
                    pstmt.setString(6, employee.getName());
                    pstmt.setString(7, employee.getEmail());
                    pstmt.setString(8, employee.getPhone());
                    pstmt.setString(9, employee.getPasswordHash());
                    pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Erro ao registrar.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
                                "where e.unit_id = u.id and lower(u.name) = lower(?)"
                )
        ) {

            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa pelo cpf
     * @param cpf O cpf do funcionario (pode vir com ou sem pontuacao)
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

            pstmt.setString(1, cleanCpf(cpf));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setInt(9, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario (pode vir com ou sem pontuacao)
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
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, cleanCpf(cpf));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
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

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, employee.getPasswordHash());
            pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * metodo para registrar mais de um empregado numa unidade especifica
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * @param employees Uma lista de empregados
     * @param unitId O id da unidade para validacao
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLotPerUnit(List<Employees> employees, int unitId) {

        if (employees == null || employees.isEmpty()) {
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

            try {
                // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
                conn.setAutoCommit(false);

                for (int i = 0; i < employees.size(); i++) {
                    Employees employee = employees.get(i);

                    if (!isValid(employee)) {
                        return false;
                    }

                    if (unitId != employee.getUnitId()) {
                        return false;
                    }

                    pstmt.setInt(1, employee.getCompanyId());
                    pstmt.setInt(2, employee.getPermissionGroupId());
                    pstmt.setInt(3, employee.getUnitId());
                    pstmt.setInt(4, employee.getSectorId());
                    pstmt.setString(5, cleanCpf(employee.getCpf()));
                    pstmt.setString(6, employee.getName());
                    pstmt.setString(7, employee.getEmail());
                    pstmt.setString(8, employee.getPhone());
                    pstmt.setString(9, employee.getPasswordHash());
                    pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Erro ao registrar.", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * metodo para desativar o empregado pelo cpf que esta na unidade
     * @param cpf O cpf do empregado (pode vir com ou sem pontuacao)
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

            pstmt.setString(1, cleanCpf(cpf));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false caso tenha dado erro
     */
    public boolean updateByIdPerUnit(Employees employee, int id, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        if (unitId != employee.getUnitId()) {
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
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setInt(9, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo nome numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade
     * @param employee Os valores do empregado para ser atualizado
     * @param name O nome para achar o funcionario
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByNamePerUnit(Employees employee, String name, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        if (unitId != employee.getUnitId()) {
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
                                "where lower(name) = lower(?) and unit_id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, name);
            pstmt.setInt(10, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario (pode vir com ou sem pontuacao)
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByCpfPerUnit(Employees employee, String cpf, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        if (unitId != employee.getUnitId()) {
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
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, cleanCpf(cpf));
            pstmt.setInt(10, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

}