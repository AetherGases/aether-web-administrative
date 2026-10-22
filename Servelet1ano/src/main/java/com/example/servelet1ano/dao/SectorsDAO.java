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

    /**
     * Interface usada pra passar os parametros do PreparedStatement para os metodos auxiliares
     */
    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }

    /**
     * Select base dos setores, com os joins de empresa e unidade
     * Termina com espaco, entao é so colocar o where depois
     */
    private static final String SELECT_SECTORS =
            "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                    "c.id as idCompany, c.name as companyName, " +
                    "u.id as idUnit, u.name as unitName " +
                    "from sectors s " +
                    "join companies c on c.id = s.company_id " +
                    "join units u on u.id = s.unit_id ";

    /**
     * Comando de cadastro do setor (register e os cadastros em lote usam o mesmo)
     */
    private static final String INSERT_SECTOR =
            "insert into sectors (unit_id, company_id, name, description) " +
                    "values (?, ?, ?, ?)";

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
     * Valida os dados do setor antes de gravar (o nome é obrigatorio no banco)
     * @param sector O setor a validar
     * @return true se o setor existir e o nome estiver preenchido
     */
    private boolean isValid(Sectors sector) {

        if (sector == null) {
            return false;
        }

        if (sector.getName() == null || sector.getName().isBlank()) {
            return false;
        }

        return true;
    }

    /**
     * Confere se o setor tem a unidade e a empresa informadas (os dois ids sao obrigatorios pra cadastrar ou atualizar)
     * Sem isso o id ficaria 0 e o banco recusaria por causa da chave estrangeira
     * @param sector O setor a conferir
     * @return true se o id da unidade e o id da empresa forem maiores que zero
     */
    private boolean hasParents(Sectors sector) {

        if (sector == null) {
            return false;
        }

        if (sector.getUnitId() <= 0 || sector.getCompanyId() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Confere se o setor é da unidade informada
     * @param sector O setor a conferir
     * @param unitId O id da unidade
     * @return true se o setor existir e a unidade dele for a informada
     */
    private boolean belongsToUnit(Sectors sector, int unitId) {
        return sector != null && sector.getUnitId() == unitId;
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
     * Monta um objeto Sectors a partir da linha atual do ResultSet (com empresa e unidade)
     * @param rs O ResultSet posicionado na linha que sera lida (vindo do SELECT_SECTORS)
     * @return O Sectors com os dados da linha
     */
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

    /**
     * Coloca os valores do setor no PreparedStatement (unidade, empresa, nome e descricao)
     * Usado no cadastro e nos updates que mudam tambem a unidade e a empresa
     * @param pstmt O PreparedStatement que vai receber os valores
     * @param sector O setor com os valores
     */
    private void bindSector(PreparedStatement pstmt, Sectors sector) throws SQLException {

        pstmt.setInt(1, sector.getUnitId());
        pstmt.setInt(2, sector.getCompanyId());
        pstmt.setString(3, sector.getName());
        pstmt.setString(4, sector.getDescription());
    }

    /**
     * Cadastra uma lista de setores numa transacao: todos entram juntos, se algum der erro nenhum fica salvo
     * A validacao dos setores é feita antes por quem chama
     * @param sectors A lista de setores
     * @param errorMessage Mensagem da excecao caso de erro
     * @return true se todos foram registrados
     * @throws RuntimeException se ocorrer erro de SQL
     */
    private boolean executeBatch(List<Sectors> sectors, String errorMessage) {

        try (Connection conn = requireConnection()) {

            // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SECTOR)) {

                for (Sectors sector : sectors) {
                    bindSector(pstmt, sector);
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

                throw new RuntimeException(errorMessage, e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }


    /**
     * Metodo que busca os setores ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Sectors> com os setores encontrados
     */
    @Override
    public List<Sectors> searchAll(SectorsFilter filter) {

        List<Sectors> sectors = new ArrayList<>();

        if (filter == null) {
            filter = new SectorsFilter();
        }

        StringBuilder sql = new StringBuilder(
                SELECT_SECTORS + "where s.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and s.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and s.name ilike ?");
            parametros.add("%" + filter.getName() + "%");
        }

        if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
            sql.append(" and s.description ilike ?");
            parametros.add("%" + filter.getDescription() + "%");
        }

        if (filter.getUnitId() != null) {
            sql.append(" and u.id = ?");
            parametros.add(filter.getUnitId());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and c.id = ?");
            parametros.add(filter.getCompanyId());
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

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do setor
     * @return Retorna o setor encontrado ou null caso nao exista
     */
    @Override
    public Sectors searchById(int id) {

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        SELECT_SECTORS + "where s.id = ? and s.is_active = true"
                )
        ) {

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

    /**
     * metodo para cadastrar novos setores no banco
     * @param sector O setor que sera cadastrado
     * @return true se foi registrado e false se os dados forem invalidos
     */
    @Override
    public boolean register(Sectors sector) {

        if (!isValid(sector)) {
            return false;
        }

        if (!hasParents(sector)) {
            return false;
        }

        return executeUpdate(
                INSERT_SECTOR,
                pstmt -> bindSector(pstmt, sector),
                "Erro ao cadastrar setor."
        );
    }

    /**
     * metodo para registrar mais de um setor
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * @param sectors Uma lista de setores
     * @return true se foi registrado e false se algum dos setores for invalido
     */
    public boolean registerLot(List<Sectors> sectors) {

        if (sectors == null || sectors.isEmpty()) {
            return false;
        }

        for (Sectors sector : sectors) {

            if (!isValid(sector)) {
                return false;
            }

            if (!hasParents(sector)) {
                return false;
            }
        }

        return executeBatch(sectors, "Erro ao cadastrar setores em lote.");
    }

    /**
     * metodo para desativar o setor por id (is_active = false)
     * Se o setor ainda tiver funcionarios ativos nao desativa e devolve false (desative ou mude os funcionarios antes)
     * @param id Valor unico de cada setor
     * @return true se foi desativado e false caso o setor ainda tenha funcionarios ativos (ou nao exista)
     */
    @Override
    public boolean delete(int id) {

        return executeUpdate(
                "update sectors s set is_active = false, updated_at = current_timestamp " +
                        "where s.id = ? " +
                        "and not exists (select 1 from employees e where e.sector_id = s.id and e.is_active = true)",
                pstmt -> pstmt.setInt(1, id),
                "Erro ao desativar setor."
        );
    }

    /**
     * metodo que desativa os setores de uma unidade (para quando uma unidade for desativada)
     * Se a unidade nao tiver setores devolve false, e isso nao e um erro
     * @param unitId O id da unidade
     * @return true se foi desativado e false caso a unidade nao tenha setores
     */
    public boolean deleteByUnitId(int unitId) {

        try (Connection conn = requireConnection()) {

            return deleteByUnitId(conn, unitId);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar setores da unidade.",
                    e
            );
        }
    }

    /**
     * metodo que desativa os setores de uma unidade dentro de uma transacao que ja esta aberta
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Se a unidade nao tiver setores devolve false, e isso nao e um erro
     * Desative os funcionarios da unidade antes de chamar esse metodo
     * @param conn A conexao da transacao
     * @param unitId O id da unidade
     * @return true se algum setor foi desativado
     * @throws SQLException se o banco der erro, pra quem chamou desfazer a transacao (rollback)
     */
    public boolean deleteByUnitId(Connection conn, int unitId) throws SQLException {

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where unit_id = ?"
                )
        ) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa os setores de uma empresa (para quando uma empresa for desativada)
     * Se a empresa nao tiver setores devolve false, e isso nao e um erro
     * @param companyId O id da empresa
     * @return true se foi desativado e false caso a empresa nao tenha setores
     */
    public boolean deleteByCompanyId(int companyId) {

        try (Connection conn = requireConnection()) {

            return deleteByCompanyId(conn, companyId);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar setores da empresa.",
                    e
            );
        }
    }

    /**
     * metodo que desativa os setores de uma empresa dentro de uma transacao que ja esta aberta
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Se a empresa nao tiver setores devolve false, e isso nao e um erro
     * Desative os funcionarios da empresa antes de chamar esse metodo
     * @param conn A conexao da transacao
     * @param companyId O id da empresa
     * @return true se algum setor foi desativado
     * @throws SQLException se o banco der erro, pra quem chamou desfazer a transacao (rollback)
     */
    public boolean deleteByCompanyId(Connection conn, int companyId) throws SQLException {

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where company_id = ?"
                )
        ) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa setores pelo nome (is_active = false)
     * Atencao: desativa todos os setores com esse nome, de qualquer unidade ou empresa, e nao confere
     * se ainda ha funcionarios ativos neles (diferente do delete por id)
     * @param name O nome do setor
     * @return true se algum setor foi desativado e false caso nenhum seja encontrado
     */
    public boolean deleteByName(String name) {

        return executeUpdate(
                "update sectors set is_active = false, updated_at = current_timestamp where name ilike ?",
                pstmt -> pstmt.setString(1, name),
                "Erro ao desativar setor por nome."
        );
    }

    /**
     * metodo que muda os values do setor selecionado pelo id
     * @param sector Os valores do setor para ser atualizado
     * @param id O valor unico do setor que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos ou o setor nao existir
     */
    @Override
    public boolean update(Sectors sector, int id) {

        if (!isValid(sector)) {
            return false;
        }

        if (!hasParents(sector)) {
            return false;
        }

        return executeUpdate(
                "update sectors set unit_id = ?, " +
                        "company_id = ?, " +
                        "name = ?, " +
                        "description = ?, " +
                        "updated_at = current_timestamp " +
                        "where id = ?",
                pstmt -> {
                    bindSector(pstmt, sector);
                    pstmt.setInt(5, id);
                },
                "Erro ao atualizar setor."
        );
    }

    /**
     * metodo que muda os values do setor selecionado pelo nome atual
     * Atencao: atualiza todos os setores com esse nome, de qualquer unidade ou empresa
     * Mesmas regras do update por id
     * @param sector Os valores do setor para ser atualizado
     * @param name O nome atual para achar o setor
     * @return true se foi mudado e false se os dados forem invalidos ou nenhum setor for encontrado
     */
    public boolean updateByName(Sectors sector, String name) {

        if (!isValid(sector)) {
            return false;
        }

        if (!hasParents(sector)) {
            return false;
        }

        return executeUpdate(
                "update sectors set unit_id = ?, " +
                        "company_id = ?, " +
                        "name = ?, " +
                        "description = ?, " +
                        "updated_at = current_timestamp " +
                        "where name ilike ?",
                pstmt -> {
                    bindSector(pstmt, sector);
                    pstmt.setString(5, name);
                },
                "Erro ao atualizar setor por nome."
        );
    }

    //    ------------- Metodos específicos para admins das units --------------

    /**
     * metodo para cadastrar novos setores no banco de uma unidade
     * @param sector O setor que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false se os dados forem invalidos ou o setor nao for da unidade
     */
    public boolean registerPerUnit(Sectors sector, int unitId) {

        if (!belongsToUnit(sector, unitId)) {
            return false;
        }

        return register(sector);
    }

    /**
     * metodo para registrar mais de um setor numa unidade especifica
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * @param sectors Uma lista de setores
     * @param unitId O id da unidade para validacao
     * @return true se foi registrado e false se algum setor for invalido ou nao for da unidade
     */
    public boolean registerLotPerUnit(List<Sectors> sectors, int unitId) {

        if (sectors == null || sectors.isEmpty()) {
            return false;
        }

        for (Sectors sector : sectors) {

            if (!isValid(sector)) {
                return false;
            }

            if (!hasParents(sector)) {
                return false;
            }

            if (!belongsToUnit(sector, unitId)) {
                return false;
            }
        }

        return executeBatch(sectors, "Erro ao cadastrar setores da unidade em lote.");
    }

    /**
     * metodo para desativar o setor por id que esta na unidade
     * A conferencia da unidade e a desativacao acontecem no mesmo comando, entao nao da pra mudar nada no meio
     * Se o setor ainda tiver funcionarios ativos nao desativa e devolve false
     * @param id Valor unico de cada setor
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso o setor nao seja da unidade ou ainda tenha funcionarios ativos
     */
    public boolean deleteByIdPerUnit(int id, int unitId) {

        return executeUpdate(
                "update sectors s set is_active = false, updated_at = current_timestamp " +
                        "where s.id = ? and s.unit_id = ? and s.is_active = true " +
                        "and not exists (select 1 from employees e where e.sector_id = s.id and e.is_active = true)",
                pstmt -> {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, unitId);
                },
                "Erro ao desativar setor da unidade."
        );
    }

    /**
     * metodo que muda os values do setor selecionado pelo id numa unidade especifica
     * O setor nao pode ser mudado para outra unidade nem para outra empresa (so o nome e a descricao mudam)
     * A conferencia da unidade e a atualizacao acontecem no mesmo comando, entao nao da pra mudar nada no meio
     * @param sector Os valores do setor para ser atualizado
     * @param id O valor unico do setor que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false se os dados forem invalidos ou o setor nao for da unidade
     */
    public boolean updateByIdPerUnit(Sectors sector, int id, int unitId) {

        if (!isValid(sector)) {
            return false;
        }

        if (!belongsToUnit(sector, unitId)) {
            return false;
        }

        return executeUpdate(
                "update sectors set name = ?, " +
                        "description = ?, " +
                        "updated_at = current_timestamp " +
                        "where id = ? and unit_id = ? and is_active = true",
                pstmt -> {
                    pstmt.setString(1, sector.getName());
                    pstmt.setString(2, sector.getDescription());
                    pstmt.setInt(3, id);
                    pstmt.setInt(4, unitId);
                },
                "Erro ao atualizar setor da unidade."
        );
    }

    /**
     * metodo que muda os values do setor selecionado pelo nome numa unidade especifica
     * O setor nao pode ser mudado para outra unidade nem para outra empresa (so o nome e a descricao mudam)
     * Só mexe nos setores ativos da unidade
     * Se a unidade tiver mais de um setor ativo com esse nome nao atualiza nenhum e devolve false (use o updateByIdPerUnit)
     * @param sector Os valores do setor para ser atualizado
     * @param name O nome para achar o setor
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false se os dados forem invalidos ou alguma regra acima nao passar
     */
    public boolean updateByNamePerUnit(Sectors sector, String name, int unitId) {

        if (!isValid(sector)) {
            return false;
        }

        if (!belongsToUnit(sector, unitId)) {
            return false;
        }

        return executeUpdate(
                "update sectors set name = ?, " +
                        "description = ?, " +
                        "updated_at = current_timestamp " +
                        "where lower(name) = lower(?) and unit_id = ? and is_active = true " +
                        "and (select count(*) from sectors where lower(name) = lower(?) and unit_id = ? and is_active = true) = 1",
                pstmt -> {
                    pstmt.setString(1, sector.getName());
                    pstmt.setString(2, sector.getDescription());
                    pstmt.setString(3, name);
                    pstmt.setInt(4, unitId);
                    pstmt.setString(5, name);
                    pstmt.setInt(6, unitId);
                },
                "Erro ao atualizar setor por nome na unidade."
        );
    }

}