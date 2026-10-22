package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.PermissionGroupsFilter;
import com.example.servelet1ano.model.PermissionGroups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionGroupsDAO implements DAOI<PermissionGroups, PermissionGroupsFilter> {

    /**
     * Interface usada pra passar os parametros do PreparedStatement para os metodos auxiliares
     */
    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }

    /**
     * Abre a conexao com o banco
     * @return A conexao aberta
     * @throws IllegalStateException se nao for possivel conectar
     */
    private Connection requireConnection() throws SQLException {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    /**
     * Valida os dados do grupo de permissao antes de gravar (o nome é obrigatorio no banco)
     * @param permissionGroup O grupo de permissao a validar
     * @return true se o grupo existir e o nome estiver preenchido
     */
    private boolean isValid(PermissionGroups permissionGroup) {

        if (permissionGroup == null) {
            return false;
        }

        if (permissionGroup.getName() == null || permissionGroup.getName().isBlank()) {
            return false;
        }

        return true;
    }

    /**
     * Executa um insert/update/delete e diz se alguma linha foi afetada
     * @param sql O comando SQL
     * @param binder Quem preenche os parametros do comando
     * @param errorMessage Mensagem da excecao caso de erro
     * @return true se alguma linha foi afetada
     * @throws RuntimeException se ocorrer erro de SQL
     */
    private boolean executeUpdate(String sql, StatementBinder binder, String errorMessage) {

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            binder.bind(pstmt);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * Monta um objeto PermissionGroups a partir da linha atual do ResultSet
     * @param rs O ResultSet posicionado na linha que sera lida
     * @return O PermissionGroups com os dados da linha
     */
    private PermissionGroups mapPermissionGroup(ResultSet rs) throws SQLException {

        return new PermissionGroups(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getObject("company_id", Integer.class)
        );
    }


    /**
     * Metodo que busca os grupos de permissao ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o companyId do filtro vier nulo, busca so os grupos globais (da Aether)
     * Se o filter vier null, busca os grupos globais sem filtro nenhum
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<PermissionGroups> com os grupos encontrados
     */
    @Override
    public List<PermissionGroups> searchAll(PermissionGroupsFilter filter) {

        List<PermissionGroups> permissionGroups = new ArrayList<>();

        if (filter == null) {
            filter = new PermissionGroupsFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select * from permission_groups where is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getCompanyId() == null) {
            sql.append(" and company_id is null");
        } else {
            sql.append(" and company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getId() != null) {
            sql.append(" and id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and name ilike ?");
            parametros.add("%" + filter.getName() + "%");
        }

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

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

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do grupo de permissao
     * @return Retorna o grupo de permissao encontrado ou null caso nao exista
     */
    @Override
    public PermissionGroups searchById(int id) {

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from permission_groups where id = ? and is_active = true"
                )
        ) {

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

    /**
     * metodo para cadastrar novos grupos de permissao no banco
     * O companyId pode vir nulo, quando o grupo for global (da Aether)
     * @param permissionGroup O grupo de permissao que sera cadastrado
     * @return true se foi registrado e false se os dados forem invalidos
     */
    @Override
    public boolean register(PermissionGroups permissionGroup) {

        if (!isValid(permissionGroup)) {
            return false;
        }

        return executeUpdate(
                "insert into permission_groups (name, company_id) values (?, ?)",
                pstmt -> {
                    pstmt.setString(1, permissionGroup.getName());
                    pstmt.setObject(2, permissionGroup.getCompanyId());
                },
                "Erro ao cadastrar grupo de permissão."
        );
    }

    /**
     * metodo para registrar mais de um grupo de permissao
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * @param permissionGroups Uma lista de grupos de permissao
     * @return true se foi registrado e false se algum dos grupos for invalido
     */
    public boolean registerLot(List<PermissionGroups> permissionGroups) {

        if (permissionGroups == null || permissionGroups.isEmpty()) {
            return false;
        }

        for (PermissionGroups permissionGroup : permissionGroups) {

            if (!isValid(permissionGroup)) {
                return false;
            }
        }

        try (Connection conn = requireConnection()) {

            // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
            conn.setAutoCommit(false);

            try (
                    PreparedStatement pstmt = conn.prepareStatement(
                            "insert into permission_groups (name, company_id) values (?, ?)"
                    )
            ) {

                for (PermissionGroups permissionGroup : permissionGroups) {
                    pstmt.setString(1, permissionGroup.getName());
                    pstmt.setObject(2, permissionGroup.getCompanyId());

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                // o rollback fica aqui dentro porque o catch de fora so roda depois da conexao fechada
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
            throw new RuntimeException(
                    "Erro ao executar cadastro em lote.",
                    e
            );
        }
    }

    /**
     * metodo para desativar o grupo de permissao por id (is_active = false)
     * Nao desativa se algum funcionario ativo ainda estiver nesse grupo (a conferencia e a desativacao
     * acontecem no mesmo comando, entao nao da pra um funcionario entrar no grupo no meio)
     * @param id Valor unico de cada grupo de permissao
     * @return true se foi desativado e false caso o grupo ainda tenha funcionarios ativos (ou nao exista)
     */
    @Override
    public boolean delete(int id) {

        return executeUpdate(
                "update permission_groups pg set is_active = false, updated_at = current_timestamp " +
                        "where pg.id = ? and pg.is_active = true " +
                        "and not exists (select 1 from employees e where e.permission_group_id = pg.id and e.is_active = true)",
                pstmt -> pstmt.setInt(1, id),
                "Erro ao desativar grupo de permissão por ID."
        );
    }

    /**
     * metodo para desativar grupos de permissao pelo nome (is_active = false)
     * Mesma regra do delete por id: nao desativa o grupo que ainda tenha funcionarios ativos
     * Atencao: desativa todos os grupos com esse nome, de qualquer empresa
     * @param name O nome do grupo de permissao
     * @return true se algum grupo foi desativado e false caso nenhum seja desativado
     */
    public boolean deleteByName(String name) {

        return executeUpdate(
                "update permission_groups pg set is_active = false, updated_at = current_timestamp " +
                        "where pg.name ilike ? and pg.is_active = true " +
                        "and not exists (select 1 from employees e where e.permission_group_id = pg.id and e.is_active = true)",
                pstmt -> pstmt.setString(1, name),
                "Erro ao desativar grupo de permissão por nome."
        );
    }

    /**
     * metodo que desativa os grupos de permissao de uma empresa (para quando uma empresa for desativada)
     * Se a empresa nao tiver grupos devolve false, e isso nao e um erro
     * @param companyId O id da empresa
     * @return true se foi desativado e false caso a empresa nao tenha grupos
     */
    public boolean deleteByCompanyId(int companyId) {

        try (Connection conn = requireConnection()) {

            return deleteByCompanyId(conn, companyId);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar grupos de permissão da empresa.",
                    e
            );
        }
    }

    /**
     * metodo que desativa os grupos de permissao de uma empresa dentro de uma transacao que ja esta aberta
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Se a empresa nao tiver grupos devolve false, e isso nao e um erro
     * Desative os funcionarios da empresa antes de chamar esse metodo
     * @param conn A conexao da transacao
     * @param companyId O id da empresa
     * @return true se algum grupo foi desativado
     * @throws SQLException se o banco der erro, pra quem chamou desfazer a transacao (rollback)
     */
    public boolean deleteByCompanyId(Connection conn, int companyId) throws SQLException {

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "update permission_groups set is_active = false, updated_at = current_timestamp where company_id = ?"
                )
        ) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que muda os values do grupo de permissao selecionado pelo id
     * So muda o nome: o grupo nao pode ser levado pra outra empresa, porque os funcionarios dele continuariam nele
     * Nao atualiza grupo que ja foi desativado
     * @param permissionGroup Os valores do grupo de permissao para ser atualizado
     * @param id O valor unico do grupo de permissao que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos ou o grupo nao existir/estiver desativado
     */
    @Override
    public boolean update(PermissionGroups permissionGroup, int id) {

        if (!isValid(permissionGroup)) {
            return false;
        }

        return executeUpdate(
                "update permission_groups set name = ?, " +
                        "updated_at = current_timestamp " +
                        "where id = ? and is_active = true",
                pstmt -> {
                    pstmt.setString(1, permissionGroup.getName());
                    pstmt.setInt(2, id);
                },
                "Erro ao atualizar grupo de permissão por ID."
        );
    }

    /**
     * metodo que muda o nome do grupo de permissao selecionado pelo nome atual
     * Mesmas regras do update por id: so muda o nome e nao atualiza grupo desativado
     * Atencao: atualiza todos os grupos ativos com esse nome, de qualquer empresa
     * @param permissionGroup Os valores do grupo de permissao para ser atualizado
     * @param name O nome atual para achar o grupo
     * @return true se foi mudado e false se os dados forem invalidos ou nenhum grupo for encontrado
     */
    public boolean updateByName(PermissionGroups permissionGroup, String name) {

        if (!isValid(permissionGroup)) {
            return false;
        }

        return executeUpdate(
                "update permission_groups set name = ?, " +
                        "updated_at = current_timestamp " +
                        "where name ilike ? and is_active = true",
                pstmt -> {
                    pstmt.setString(1, permissionGroup.getName());
                    pstmt.setString(2, name);
                },
                "Erro ao atualizar grupo de permissão por nome."
        );
    }

}