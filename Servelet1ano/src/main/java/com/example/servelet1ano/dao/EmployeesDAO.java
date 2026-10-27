package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.StatusEmployee;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.util.SqlUtil;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EmployeesDAO implements DAOI<Employees, EmployeesFilter> {

    /**
     * Colunas usadas nas buscas (a senha NAO entra aqui; so a busca de login traz o hash)
     * As juncoes sao left join pra nao sumir um funcionario que esteja com algum vinculo vazio
     */
    private static final String SELECT_FIELDS =
            "e.id, e.cpf, e.name, e.email, e.phone, e.status, e.storage_file_id, e.created_at, e.updated_at, " +
                    "e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, e.sector_id as sectorId, " +
                    "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                    "u.name as unitName, u.id as idUnit, sec.id as idSector, sec.name as sectorName ";

    private static final String FROM_EMPLOYEES =
            "from employees e " +
                    "left join companies c on e.company_id = c.id " +
                    "left join permission_groups pg on e.permission_group_id = pg.id " +
                    "left join units u on u.id = e.unit_id " +
                    "left join sectors sec on sec.id = e.sector_id ";

    private static final String INSERT_EMPLOYEE =
            "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                    "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)";

    private static final String UPDATE_EMPLOYEE =
            "update employees set company_id = ?, " +
                    "permission_group_id = ?, " +
                    "unit_id = ?, " +
                    "sector_id = ?, " +
                    "cpf = ?, " +
                    "name = ?, " +
                    "email = ?, " +
                    "phone = ?, " +
                    "updated_at = current_timestamp ";

    /**
     * Valida os dados do funcionario antes de gravar (cpf, email, nome e, se preenchido, telefone)
     * @param employee O funcionario a validar
     * @return true se os dados forem validos e couberem na tabela
     */
    private boolean isValid(Employees employee) {

        if (employee == null) {
            return false;
        }

        if (!Validators.isCpf(employee.getCpf())) {
            return false;
        }

        if (employee.getEmail() == null
                || employee.getEmail().trim().length() > 255
                || !Validators.isEmail(employee.getEmail())) {
            return false;
        }

        if (employee.getName() == null
                || employee.getName().isBlank()
                || employee.getName().trim().length() > 255) {
            return false;
        }

        if (employee.getPhone() != null && !employee.getPhone().isBlank()
                && !Validators.isPhone(employee.getPhone())) {
            return false;
        }

        return true;
    }

    /**
     * Confere se o funcionario veio com o hash da senha (obrigatorio no cadastro)
     * @param employee O funcionario
     * @return true se o hash estiver preenchido
     */
    private boolean hasPasswordHash(Employees employee) {
        return employee.getPasswordHash() != null
                && !employee.getPasswordHash().isBlank()
                && employee.getPasswordHash().length() <= 255;
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
     * Deixa o email sem espacos nas pontas e em minusculo, pra o mesmo email nao ser cadastrado duas vezes com maiusculas diferentes
     * @param email O email como foi digitado
     * @return O email normalizado, ou null se vier null
     */
    private String cleanEmail(String email) {

        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Devolve o telefone so com DDD + numero (sem pontuacao e sem +55), ou null se estiver vazio
     * @param employee O funcionario
     * @return O telefone normalizado, ou null
     */
    private String phoneOf(Employees employee) {

        if (employee.getPhone() == null || employee.getPhone().isBlank()) {
            return null;
        }

        return Validators.normalizePhone(employee.getPhone());
    }

    /**
     * Confere se o erro do banco foi de valor duplicado (unique violation, SQLState 23505), ex: cpf ou email ja cadastrado
     * Num lote (batch) o erro original pode vir encadeado, por isso percorre a cadeia
     * @param e O erro do banco
     * @return true se foi duplicidade
     */
    private boolean isUniqueViolation(SQLException e) {

        for (SQLException atual = e; atual != null; atual = atual.getNextException()) {
            if ("23505".equals(atual.getSQLState())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Usado nos catch dos metodos que gravam: duplicidade (cpf/email ja cadastrado) vira false; qualquer outro erro vira RuntimeException
     */
    private boolean falseIfDuplicate(SQLException e, String message) {

        if (isUniqueViolation(e)) {
            return false;
        }

        throw new RuntimeException(message, e);
    }

    /**
     * Confere se os vinculos do funcionario fazem sentido juntos:
     * a empresa existe e esta ativa, a unidade e dessa empresa, o setor e dessa unidade
     * e o grupo de permissao e dessa empresa (ou e um grupo global, sem empresa)
     * @param conn A conexao
     * @param employee O funcionario
     * @return true se estiver tudo consistente
     */
    private boolean referencesConsistent(Connection conn, Employees employee) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select 1 from companies c " +
                        "join units u on u.company_id = c.id and u.id = ? and u.is_active = true " +
                        "join sectors s on s.id = ? and s.unit_id = u.id and s.is_active = true " +
                        "join permission_groups pg on pg.id = ? and pg.is_active = true " +
                        "and (pg.company_id = c.id or pg.company_id is null) " +
                        "where c.id = ? and c.is_active = true"
        )) {

            pstmt.setInt(1, employee.getUnitId());
            pstmt.setInt(2, employee.getSectorId());
            pstmt.setInt(3, employee.getPermissionGroupId());
            pstmt.setInt(4, employee.getCompanyId());

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Preenche os 8 primeiros parametros (empresa, grupo, unidade, setor, cpf, nome, email e telefone), iguais no insert e no update
     */
    private void bindData(PreparedStatement pstmt, Employees employee) throws SQLException {

        pstmt.setInt(1, employee.getCompanyId());
        pstmt.setInt(2, employee.getPermissionGroupId());
        pstmt.setInt(3, employee.getUnitId());
        pstmt.setInt(4, employee.getSectorId());
        pstmt.setString(5, cleanCpf(employee.getCpf()));
        pstmt.setString(6, employee.getName().trim());
        pstmt.setString(7, cleanEmail(employee.getEmail()));
        pstmt.setString(8, phoneOf(employee));
    }

    /**
     * Preenche os parametros do insert (os 8 dados, o hash da senha e o status, sempre ACTIVE na criacao)
     */
    private void bindInsert(PreparedStatement pstmt, Employees employee) throws SQLException {

        bindData(pstmt, employee);
        pstmt.setString(9, employee.getPasswordHash());
        pstmt.setString(10, StatusEmployee.ACTIVE.getValor());
    }

    /**
     * Atualiza um funcionario ativo usando a conexao recebida
     * @param where Condicao fixa do codigo (ex: "where id = ? and is_active = true"), nunca texto digitado
     * @param whereParams Valores dos ? da condicao
     */
    private boolean updateEmployee(Connection conn, Employees employee, String where, Object... whereParams) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_EMPLOYEE + where)) {

            bindData(pstmt, employee);

            for (int i = 0; i < whereParams.length; i++) {
                pstmt.setObject(9 + i, whereParams[i]);
            }

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Roda um update simples (desativar, mudar status, mudar senha) de um unico funcionario ou de um grupo deles
     * @param sql Comando fixo do codigo
     * @param params Valores dos ? do comando
     */
    private boolean executeUpdate(String sql, String errorMessage, Object... params) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Monta um funcionario (com empresa, grupo, unidade e setor) a partir da linha atual do ResultSet
     * Nao carrega a senha
     */
    private Employees mapEmployee(ResultSet rs) throws SQLException {

        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;
        Sectors sector = null;

        int idCompany = rs.getInt("idCompany");
        if (!rs.wasNull()) {
            company = new Companies(idCompany, rs.getString("companyName"));
        }

        int idPermissionGroup = rs.getInt("idPermissionGroup");
        if (!rs.wasNull()) {
            permissionGroup = new PermissionGroups(idPermissionGroup, rs.getString("permissionGroup"));
        }

        int idUnit = rs.getInt("idUnit");
        if (!rs.wasNull()) {
            unit = new Units(idUnit, rs.getString("unitName"));
        }

        int idSector = rs.getInt("idSector");
        if (!rs.wasNull()) {
            sector = new Sectors(idSector, rs.getString("sectorName"));
        }

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

        employee.setStorageFileId(rs.getObject("storage_file_id", Integer.class));
        employee.setCreatedAt(rs.getTimestamp("created_at"));
        employee.setUpdatedAt(rs.getTimestamp("updated_at"));

        return employee;
    }


    /**
     * Metodo que busca os funcionarios ativos aplicando os filtros informados
     * Os campos do filter que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * Nao traz a senha (hash); para o login use searchForLogin
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Employees> com os empregados encontrados
     */
    @Override
    public List<Employees> searchAll(EmployeesFilter filter) {

        List<Employees> employees = new ArrayList<>();

        if (filter == null) {
            filter = new EmployeesFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select " + SELECT_FIELDS + FROM_EMPLOYEES + "where e.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and e.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and e.name ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getName().trim()) + "%");
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
            parametros.add("%" + SqlUtil.escapeLike(filter.getCompanyName().trim()) + "%");
        }

        if (filter.getUnitId() != null) {
            sql.append(" and e.unit_id = ?");
            parametros.add(filter.getUnitId());
        }

        if (filter.getUnitName() != null && !filter.getUnitName().isBlank()) {
            sql.append(" and u.name ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getUnitName().trim()) + "%");
        }

        if (filter.getSectorId() != null) {
            sql.append(" and e.sector_id = ?");
            parametros.add(filter.getSectorId());
        }

        if (filter.getSectorName() != null && !filter.getSectorName().isBlank()) {
            sql.append(" and sec.name ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getSectorName().trim()) + "%");
        }

        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            sql.append(" and lower(e.email) = ?");
            parametros.add(cleanEmail(filter.getEmail()));
        }

        sql.append(" order by e.id");

        try (
                Connection conn = ConnectionFactory.connect();
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

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os funcionários no banco de dados.", e);
        }

        return employees;
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * Nao traz a senha (hash)
     * @param id Numero unico do empregado
     * @return Retorna o empregado encontrado ou null caso nao exista
     */
    @Override
    public Employees searchById(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select " + SELECT_FIELDS + FROM_EMPLOYEES + "where e.id = ? and e.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o funcionário de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para buscar o empregado pelo cpf (somente ativos)
     * O cpf é unico no banco, entao devolve só um registro
     * Nao traz a senha (hash)
     * @param cpf O cpf do empregado (pode vir com ou sem pontuacao)
     * @return Retorna o empregado encontrado ou null caso nao exista
     */
    public Employees searchByCpf(String cpf) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select " + SELECT_FIELDS + FROM_EMPLOYEES + "where e.cpf = ? and e.is_active = true"
                )
        ) {

            pstmt.setString(1, cleanCpf(cpf));

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o funcionário pelo cpf no banco de dados.", e);
        }
    }

    /**
     * metodo para buscar o empregado que vai fazer login (somente ativos)
     * E o unico metodo de busca que traz a senha (hash), para o servlet conferir com o PasswordHasher
     * Procura pelo email (sem diferenciar maiusculas) dentro da empresa e, se o nome vier, pelo nome igual
     * @param email O email digitado no login
     * @param companyId O id da empresa escolhida no login
     * @param name O nome digitado no login (se for null ou vazio, nao entra na busca)
     * @return O empregado com o passwordHash preenchido, ou null se nao achou exatamente um
     */
    public Employees searchForLogin(String email, int companyId, String name) {

        if (email == null || email.isBlank()) {
            return null;
        }

        boolean withName = name != null && !name.isBlank();

        String sql = "select e.password_hash, " + SELECT_FIELDS + FROM_EMPLOYEES +
                "where lower(e.email) = ? and e.company_id = ? and e.is_active = true" +
                (withName ? " and lower(e.name) = lower(?)" : "") +
                " limit 2";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, cleanEmail(email));
            pstmt.setInt(2, companyId);

            if (withName) {
                pstmt.setString(3, name.trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Employees employee = mapEmployee(rs);
                employee.setPasswordHash(rs.getString("password_hash"));

                if (rs.next()) {
                    return null;
                }

                return employee;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o funcionário para o login no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar novos empregados no banco (sempre ativo na criacao)
     * A senha ja deve vir como hash no passwordHash (gerado pelo PasswordHasher)
     * O email e gravado em minusculo e o telefone so com DDD + numero
     * @param employee O funcionario que sera cadastrado
     * @return true se foi registrado e false se os dados forem invalidos, os vinculos nao baterem
     * (ex: setor de outra unidade) ou o cpf/email ja estiver cadastrado
     */
    @Override
    public boolean register(Employees employee) {

        if (!isValid(employee) || !hasPasswordHash(employee)) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesConsistent(conn, employee)) {
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_EMPLOYEE)) {

                bindInsert(pstmt, employee);

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            return falseIfDuplicate(e, "Erro ao cadastrar o funcionário no banco de dados.");
        }
    }

    /**
     * metodo para registrar mais de um empregado
     * Todos entram juntos: se algum for invalido ou der erro, nenhum fica salvo (transacao)
     * @param employees Uma lista de empregados
     * @return true se todos foram registrados e false se algum tiver dados invalidos ou duplicados
     */
    public boolean registerLot(List<Employees> employees) {

        if (employees == null || employees.isEmpty()) {
            return false;
        }

        for (Employees employee : employees) {
            if (!isValid(employee) || !hasPasswordHash(employee)) {
                return false;
            }
        }

        try (Connection conn = ConnectionFactory.connect()) {

            // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
            conn.setAutoCommit(false);

            try {

                Set<String> verificados = new HashSet<>();

                for (Employees employee : employees) {
                    String chave = employee.getCompanyId() + ":" + employee.getUnitId() + ":"
                            + employee.getSectorId() + ":" + employee.getPermissionGroupId();

                    if (verificados.add(chave) && !referencesConsistent(conn, employee)) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(INSERT_EMPLOYEE)) {

                    for (Employees employee : employees) {
                        bindInsert(pstmt, employee);
                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                // o rollback fica aqui dentro porque o catch de fora so roda depois da conexao fechada
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            return falseIfDuplicate(e, "Erro ao cadastrar os funcionários em lote no banco de dados.");
        }
    }

    /**
     * metodo para desativar o empregado por id (is_active = false, status = 'INACTIVE')
     * @param id Valor unico de cada empregado
     * @return true se foi desativado e false se o empregado nao existe ou ja estava desativado
     */
    @Override
    public boolean delete(int id) {

        return executeUpdate(
                "update employees set is_active = false, status = 'INACTIVE', updated_at = current_timestamp " +
                        "where id = ? and is_active = true",
                "Erro ao desativar o funcionário de id " + id + " no banco de dados.",
                id
        );
    }

    /**
     * metodo que desativa por id da empresa (para quando uma empresa for desativada)
     * Se a empresa nao tiver funcionarios ativos devolve false, e isso nao e um erro
     * @param idCompany O id da empresa
     * @return true se algum funcionario foi desativado e false se a empresa nao tinha funcionarios ativos
     */
    public boolean deleteByIdCompany(int idCompany) {

        try (Connection conn = ConnectionFactory.connect()) {

            return deleteByIdCompany(idCompany, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar os funcionários da empresa de id " + idCompany + ".", e);
        }
    }

    /**
     * metodo que desativa os empregados de uma empresa usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao)
     * @param idCompany O id da empresa
     * @param conn A conexao (nao e fechada aqui)
     * @return true se algum funcionario foi desativado e false se a empresa nao tinha funcionarios ativos
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByIdCompany(int idCompany, Connection conn) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "update employees set is_active = false, status = 'INACTIVE', updated_at = current_timestamp " +
                        "where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, idCompany);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa os empregados quando uma unidade for desativada
     * Se a unidade nao tiver funcionarios ativos devolve false, e isso nao e um erro
     * @param unitId numero unico da unidade
     * @return true se algum funcionario foi desativado e false se a unidade nao tinha funcionarios ativos
     */
    public boolean deleteByIdUnit(int unitId) {

        try (Connection conn = ConnectionFactory.connect()) {

            return deleteByIdUnit(unitId, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar os funcionários da unidade de id " + unitId + ".", e);
        }
    }

    /**
     * metodo que desativa os empregados de uma unidade usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao, ex: desativar a unidade e seus funcionarios juntos)
     * @param unitId numero unico da unidade
     * @param conn A conexao (nao e fechada aqui)
     * @return true se algum funcionario foi desativado e false se a unidade nao tinha funcionarios ativos
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByIdUnit(int unitId, Connection conn) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                        "where unit_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa os empregados de uma unidade pelo nome da unidade, dentro de uma empresa
     * O nome da unidade precisa ser igual (sem diferenciar maiusculas de minusculas)
     * Se a empresa tiver mais de uma unidade com esse nome, desativa os funcionarios de todas elas
     * @param name O nome da unidade
     * @param companyId O id da empresa dona da unidade (evita desativar funcionarios de outras empresas)
     * @return true se algum funcionario foi desativado e false se nao achou nenhum
     */
    public boolean deleteByNameUnit(String name, int companyId) {

        if (name == null || name.isBlank()) {
            return false;
        }

        return executeUpdate(
                "update employees e set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                        "from units u " +
                        "where e.unit_id = u.id and lower(u.name) = lower(?) and u.company_id = ? and e.is_active = true",
                "Erro ao desativar os funcionários da unidade pelo nome no banco de dados.",
                name.trim(), companyId
        );
    }

    /**
     * metodo que desativa pelo cpf
     * @param cpf O cpf do funcionario (pode vir com ou sem pontuacao)
     * @return true se foi desativado e false se o funcionario nao existe ou ja estava desativado
     */
    public boolean deleteByCpf(String cpf) {

        return executeUpdate(
                "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                        "where cpf = ? and is_active = true",
                "Erro ao desativar o funcionário pelo cpf no banco de dados.",
                cleanCpf(cpf)
        );
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id
     * Nao altera a senha (use updatePasswordHash) nem o status (use os metodos de status)
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos, os vinculos nao baterem,
     * o cpf/email ja for de outro funcionario ou o funcionario nao existir/estiver desativado
     */
    @Override
    public boolean update(Employees employee, int id) {

        if (!isValid(employee)) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesConsistent(conn, employee)) {
                return false;
            }

            return updateEmployee(conn, employee, "where id = ? and is_active = true", id);

        } catch (SQLException e) {
            return falseIfDuplicate(e, "Erro ao atualizar o funcionário de id " + id + " no banco de dados.");
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario (pode vir com ou sem pontuacao)
     * @return true se foi atualizado e false se os dados forem invalidos, os vinculos nao baterem,
     * o cpf/email ja for de outro funcionario ou o funcionario nao existir/estiver desativado
     */
    public boolean updateByCpf(Employees employee, String cpf) {

        if (!isValid(employee)) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesConsistent(conn, employee)) {
                return false;
            }

            return updateEmployee(conn, employee, "where cpf = ? and is_active = true", cleanCpf(cpf));

        } catch (SQLException e) {
            return falseIfDuplicate(e, "Erro ao atualizar o funcionário pelo cpf no banco de dados.");
        }
    }

    /**
     * Metodo que atualiza a senha (hash) do empregado pelo id
     * O passwordHash ja deve vir pronto (gerado pelo PasswordHasher)
     * @param id O id do empregado
     * @param passwordHash O hash da nova senha
     * @return true se foi atualizado e false se o hash for vazio ou o funcionario nao existir/estiver desativado
     */
    public boolean updatePasswordHash(int id, String passwordHash) {

        if (passwordHash == null || passwordHash.isBlank() || passwordHash.length() > 255) {
            return false;
        }

        return executeUpdate(
                "update employees set password_hash = ?, updated_at = current_timestamp where id = ? and is_active = true",
                "Erro ao atualizar a senha do funcionário de id " + id + " no banco de dados.",
                passwordHash, id
        );
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION'
     * @param id O id do empregado
     * @return true se foi atualizado e false se o funcionario nao existe ou esta desativado
     */
    public boolean updateStatusOnVacation(int id) {

        return executeUpdate(
                "update employees set status = 'IN_VACATION', updated_at = current_timestamp where id = ? and is_active = true",
                "Erro ao atualizar o status do funcionário de id " + id + " no banco de dados.",
                id
        );
    }

    /**
     * Metodo que altera o status do empregado para 'ACTIVE'
     * @param id O id do empregado
     * @return true se foi atualizado e false se o funcionario nao existe ou esta desativado
     */
    public boolean updateStatusActive(int id) {

        return executeUpdate(
                "update employees set status = 'ACTIVE', updated_at = current_timestamp where id = ? and is_active = true",
                "Erro ao atualizar o status do funcionário de id " + id + " no banco de dados.",
                id
        );
    }


//    ------------- Metodos específicos para admins das units --------------

    /**
     * metodo para cadastrar novos empregados no banco
     * @param employee O funcionario que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false se o funcionario nao for da unidade informada, os dados forem invalidos,
     * os vinculos nao baterem ou o cpf/email ja estiver cadastrado
     */
    public boolean registerPerUnit(Employees employee, int unitId) {

        if (employee == null || unitId != employee.getUnitId()) {
            return false;
        }

        return register(employee);
    }

    /**
     * metodo para registrar mais de um empregado numa unidade especifica
     * Todos entram juntos: se algum for de outra unidade, invalido ou der erro, nenhum fica salvo (transacao)
     * @param employees Uma lista de empregados
     * @param unitId O id da unidade para validacao
     * @return true se todos foram registrados e false se algum nao for da unidade ou tiver dados invalidos
     */
    public boolean registerLotPerUnit(List<Employees> employees, int unitId) {

        if (employees == null || employees.isEmpty()) {
            return false;
        }

        for (Employees employee : employees) {
            if (employee == null || unitId != employee.getUnitId()) {
                return false;
            }
        }

        return registerLot(employees);
    }

    /**
     * metodo para desativar o empregado por id que esta na unidade
     * @param id Valor unico de cada empregado
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false se o funcionario nao existe, ja estava desativado ou nao e da unidade
     */
    public boolean deleteByIdPerUnit(int id, int unitId) {

        return executeUpdate(
                "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                        "where id = ? and unit_id = ? and is_active = true",
                "Erro ao desativar o funcionário de id " + id + " da unidade " + unitId + ".",
                id, unitId
        );
    }

    /**
     * metodo para desativar o empregado pelo cpf que esta na unidade
     * @param cpf O cpf do empregado (pode vir com ou sem pontuacao)
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false se o funcionario nao existe, ja estava desativado ou nao e da unidade
     */
    public boolean deleteByCpfPerUnit(String cpf, int unitId) {

        return executeUpdate(
                "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
                        "where cpf = ? and unit_id = ? and is_active = true",
                "Erro ao desativar o funcionário pelo cpf na unidade " + unitId + ".",
                cleanCpf(cpf), unitId
        );
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false se os dados forem invalidos, os vinculos nao baterem,
     * o cpf/email ja for de outro funcionario ou o funcionario nao for da unidade
     */
    public boolean updateByIdPerUnit(Employees employee, int id, int unitId) {

        if (!isValid(employee) || unitId != employee.getUnitId()) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesConsistent(conn, employee)) {
                return false;
            }

            return updateEmployee(conn, employee, "where id = ? and unit_id = ? and is_active = true", id, unitId);

        } catch (SQLException e) {
            return falseIfDuplicate(e, "Erro ao atualizar o funcionário de id " + id + " da unidade " + unitId + ".");
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo nome numa unidade especifica
     * So atualiza se existir exatamente um funcionario ativo com esse nome na unidade
     * (se houver mais de um, nao da pra saber qual atualizar, entao use o update por id ou por cpf)
     * O funcionario nao pode ser mudado para outra unidade
     * @param employee Os valores do empregado para ser atualizado
     * @param name O nome para achar o funcionario
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false se os dados forem invalidos, os vinculos nao baterem
     * ou nao achou exatamente um funcionario
     */
    public boolean updateByNamePerUnit(Employees employee, String name, int unitId) {

        if (!isValid(employee) || unitId != employee.getUnitId() || name == null || name.isBlank()) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesConsistent(conn, employee)) {
                return false;
            }

            Integer id = findSingleIdByNameInUnit(conn, name, unitId);

            if (id == null) {
                return false;
            }

            return updateEmployee(conn, employee, "where id = ? and unit_id = ? and is_active = true", id, unitId);

        } catch (SQLException e) {
            return falseIfDuplicate(e, "Erro ao atualizar o funcionário pelo nome na unidade " + unitId + ".");
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf numa unidade especifica
     * O funcionario nao pode ser mudado para outra unidade
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario (pode vir com ou sem pontuacao)
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false se os dados forem invalidos, os vinculos nao baterem,
     * o cpf/email ja for de outro funcionario ou o funcionario nao for da unidade
     */
    public boolean updateByCpfPerUnit(Employees employee, String cpf, int unitId) {

        if (!isValid(employee) || unitId != employee.getUnitId()) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesConsistent(conn, employee)) {
                return false;
            }

            return updateEmployee(conn, employee, "where cpf = ? and unit_id = ? and is_active = true", cleanCpf(cpf), unitId);

        } catch (SQLException e) {
            return falseIfDuplicate(e, "Erro ao atualizar o funcionário pelo cpf na unidade " + unitId + ".");
        }
    }

    /**
     * Metodo que atualiza a senha (hash) do empregado pelo id numa unidade especifica
     * @param id O id do empregado
     * @param passwordHash O hash da nova senha
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false se o hash for vazio ou o funcionario nao existir, estiver desativado ou nao for da unidade
     */
    public boolean updatePasswordHashPerUnit(int id, String passwordHash, int unitId) {

        if (passwordHash == null || passwordHash.isBlank() || passwordHash.length() > 255) {
            return false;
        }

        return executeUpdate(
                "update employees set password_hash = ?, updated_at = current_timestamp " +
                        "where id = ? and unit_id = ? and is_active = true",
                "Erro ao atualizar a senha do funcionário de id " + id + " da unidade " + unitId + ".",
                passwordHash, id, unitId
        );
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false se o funcionario nao existe, esta desativado ou nao e da unidade
     */
    public boolean updateStatusOnVacationPerUnit(int id, int unitId) {

        return executeUpdate(
                "update employees set status = 'IN_VACATION', updated_at = current_timestamp " +
                        "where id = ? and unit_id = ? and is_active = true",
                "Erro ao atualizar o status do funcionário de id " + id + " da unidade " + unitId + ".",
                id, unitId
        );
    }

    /**
     * Metodo que altera o status do empregado para 'ACTIVE' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false se o funcionario nao existe, esta desativado ou nao e da unidade
     */
    public boolean updateStatusActivePerUnit(int id, int unitId) {

        return executeUpdate(
                "update employees set status = 'ACTIVE', updated_at = current_timestamp " +
                        "where id = ? and unit_id = ? and is_active = true",
                "Erro ao atualizar o status do funcionário de id " + id + " da unidade " + unitId + ".",
                id, unitId
        );
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Procura o id do funcionario ativo com esse nome (igual, sem diferenciar maiusculas) dentro de uma unidade
     * @return O id, ou null se nao achou nenhum ou se achou mais de um
     */
    private Integer findSingleIdByNameInUnit(Connection conn, String name, int unitId) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select id from employees " +
                        "where lower(name) = lower(?) and unit_id = ? and is_active = true " +
                        "limit 2"
        )) {

            pstmt.setString(1, name.trim());
            pstmt.setInt(2, unitId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                int id = rs.getInt("id");

                if (rs.next()) {
                    return null;
                }

                return id;
            }
        }
    }

    /**
     * Desfaz a transacao sem esconder o erro original
     */
    private void rollbackQuietly(Connection conn, Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }

}