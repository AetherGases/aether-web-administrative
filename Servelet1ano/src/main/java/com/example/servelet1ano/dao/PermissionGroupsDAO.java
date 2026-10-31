package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.PermissionGroupsFilter;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PermissionGroupsDAO implements DAOI<PermissionGroups, PermissionGroupsFilter> {

    /**
     * Condicao usada nos updates que desativam grupo: so desativa se nao houver funcionario ativo usando o grupo
     * (antes de desativar, os funcionarios precisam ser movidos para outro grupo ou desativados)
     */
    private static final String NO_ACTIVE_EMPLOYEES =
            " and not exists (select 1 from employees e where e.permission_group_id = permission_groups.id and e.is_active = true)";

    /**
     * Metodo que busca os grupos de permissao ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o companyId vier nulo, NAO filtra por empresa (busca em todas). Para buscar so os grupos globais, use searchGlobal()
     * @param filter Objeto com os campos opcionais para filtrar a busca (se for null, busca todos)
     * @return List<PermissionGroups> com os grupos encontrados
     */
    @Override
    public List<PermissionGroups> searchAll(PermissionGroupsFilter filter){

        if (filter == null) {
            filter = new PermissionGroupsFilter();
        }

        List<PermissionGroups> permissionGroups = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select * from permission_groups where is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getCompanyId() != null) {
            sql.append(" and company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getId() != null) {
            sql.append(" and id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and name ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getName().trim()) + "%");
        }

        sql.append(" order by id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()){
                    permissionGroups.add(mapGroup(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os grupos de permissão no banco de dados.", e);
        }

        return permissionGroups;
    }


    /**
     * Busca um grupo ativo pelo nome exato e empresa.
     *
     * @param name Nome do grupo
     * @param companyId Id da empresa
     * @return Grupo encontrado ou null
     */
    public PermissionGroups searchByNameAndCompany(
            String name,
            int companyId
    ) {

        if (name == null || name.isBlank() || companyId <= 0) {
            return null;
        }

        String sql = """
            SELECT *
            FROM permission_groups
            WHERE name = ?
              AND company_id = ?
              AND is_active = TRUE
            ORDER BY id
            LIMIT 1
            """;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, name);
            pstmt.setInt(2, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapGroup(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar grupo de permissão da empresa.", e
            );
        }
    }


    /**
     * Metodo que busca so os grupos de permissao globais ativos (os que nao pertencem a nenhuma empresa, da Aether)
     * @return List<PermissionGroups> com os grupos globais
     */
    public List<PermissionGroups> searchGlobal(){

        List<PermissionGroups> permissionGroups = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from permission_groups where is_active = true and company_id is null order by id"
                );
                ResultSet rs = pstmt.executeQuery()
        ){

            while (rs.next()){
                permissionGroups.add(mapGroup(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os grupos de permissão globais no banco de dados.", e);
        }

        return permissionGroups;
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do grupo de permissao
     * @return Retorna o grupo de permissao encontrado, ou null se nao existir ou estiver desativado
     */
    @Override
    public PermissionGroups searchById(int id){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from permission_groups where id = ? and is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()){
                    return mapGroup(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o grupo de permissão de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar novos grupos de permissao no banco
     * O companyId pode vir nulo, quando o grupo for global (da Aether)
     * @param permissionGroup O grupo de permissao que sera cadastrado
     * @return true se foi registrado e false se os dados forem invalidos (ex: nome vazio ou empresa inexistente)
     */
    @Override
    public boolean register(PermissionGroups permissionGroup){

        if (!isValid(permissionGroup)) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!companyIsValid(conn, permissionGroup.getCompanyId())) {
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "insert into permission_groups (name, company_id) values (?, ?)"
            )) {

                pstmt.setString(1, permissionGroup.getName().trim());
                setCompany(pstmt, 2, permissionGroup.getCompanyId());

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar o grupo de permissão no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar um grupo de permissao ja ligado as suas permissoes (tabela permission_group_permissions)
     * Tudo ou nada: se der erro em alguma ligacao, nem o grupo nem as ligacoes ficam salvos
     * @param permissionGroup O grupo de permissao que sera cadastrado
     * @param permissionIds Os ids das permissoes (tabela permissions) que o grupo vai ter; ids repetidos sao ignorados
     * @return O id do grupo cadastrado, ou -1 se os dados forem invalidos
     * (ex: nome vazio, empresa inexistente, lista vazia ou alguma permissao inexistente/desativada)
     */
    public int registerWithPermissions(PermissionGroups permissionGroup, List<Integer> permissionIds){

        if (!isValid(permissionGroup) || permissionIds == null || permissionIds.isEmpty()) {
            return -1;
        }

        Set<Integer> ids = new LinkedHashSet<>(permissionIds);

        try (Connection conn = ConnectionFactory.connect()) {

            conn.setAutoCommit(false);

            try {

                if (!companyIsValid(conn, permissionGroup.getCompanyId())
                        || !permissionsAreValid(conn, ids)) {
                    conn.rollback();
                    return -1;
                }

                int groupId;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "insert into permission_groups (name, company_id) values (?, ?) returning id"
                )) {

                    pstmt.setString(1, permissionGroup.getName().trim());
                    setCompany(pstmt, 2, permissionGroup.getCompanyId());

                    try (ResultSet rs = pstmt.executeQuery()) {
                        rs.next();
                        groupId = rs.getInt("id");
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "insert into permission_group_permissions (permission_group_id, permission_id) values (?, ?)"
                )) {

                    for (Integer permissionId : ids) {
                        pstmt.setInt(1, groupId);
                        pstmt.setInt(2, permissionId);

                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                }

                conn.commit();
                return groupId;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar o grupo de permissão com as permissões no banco de dados.", e);
        }
    }

    /**
     * metodo para registrar mais de um grupo de permissao
     * Tudo ou nada: se algum grupo for invalido ou der erro, nenhum e cadastrado
     * @param permissionGroups Uma lista de grupos de permissao
     * @return true se todos foram registrados e false se algum tiver dados invalidos
     */
    public boolean registerLot(List<PermissionGroups> permissionGroups){

        if (permissionGroups == null || permissionGroups.isEmpty()) {
            return false;
        }

        for (PermissionGroups permissionGroup : permissionGroups) {
            if (!isValid(permissionGroup)) {
                return false;
            }
        }

        try (Connection conn = ConnectionFactory.connect()) {

            conn.setAutoCommit(false);

            try {

                Set<Integer> verificadas = new HashSet<>();

                for (PermissionGroups permissionGroup : permissionGroups) {
                    Integer companyId = permissionGroup.getCompanyId();

                    if (companyId != null
                            && verificadas.add(companyId)
                            && !companyIsValid(conn, companyId)) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "insert into permission_groups (name, company_id) values (?, ?)"
                )) {

                    for (PermissionGroups permissionGroup : permissionGroups) {
                        pstmt.setString(1, permissionGroup.getName().trim());
                        setCompany(pstmt, 2, permissionGroup.getCompanyId());

                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar os grupos de permissão em lote no banco de dados.", e);
        }
    }

    /**
     * metodo para desativar o grupo de permissao por id (is_active = false)
     * Nao desativa se ainda houver funcionarios ativos usando o grupo: antes e preciso mover ou desativar esses funcionarios
     * (use hasActiveEmployees para saber o motivo quando devolver false)
     * @param id Valor unico de cada grupo de permissao
     * @return true se foi desativado e false se o grupo nao existe, ja estava desativado ou ainda tem funcionarios ativos
     */
    @Override
    public boolean delete(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update permission_groups set is_active = false, updated_at = current_timestamp " +
                                "where id = ? and is_active = true" + NO_ACTIVE_EMPLOYEES
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar o grupo de permissão de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que confere se o grupo de permissao ainda tem funcionarios ativos usando ele
     * Util para o servlet avisar o usuario antes de desativar o grupo (mover ou desativar os funcionarios primeiro)
     * @param permissionGroupId O id do grupo de permissao
     * @return true se existe pelo menos um funcionario ativo usando o grupo
     */
    public boolean hasActiveEmployees(int permissionGroupId){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select 1 from employees where permission_group_id = ? and is_active = true limit 1"
                )
        ) {

            pstmt.setInt(1, permissionGroupId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conferir os funcionários do grupo de permissão de id " + permissionGroupId + " no banco de dados.", e);
        }
    }

    /**
     * metodo que desativa pelo nome, dentro de uma empresa
     * O nome precisa ser igual (sem diferenciar maiusculas de minusculas)
     * Desativa todos os grupos ativos da empresa com esse nome
     * Se algum deles ainda tiver funcionarios ativos, nenhum e desativado (mover ou desativar os funcionarios primeiro)
     * @param name O nome do grupo de permissao
     * @param companyId O id da empresa dona do grupo (evita desativar grupos de outras empresas); se for null, procura entre os grupos globais
     * @return true se foi desativado e false se nenhum grupo foi encontrado ou algum ainda tem funcionarios ativos
     */
    public boolean deleteByName(String name, Integer companyId){

        if (name == null || name.isBlank()) {
            return false;
        }

        String where = "lower(name) = lower(?) and is_active = true and " +
                (companyId == null ? "company_id is null" : "company_id = ?");

        try(Connection conn = ConnectionFactory.connect()) {

            try (PreparedStatement check = conn.prepareStatement(
                    "select 1 from permission_groups where " + where +
                            " and exists (select 1 from employees e where e.permission_group_id = permission_groups.id and e.is_active = true)" +
                            " limit 1"
            )) {

                check.setString(1, name.trim());

                if (companyId != null) {
                    check.setInt(2, companyId);
                }

                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "update permission_groups set is_active = false, updated_at = current_timestamp where " + where
            )) {

                pstmt.setString(1, name.trim());

                if (companyId != null) {
                    pstmt.setInt(2, companyId);
                }

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar o grupo de permissão pelo nome no banco de dados.", e);
        }
    }

    /**
     * metodo que desativa os grupos de permissao de uma empresa (para quando uma empresa for desativada)
     * Nao confere os funcionarios: quem desativa a empresa ja desativa os funcionarios dela antes
     * @param companyId O id da empresa
     * @return true se algum grupo foi desativado e false se a empresa nao tinha grupos ativos
     */
    public boolean deleteByCompanyId(int companyId){

        try(Connection conn = ConnectionFactory.connect()) {

            return deleteByCompanyId(companyId, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar os grupos de permissão da empresa de id " + companyId + ".", e);
        }
    }

    /**
     * metodo que desativa os grupos de permissao de uma empresa usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao, ex: desativar a empresa e tudo o que depende dela juntos)
     * @param companyId O id da empresa
     * @param conn A conexao (nao e fechada aqui)
     * @return true se algum grupo foi desativado e false se a empresa nao tinha grupos ativos
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByCompanyId(int companyId, Connection conn) throws SQLException {

        try(PreparedStatement pstmt = conn.prepareStatement(
                "update permission_groups set is_active = false, updated_at = current_timestamp " +
                        "where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que muda os values do grupo de permissao selecionado pelo id
     * O grupo nao pode mudar de empresa (os funcionarios que usam o grupo ficariam com permissao de outra empresa)
     * @param permissionGroup Os valores do grupo de permissao para ser atualizado
     * @param id O valor unico do grupo de permissao que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos ou o grupo nao existir/estiver desativado
     */
    @Override
    public boolean update(PermissionGroups permissionGroup, int id){

        if (!isValid(permissionGroup)) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!companyIsValid(conn, permissionGroup.getCompanyId())) {
                return false;
            }

            return updateGroup(conn, permissionGroup, id);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o grupo de permissão de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que muda os values do grupo de permissao selecionado pelo nome, dentro de uma empresa
     * So atualiza se existir exatamente um grupo ativo com esse nome na empresa
     * (se houver mais de um, nao da pra saber qual atualizar, entao use o update por id)
     * @param permissionGroup Os valores do grupo de permissao para ser atualizado
     * @param name O nome para achar o grupo de permissao
     * @param companyId O id da empresa dona do grupo; se for null, procura entre os grupos globais
     * @return true se foi atualizado e false se os dados forem invalidos ou nao achou exatamente um grupo
     */
    public boolean updateByName(PermissionGroups permissionGroup, String name, Integer companyId){

        if (!isValid(permissionGroup)
                || !Objects.equals(permissionGroup.getCompanyId(), companyId)
                || name == null || name.isBlank()) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!companyIsValid(conn, companyId)) {
                return false;
            }

            Integer id = findSingleIdByName(conn, name, companyId);

            if (id == null) {
                return false;
            }

            return updateGroup(conn, permissionGroup, id);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o grupo de permissão pelo nome no banco de dados.", e);
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Monta um grupo de permissao a partir da linha atual do ResultSet
     */
    private PermissionGroups mapGroup(ResultSet rs) throws SQLException {
        return new PermissionGroups(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getObject("company_id", Integer.class)
        );
    }

    /**
     * Confere os dados do grupo antes de ir pro banco (o nome e obrigatorio e tem no maximo 100 caracteres)
     */
    private boolean isValid(PermissionGroups permissionGroup){

        return permissionGroup != null
                && permissionGroup.getName() != null
                && !permissionGroup.getName().isBlank()
                && permissionGroup.getName().trim().length() <= 100;
    }

    /**
     * Confere a empresa do grupo: se for null o grupo e global (valido); senao a empresa precisa existir e estar ativa
     */
    private boolean companyIsValid(Connection conn, Integer companyId) throws SQLException {

        if (companyId == null) {
            return true;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select 1 from companies where id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Confere se todas as permissoes informadas existem e estao ativas (tabela permissions)
     * @param ids Os ids das permissoes, sem repeticao
     * @return true se todas existirem e estiverem ativas
     */
    private boolean permissionsAreValid(Connection conn, Set<Integer> ids) throws SQLException {

        if (ids.contains(null)) {
            return false;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select count(*) from permissions where is_active = true and id = any (?)"
        )) {

            pstmt.setArray(1, conn.createArrayOf("integer", ids.toArray()));

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) == ids.size();
            }
        }
    }

    /**
     * Coloca o companyId no PreparedStatement, aceitando null (grupo global)
     */
    private void setCompany(PreparedStatement pstmt, int index, Integer companyId) throws SQLException {

        if (companyId == null) {
            pstmt.setNull(index, Types.INTEGER);
        } else {
            pstmt.setInt(index, companyId);
        }
    }

    /**
     * Atualiza o nome de um grupo ativo pelo id usando a conexao recebida
     * A empresa nao e alterada: o where exige que seja a mesma empresa do grupo (null conta como "global")
     */
    private boolean updateGroup(Connection conn, PermissionGroups permissionGroup, int id) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "update permission_groups set name = ?, " +
                        "updated_at = current_timestamp " +
                        "where id = ? and company_id is not distinct from ? and is_active = true"
        )) {

            pstmt.setString(1, permissionGroup.getName().trim());
            pstmt.setInt(2, id);
            setCompany(pstmt, 3, permissionGroup.getCompanyId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Procura o id do grupo ativo com esse nome (igual, sem diferenciar maiusculas) dentro de uma empresa (ou entre os globais, se companyId for null)
     * @return O id, ou null se nao achou nenhum ou se achou mais de um
     */
    private Integer findSingleIdByName(Connection conn, String name, Integer companyId) throws SQLException {

        String sql = "select id from permission_groups " +
                "where lower(name) = lower(?) and is_active = true and " +
                (companyId == null ? "company_id is null" : "company_id = ?") +
                " limit 2";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name.trim());

            if (companyId != null) {
                pstmt.setInt(2, companyId);
            }

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
    private void rollbackQuietly(Connection conn, Exception cause){
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }

}