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
     * Valida os dados do funcionario antes de gravar (cpf, email, nome e, se preenchido, celular)
     * @param employee O funcionario a validar
     * @return true se cpf, email, nome e o celular (quando informado) forem validos
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

        if (employee.getName() == null || employee.getName().isBlank()) {
            return false;
        }

        if (employee.getPhone() != null && !employee.getPhone().isBlank() && !Validators.isMobilePhone(employee.getPhone())) {
            return false;
        }

        return true;
    }

    /**
     * Confere se o funcionario tem a empresa, o grupo de permissao, a unidade e o setor informados
     * Sem isso o id ficaria 0 e o banco recusaria por causa da chave estrangeira
     * @param employee O funcionario a conferir
     * @return true se os 4 ids forem maiores que zero
     */
    private boolean hasParents(Employees employee) {

        if (employee == null) {
            return false;
        }

        if (employee.getCompanyId() <= 0 || employee.getPermissionGroupId() <= 0
                || employee.getUnitId() <= 0 || employee.getSectorId() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Confere se o funcionario veio com o hash da senha (o hash é obrigatorio no banco)
     * Usado so nos cadastros, porque nas atualizacoes a senha nao muda
     * @param employee O funcionario a conferir
     * @return true se o hash estiver preenchido
     */
    private boolean hasPasswordHash(Employees employee) {

        if (employee == null) {
            return false;
        }

        if (employee.getPasswordHash() == null || employee.getPasswordHash().isBlank()) {
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
     * Deixa so os digitos do celular (tira parenteses, traco e espacos)
     * Usado antes de gravar, pra o celular ficar sempre no mesmo formato no banco
     * @param phone O celular como foi digitado
     * @return O celular so com os digitos, ou null se vier null ou vazio
     */
    private String cleanPhone(String phone) {

        if (phone == null) {
            return null;
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.isEmpty()) {
            return null;
        }

        return digits;
    }

    /**
     * Tira os espacos das pontas do email
     * Usado antes de gravar, pra o email ficar sempre no mesmo formato no banco
     * @param email O email como foi digitado
     * @return O email sem espacos nas pontas, ou null se vier null
     */
    private String cleanEmail(String email) {

        if (email == null) {
            return null;
        }

        return email.trim();
    }


    /**
     * Metodo que busca os funcionarios ativos aplicando os filtros informados
     * Os campos do filter que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * Nao traz o hash da senha (pra isso use o searchForLogin)
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

            sql.append(" and lower(e.email) = lower(?)");
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

                    employee.setStorageFileId(rs.getInt("storage_file_id"));
                    employee.setCreatedAt(rs.getTimestamp("created_at"));
                    employee.setUpdatedAt(rs.getTimestamp("updated_at"));

                    employees.add(employee);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * Nao traz o hash da senha (pra isso use o searchForLogin)
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

                    employee.setStorageFileId(rs.getInt("storage_file_id"));
                    employee.setCreatedAt(rs.getTimestamp("created_at"));
                    employee.setUpdatedAt(rs.getTimestamp("updated_at"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employee;
    }

    /**
     * metodo para buscar o empregado pelo cpf (somente ativos)
     * O cpf é unico no banco, entao devolve só um registro
     * Nao traz o hash da senha (pra isso use o searchForLogin)
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

                    employee.setStorageFileId(rs.getInt("storage_file_id"));
                    employee.setCreatedAt(rs.getTimestamp("created_at"));
                    employee.setUpdatedAt(rs.getTimestamp("updated_at"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employee;
    }

    /**
     * metodo para buscar o empregado pelo email e pela empresa na hora do login (somente ativos)
     * O email é unico dentro da empresa (o cadastro e a atualizacao nao deixam repetir), entao devolve só um registro
     * É o unico metodo de busca que traz o hash da senha, os outros nao trazem
     * @param email O email do empregado
     * @param companyId O id da empresa do empregado
     * @return Retorna o empregado encontrado (com o hash da senha) ou null caso nao exista
     */
    public Employees searchForLogin(String email, int companyId) {

        Employees employee = null;
        PermissionGroups permissionGroup = null;
        Companies company = null;
        Units unit = null;
        Sectors sector = null;

        if (email == null || email.isBlank()) {
            return null;
        }

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
                                "where lower(e.email) = lower(?) and e.company_id = ? and e.is_active = true"
                )
        ) {

            pstmt.setString(1, email.trim());
            pstmt.setInt(2, companyId);

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
            e.printStackTrace();
        }

        return employee;
    }

    /**
     * metodo para cadastrar novos empregados no banco (sempre ativo na criacao)
     * A senha ja deve vir como hash no passwordHash (gerado pelo PasswordHasher)
     * Nao cadastra se ja existir um funcionario ativo com o mesmo cpf ou com o mesmo email na empresa
     * Se o cpf for de um funcionario que foi desativado, o cadastro antigo é reativado com os dados novos
     * @param employee O funcionario que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(Employees employee) {

        if (!isValid(employee)) {
            return false;
        }

        if (!hasParents(employee)) {
            return false;
        }

        if (!hasPasswordHash(employee)) {
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
                                "select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee " +
                                "where not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = ? and o.is_active = true) " +
                                "on conflict (cpf) do update set company_id = excluded.company_id, " +
                                "permission_group_id = excluded.permission_group_id, " +
                                "unit_id = excluded.unit_id, " +
                                "sector_id = excluded.sector_id, " +
                                "name = excluded.name, " +
                                "email = excluded.email, " +
                                "phone = excluded.phone, " +
                                "password_hash = excluded.password_hash, " +
                                "status = excluded.status, " +
                                "is_active = true, " +
                                "updated_at = current_timestamp " +
                                "where employees.is_active = false"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, cleanEmail(employee.getEmail()));
            pstmt.setString(8, cleanPhone(employee.getPhone()));
            pstmt.setString(9, employee.getPasswordHash());
            pstmt.setString(10, StatusEmployee.ACTIVE.getValor());
            pstmt.setString(11, cleanEmail(employee.getEmail()));
            pstmt.setInt(12, employee.getCompanyId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de um empregado
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * Mesmas regras do register: se algum cpf ou email ja estiver em uso, nenhum é salvo
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
                                "select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee " +
                                "where not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = ? and o.is_active = true) " +
                                "on conflict (cpf) do update set company_id = excluded.company_id, " +
                                "permission_group_id = excluded.permission_group_id, " +
                                "unit_id = excluded.unit_id, " +
                                "sector_id = excluded.sector_id, " +
                                "name = excluded.name, " +
                                "email = excluded.email, " +
                                "phone = excluded.phone, " +
                                "password_hash = excluded.password_hash, " +
                                "status = excluded.status, " +
                                "is_active = true, " +
                                "updated_at = current_timestamp " +
                                "where employees.is_active = false"
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

                    if (!hasParents(employee)) {
                        return false;
                    }

                    if (!hasPasswordHash(employee)) {
                        return false;
                    }

                    pstmt.setInt(1, employee.getCompanyId());
                    pstmt.setInt(2, employee.getPermissionGroupId());
                    pstmt.setInt(3, employee.getUnitId());
                    pstmt.setInt(4, employee.getSectorId());
                    pstmt.setString(5, cleanCpf(employee.getCpf()));
                    pstmt.setString(6, employee.getName());
                    pstmt.setString(7, cleanEmail(employee.getEmail()));
                    pstmt.setString(8, cleanPhone(employee.getPhone()));
                    pstmt.setString(9, employee.getPasswordHash());
                    pstmt.setString(10, StatusEmployee.ACTIVE.getValor());
                    pstmt.setString(11, cleanEmail(employee.getEmail()));
                    pstmt.setInt(12, employee.getCompanyId());

                    pstmt.addBatch();
                }

                int[] results = pstmt.executeBatch();

                for (int i = 0; i < results.length; i++) {
                    // 0 linhas = cpf ou email ja em uso: desfaz tudo
                    if (results[i] == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                // o rollback fica aqui dentro porque o catch de fora so roda depois da conexao fechada
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa por id da empresa (para quando uma empresa for desativada)
     * Desativa tambem os funcionarios que estao nas unidades da empresa, mesmo os que ficaram sem o id da empresa
     * Se a empresa nao tiver funcionarios devolve false, e isso nao e um erro
     * @param idCompany
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByIdCompany(int idCompany) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (conn) {

            return deleteByIdCompany(conn, idCompany);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa os funcionarios de uma empresa dentro de uma transacao que ja esta aberta
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Se a empresa nao tiver funcionarios devolve false, e isso nao e um erro
     * @param conn A conexao da transacao
     * @param idCompany O id da empresa
     * @return true se algum funcionario foi desativado
     * @throws SQLException se o banco der erro, pra quem chamou desfazer a transacao (rollback)
     */
    public boolean deleteByIdCompany(Connection conn, int idCompany) throws SQLException {

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set is_active = false, status = 'INACTIVE', updated_at = current_timestamp " +
                                "where company_id = ? or unit_id in (select id from units where company_id = ?)"
                )
        ) {

            pstmt.setInt(1, idCompany);
            pstmt.setInt(2, idCompany);

            return pstmt.executeUpdate() > 0;
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

        try (conn) {

            return deleteByIdUnit(conn, unitId);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa os funcionarios de uma unidade dentro de uma transacao que ja esta aberta
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Se a unidade nao tiver funcionarios devolve false, e isso nao e um erro
     * @param conn A conexao da transacao
     * @param unitId numero unico da unidade
     * @return true se algum funcionario foi desativado
     * @throws SQLException se o banco der erro, pra quem chamou desfazer a transacao (rollback)
     */
    public boolean deleteByIdUnit(Connection conn, int unitId) throws SQLException {

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where unit_id = ?"
                )
        ) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa os empregados de uma unidade pelo nome
     * O nome da unidade só vale dentro da empresa informada (empresas diferentes podem ter unidades com o mesmo nome)
     * Se a empresa tiver mais de uma unidade ativa com esse nome nao desativa ninguem e devolve false
     * @param name O nome da unidade
     * @param companyId O id da empresa da unidade
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByNameUnit(String name, int companyId) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees e set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                                "from units u " +
                                "where e.unit_id = u.id and u.company_id = ? and lower(u.name) = lower(?) and e.is_active = true " +
                                "and (select count(*) from units x where x.company_id = ? and lower(x.name) = lower(?) and x.is_active = true) = 1"
                )
        ) {

            pstmt.setInt(1, companyId);
            pstmt.setString(2, name);
            pstmt.setInt(3, companyId);
            pstmt.setString(4, name);

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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id
     * Nao altera a senha (use updatePasswordHash) nem o status (use os metodos de status)
     * Nao deixa o email ficar igual ao de outro funcionario ativo da mesma empresa
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(Employees employee, int id) {

        if (!isValid(employee)) {
            return false;
        }

        if (!hasParents(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees e set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where e.id = ? " +
                                "and not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = ? and o.is_active = true and o.id <> e.id)"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, cleanEmail(employee.getEmail()));
            pstmt.setString(8, cleanPhone(employee.getPhone()));
            pstmt.setInt(9, id);
            pstmt.setString(10, cleanEmail(employee.getEmail()));
            pstmt.setInt(11, employee.getCompanyId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf
     * Nao deixa o email ficar igual ao de outro funcionario ativo da mesma empresa
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario (pode vir com ou sem pontuacao)
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByCpf(Employees employee, String cpf) {

        if (!isValid(employee)) {
            return false;
        }

        if (!hasParents(employee)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees e set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where e.cpf = ? " +
                                "and not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = ? and o.is_active = true and o.id <> e.id)"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, cleanCpf(employee.getCpf()));
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, cleanEmail(employee.getEmail()));
            pstmt.setString(8, cleanPhone(employee.getPhone()));
            pstmt.setString(9, cleanCpf(cpf));
            pstmt.setString(10, cleanEmail(employee.getEmail()));
            pstmt.setInt(11, employee.getCompanyId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION'
     * So muda o status de funcionario ativo
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
                        "update employees set status = 'IN_VACATION', updated_at = current_timestamp where id = ? and is_active = true"
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
     * Se o funcionario estava desativado, reativa ele (is_active = true), desde que a unidade e o setor dele estejam ativos
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
                        "update employees e set status = 'ACTIVE', is_active = true, updated_at = current_timestamp " +
                                "where e.id = ? " +
                                "and exists (select 1 from units u where u.id = e.unit_id and u.is_active = true) " +
                                "and exists (select 1 from sectors s where s.id = e.sector_id and s.is_active = true)"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


//    ------------- Metodos específicos para admins das units --------------

    /**
     * metodo para cadastrar novos empregados no banco
     * A empresa do funcionario precisa ser a da unidade, o setor precisa ser da unidade e o grupo de permissao
     * precisa ser global ou da empresa (tudo conferido no proprio comando)
     * Nao cadastra se ja existir um funcionario ativo com o mesmo cpf ou com o mesmo email na empresa
     * Se o cpf for de um funcionario que foi desativado, o cadastro antigo é reativado com os dados novos
     * @param employee O funcionario que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerPerUnit(Employees employee, int unitId) {

        if (!isValid(employee)) {
            return false;
        }

        if (!hasParents(employee)) {
            return false;
        }

        if (!hasPasswordHash(employee)) {
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
                                "select u.company_id, ?, u.id, ?, ?, ?, ?, ?, ?, ?::status_employee " +
                                "from units u " +
                                "where u.id = ? and u.company_id = ? and u.is_active = true " +
                                "and exists (select 1 from sectors s where s.id = ? and s.unit_id = u.id and s.is_active = true) " +
                                "and exists (select 1 from permission_groups pg where pg.id = ? and pg.is_active = true and (pg.company_id is null or pg.company_id = u.company_id)) " +
                                "and not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = u.company_id and o.is_active = true) " +
                                "on conflict (cpf) do update set company_id = excluded.company_id, " +
                                "permission_group_id = excluded.permission_group_id, " +
                                "unit_id = excluded.unit_id, " +
                                "sector_id = excluded.sector_id, " +
                                "name = excluded.name, " +
                                "email = excluded.email, " +
                                "phone = excluded.phone, " +
                                "password_hash = excluded.password_hash, " +
                                "status = excluded.status, " +
                                "is_active = true, " +
                                "updated_at = current_timestamp " +
                                "where employees.is_active = false"
                )
        ) {

            pstmt.setInt(1, employee.getPermissionGroupId());
            pstmt.setInt(2, employee.getSectorId());
            pstmt.setString(3, cleanCpf(employee.getCpf()));
            pstmt.setString(4, employee.getName());
            pstmt.setString(5, cleanEmail(employee.getEmail()));
            pstmt.setString(6, cleanPhone(employee.getPhone()));
            pstmt.setString(7, employee.getPasswordHash());
            pstmt.setString(8, StatusEmployee.ACTIVE.getValor());
            pstmt.setInt(9, unitId);
            pstmt.setInt(10, employee.getCompanyId());
            pstmt.setInt(11, employee.getSectorId());
            pstmt.setInt(12, employee.getPermissionGroupId());
            pstmt.setString(13, cleanEmail(employee.getEmail()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de um empregado numa unidade especifica
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * Mesmas regras do registerPerUnit: se algum nao puder entrar, nenhum é salvo
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
                                "select u.company_id, ?, u.id, ?, ?, ?, ?, ?, ?, ?::status_employee " +
                                "from units u " +
                                "where u.id = ? and u.company_id = ? and u.is_active = true " +
                                "and exists (select 1 from sectors s where s.id = ? and s.unit_id = u.id and s.is_active = true) " +
                                "and exists (select 1 from permission_groups pg where pg.id = ? and pg.is_active = true and (pg.company_id is null or pg.company_id = u.company_id)) " +
                                "and not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = u.company_id and o.is_active = true) " +
                                "on conflict (cpf) do update set company_id = excluded.company_id, " +
                                "permission_group_id = excluded.permission_group_id, " +
                                "unit_id = excluded.unit_id, " +
                                "sector_id = excluded.sector_id, " +
                                "name = excluded.name, " +
                                "email = excluded.email, " +
                                "phone = excluded.phone, " +
                                "password_hash = excluded.password_hash, " +
                                "status = excluded.status, " +
                                "is_active = true, " +
                                "updated_at = current_timestamp " +
                                "where employees.is_active = false"
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

                    if (!hasParents(employee)) {
                        return false;
                    }

                    if (!hasPasswordHash(employee)) {
                        return false;
                    }

                    if (unitId != employee.getUnitId()) {
                        return false;
                    }

                    pstmt.setInt(1, employee.getPermissionGroupId());
                    pstmt.setInt(2, employee.getSectorId());
                    pstmt.setString(3, cleanCpf(employee.getCpf()));
                    pstmt.setString(4, employee.getName());
                    pstmt.setString(5, cleanEmail(employee.getEmail()));
                    pstmt.setString(6, cleanPhone(employee.getPhone()));
                    pstmt.setString(7, employee.getPasswordHash());
                    pstmt.setString(8, StatusEmployee.ACTIVE.getValor());
                    pstmt.setInt(9, unitId);
                    pstmt.setInt(10, employee.getCompanyId());
                    pstmt.setInt(11, employee.getSectorId());
                    pstmt.setInt(12, employee.getPermissionGroupId());
                    pstmt.setString(13, cleanEmail(employee.getEmail()));

                    pstmt.addBatch();
                }

                int[] results = pstmt.executeBatch();

                for (int i = 0; i < results.length; i++) {
                    // 0 linhas = alguma regra nao passou (cpf ou email em uso, setor, grupo ou empresa errados): desfaz tudo
                    if (results[i] == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                // o rollback fica aqui dentro porque o catch de fora so roda depois da conexao fechada
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar o empregado por id que esta na unidade
     * A conferencia da unidade e a desativacao acontecem no mesmo comando, entao nao da pra mudar nada no meio
     * @param id Valor unico de cada empregado
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso tenha dado erro ou o funcionario nao seja da unidade
     */
    public boolean deleteByIdPerUnit(int id, int unitId) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                                "where id = ? and unit_id = ? and is_active = true"
                )
        ) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar o empregado pelo cpf que esta na unidade
     * A conferencia da unidade e a desativacao acontecem no mesmo comando, entao nao da pra mudar nada no meio
     * @param cpf O cpf do empregado (pode vir com ou sem pontuacao)
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso tenha dado erro ou o funcionario nao seja da unidade
     */
    public boolean deleteByCpfPerUnit(String cpf, int unitId) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                                "where cpf = ? and unit_id = ? and is_active = true"
                )
        ) {

            pstmt.setString(1, cleanCpf(cpf));
            pstmt.setInt(2, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade nem para outra empresa
     * O setor precisa ser da unidade, o grupo de permissao precisa ser global ou da empresa e o email nao pode
     * ficar igual ao de outro funcionario ativo da empresa (tudo conferido no proprio comando)
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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees e set permission_group_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where e.id = ? and e.unit_id = ? and e.is_active = true " +
                                "and exists (select 1 from sectors s where s.id = ? and s.unit_id = e.unit_id and s.is_active = true) " +
                                "and exists (select 1 from permission_groups pg where pg.id = ? and pg.is_active = true and (pg.company_id is null or pg.company_id = e.company_id)) " +
                                "and not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = e.company_id and o.is_active = true and o.id <> e.id)"
                )
        ) {

            pstmt.setInt(1, employee.getPermissionGroupId());
            pstmt.setInt(2, employee.getSectorId());
            pstmt.setString(3, cleanCpf(employee.getCpf()));
            pstmt.setString(4, employee.getName());
            pstmt.setString(5, cleanEmail(employee.getEmail()));
            pstmt.setString(6, cleanPhone(employee.getPhone()));
            pstmt.setInt(7, id);
            pstmt.setInt(8, unitId);
            pstmt.setInt(9, employee.getSectorId());
            pstmt.setInt(10, employee.getPermissionGroupId());
            pstmt.setString(11, cleanEmail(employee.getEmail()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo nome numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade nem para outra empresa
     * Se a unidade tiver mais de um funcionario ativo com esse nome nao atualiza nenhum e devolve false (use o updateByIdPerUnit)
     * Mesmas regras do updateByIdPerUnit para o setor, o grupo de permissao e o email
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
                        "update employees e set permission_group_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where lower(e.name) = lower(?) and e.unit_id = ? and e.is_active = true " +
                                "and (select count(*) from employees x where lower(x.name) = lower(?) and x.unit_id = ? and x.is_active = true) = 1 " +
                                "and exists (select 1 from sectors s where s.id = ? and s.unit_id = e.unit_id and s.is_active = true) " +
                                "and exists (select 1 from permission_groups pg where pg.id = ? and pg.is_active = true and (pg.company_id is null or pg.company_id = e.company_id)) " +
                                "and not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = e.company_id and o.is_active = true and o.id <> e.id)"
                )
        ) {

            pstmt.setInt(1, employee.getPermissionGroupId());
            pstmt.setInt(2, employee.getSectorId());
            pstmt.setString(3, cleanCpf(employee.getCpf()));
            pstmt.setString(4, employee.getName());
            pstmt.setString(5, cleanEmail(employee.getEmail()));
            pstmt.setString(6, cleanPhone(employee.getPhone()));
            pstmt.setString(7, name);
            pstmt.setInt(8, unitId);
            pstmt.setString(9, name);
            pstmt.setInt(10, unitId);
            pstmt.setInt(11, employee.getSectorId());
            pstmt.setInt(12, employee.getPermissionGroupId());
            pstmt.setString(13, cleanEmail(employee.getEmail()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade nem para outra empresa
     * Mesmas regras do updateByIdPerUnit para o setor, o grupo de permissao e o email
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
                        "update employees e set permission_group_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where e.cpf = ? and e.unit_id = ? and e.is_active = true " +
                                "and exists (select 1 from sectors s where s.id = ? and s.unit_id = e.unit_id and s.is_active = true) " +
                                "and exists (select 1 from permission_groups pg where pg.id = ? and pg.is_active = true and (pg.company_id is null or pg.company_id = e.company_id)) " +
                                "and not exists (select 1 from employees o where lower(o.email) = lower(?) and o.company_id = e.company_id and o.is_active = true and o.id <> e.id)"
                )
        ) {

            pstmt.setInt(1, employee.getPermissionGroupId());
            pstmt.setInt(2, employee.getSectorId());
            pstmt.setString(3, cleanCpf(employee.getCpf()));
            pstmt.setString(4, employee.getName());
            pstmt.setString(5, cleanEmail(employee.getEmail()));
            pstmt.setString(6, cleanPhone(employee.getPhone()));
            pstmt.setString(7, cleanCpf(cpf));
            pstmt.setInt(8, unitId);
            pstmt.setInt(9, employee.getSectorId());
            pstmt.setInt(10, employee.getPermissionGroupId());
            pstmt.setString(11, cleanEmail(employee.getEmail()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que atualiza a senha (hash) do empregado pelo id numa unidade especifica
     * A conferencia da unidade e a atualizacao acontecem no mesmo comando, entao nao da pra mudar nada no meio
     * @param id O id do empregado
     * @param passwordHash O hash da nova senha
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updatePasswordHashPerUnit(int id, String passwordHash, int unitId) {

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
                        "update employees set password_hash = ?, updated_at = current_timestamp " +
                                "where id = ? and unit_id = ? and is_active = true"
                )
        ) {

            pstmt.setString(1, passwordHash);
            pstmt.setInt(2, id);
            pstmt.setInt(3, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION' (scoped per unit)
     * So muda o status de funcionario ativo
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnVacationPerUnit(int id, int unitId) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'IN_VACATION', updated_at = current_timestamp " +
                                "where id = ? and unit_id = ? and is_active = true"
                )
        ) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'ACTIVE' (scoped per unit)
     * Se o funcionario estava desativado, reativa ele (is_active = true), desde que a unidade e o setor dele estejam ativos
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActivePerUnit(int id, int unitId) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees e set status = 'ACTIVE', is_active = true, updated_at = current_timestamp " +
                                "where e.id = ? and e.unit_id = ? " +
                                "and exists (select 1 from units u where u.id = e.unit_id and u.is_active = true) " +
                                "and exists (select 1 from sectors s where s.id = e.sector_id and s.is_active = true)"
                )
        ) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}