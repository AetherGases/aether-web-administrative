package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.SectorsFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SectorsDAO implements DAOI<Sectors, SectorsFilter> {

    /**
     * Condicao usada nos updates que desativam setor: so desativa se nao houver funcionario ativo no setor
     * (antes de desativar, os funcionarios precisam ser movidos para outro setor ou desativados)
     */
    private static final String NO_ACTIVE_EMPLOYEES =
            " and not exists (select 1 from employees e where e.sector_id = sectors.id and e.is_active = true)";

    /**
     * Metodo que busca os setores ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * @param filter Objeto com os campos opcionais para filtrar a busca (se for null, busca todos)
     * @return List<Sectors> com os setores encontrados
     */
    @Override
    public List<Sectors> searchAll(SectorsFilter filter){

        if (filter == null) {
            filter = new SectorsFilter();
        }

        List<Sectors> sectors = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                        "c.id as idCompany, c.name as companyName, " +
                        "u.id as idUnit, u.name as unitName " +
                        "from sectors s " +
                        "left join companies c on c.id = s.company_id " +
                        "left join units u on u.id = s.unit_id " +
                        "where s.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and s.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and s.name ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getName().trim()) + "%");
        }

        if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
            sql.append(" and s.description ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getDescription().trim()) + "%");
        }

        if (filter.getUnitId() != null) {
            sql.append(" and s.unit_id = ?");
            parametros.add(filter.getUnitId());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and s.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        sql.append(" order by s.id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()){
                    sectors.add(mapSector(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os setores no banco de dados.", e);
        }

        return sectors;
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do setor
     * @return Retorna o setor encontrado, ou null se nao existir ou estiver desativado
     */
    @Override
    public Sectors searchById(int id){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                                "c.id as idCompany, c.name as companyName, " +
                                "u.id as idUnit, u.name as unitName " +
                                "from sectors s " +
                                "left join companies c on c.id = s.company_id " +
                                "left join units u on u.id = s.unit_id " +
                                "where s.id = ? and s.is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()){
                    return mapSector(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o setor de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para buscar por Id (ativos e inativos)
     * Util para telas que precisam mostrar um setor mesmo depois de desativado
     * @param id Numero unico do setor
     * @return Retorna o setor encontrado, ou null se nao existir
     */
    public Sectors searchByIdIncludingInactive(int id){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                                "c.id as idCompany, c.name as companyName, " +
                                "u.id as idUnit, u.name as unitName " +
                                "from sectors s " +
                                "left join companies c on c.id = s.company_id " +
                                "left join units u on u.id = s.unit_id " +
                                "where s.id = ?"
                )
        ){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()){
                    return mapSector(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o setor de id " + id + " (ativo ou inativo) no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar novos setores no banco
     * @param sector O setor que sera cadastrado
     * @return true se foi registrado e false se os dados forem invalidos (ex: unidade nao pertence a empresa)
     */
    @Override
    public boolean register(Sectors sector){

        if (!isValid(sector)) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!unitBelongsToCompany(conn, sector.getUnitId(), sector.getCompanyId())) {
                return false;
            }

            return insertSector(conn, sector);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar o setor no banco de dados.", e);
        }
    }

    /**
     * metodo para registrar mais de um setor
     * Tudo ou nada: se algum setor for invalido ou der erro, nenhum e cadastrado
     * @param sectors Uma lista de setores
     * @return true se todos foram registrados e false se algum dado for invalido
     */
    public boolean registerLot(List<Sectors> sectors){

        if (sectors == null || sectors.isEmpty()) {
            return false;
        }

        for (Sectors sector : sectors) {
            if (!isValid(sector)) {
                return false;
            }
        }

        try (Connection conn = ConnectionFactory.connect()) {

            conn.setAutoCommit(false);

            try {

                Set<String> verificados = new HashSet<>();

                for (Sectors sector : sectors) {
                    String chave = sector.getUnitId() + ":" + sector.getCompanyId();

                    if (verificados.add(chave)
                            && !unitBelongsToCompany(conn, sector.getUnitId(), sector.getCompanyId())) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, name, description) " +
                                "values (?, ?, ?, ?)"
                )) {

                    for (Sectors sector : sectors) {
                        pstmt.setInt(1, sector.getUnitId());
                        pstmt.setInt(2, sector.getCompanyId());
                        pstmt.setString(3, sector.getName().trim());
                        pstmt.setString(4, descriptionOf(sector));

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
            throw new RuntimeException("Erro ao cadastrar os setores em lote no banco de dados.", e);
        }
    }

    /**
     * metodo para desativar o setor por id (is_active = false)
     * Nao desativa se ainda houver funcionarios ativos no setor: antes e preciso mover ou desativar esses funcionarios
     * (use hasActiveEmployees para saber o motivo quando devolver false)
     * @param id Valor unico de cada setor
     * @return true se foi desativado e false se o setor nao existe, ja estava desativado ou ainda tem funcionarios ativos
     */
    @Override
    public boolean delete(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp " +
                                "where id = ? and is_active = true" + NO_ACTIVE_EMPLOYEES
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar o setor de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que confere se o setor ainda tem funcionarios ativos
     * Util para o servlet avisar o usuario antes de desativar o setor (mover ou desativar os funcionarios primeiro)
     * @param sectorId O id do setor
     * @return true se existe pelo menos um funcionario ativo no setor
     */
    public boolean hasActiveEmployees(int sectorId){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select 1 from employees where sector_id = ? and is_active = true limit 1"
                )
        ) {

            pstmt.setInt(1, sectorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conferir os funcionários do setor de id " + sectorId + " no banco de dados.", e);
        }
    }

    /**
     * metodo que desativa os setores de uma unidade (para quando uma unidade for desativada)
     * Nao confere os funcionarios: quem desativa a unidade ja desativa os funcionarios dela antes
     * @param unitId O id da unidade
     * @return true se algum setor foi desativado e false se a unidade nao tinha setores ativos
     */
    public boolean deleteByUnitId(int unitId){

        try(Connection conn = ConnectionFactory.connect()) {

            return deleteByUnitId(unitId, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar os setores da unidade de id " + unitId + ".", e);
        }
    }

    /**
     * metodo que desativa os setores de uma unidade usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao, ex: desativar a unidade e seus setores juntos)
     * @param unitId O id da unidade
     * @param conn A conexao (nao e fechada aqui)
     * @return true se algum setor foi desativado e false se a unidade nao tinha setores ativos
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByUnitId(int unitId, Connection conn) throws SQLException {

        try(PreparedStatement pstmt = conn.prepareStatement(
                "update sectors set is_active = false, updated_at = current_timestamp " +
                        "where unit_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa os setores de uma empresa (para quando uma empresa for desativada)
     * @param companyId O id da empresa
     * @return true se algum setor foi desativado e false se a empresa nao tinha setores ativos
     */
    public boolean deleteByCompanyId(int companyId){

        try(Connection conn = ConnectionFactory.connect()) {

            return deleteByCompanyId(companyId, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar os setores da empresa de id " + companyId + ".", e);
        }
    }

    /**
     * metodo que desativa os setores de uma empresa usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao, ex: desativar a empresa e tudo o que depende dela juntos)
     * @param companyId O id da empresa
     * @param conn A conexao (nao e fechada aqui)
     * @return true se algum setor foi desativado e false se a empresa nao tinha setores ativos
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByCompanyId(int companyId, Connection conn) throws SQLException {

        try(PreparedStatement pstmt = conn.prepareStatement(
                "update sectors set is_active = false, updated_at = current_timestamp " +
                        "where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa pelo nome, dentro de uma empresa
     * O nome precisa ser igual (sem diferenciar maiusculas de minusculas)
     * Desativa todos os setores ativos da empresa com esse nome
     * Se algum deles ainda tiver funcionarios ativos, nenhum e desativado (mover ou desativar os funcionarios primeiro)
     * @param name O nome do setor
     * @param companyId O id da empresa dona do setor (evita desativar setores de outras empresas)
     * @return true se foi desativado e false se nenhum setor foi encontrado ou algum ainda tem funcionarios ativos
     */
    public boolean deleteByName(String name, int companyId){

        if (name == null || name.isBlank()) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()) {

            try (PreparedStatement check = conn.prepareStatement(
                    "select 1 from sectors " +
                            "where lower(name) = lower(?) and company_id = ? and is_active = true " +
                            "and exists (select 1 from employees e where e.sector_id = sectors.id and e.is_active = true) " +
                            "limit 1"
            )) {

                check.setString(1, name.trim());
                check.setInt(2, companyId);

                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "update sectors set is_active = false, updated_at = current_timestamp " +
                            "where lower(name) = lower(?) and company_id = ? and is_active = true"
            )) {

                pstmt.setString(1, name.trim());
                pstmt.setInt(2, companyId);

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar o setor pelo nome no banco de dados.", e);
        }
    }

    /**
     * metodo que muda os values do setor selecionado pelo id
     * O setor nao pode ser movido para outra unidade (os funcionarios dele ficariam em outra unidade)
     * @param sector Os valores do setor para ser atualizado
     * @param id O valor unico do setor que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos, o setor nao existir/estiver desativado ou for de outra unidade
     */
    @Override
    public boolean update(Sectors sector, int id){

        if (!isValid(sector)) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!unitBelongsToCompany(conn, sector.getUnitId(), sector.getCompanyId())) {
                return false;
            }

            return updateSector(conn, sector, id, sector.getUnitId());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o setor de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que muda os values do setor selecionado pelo nome, dentro de uma empresa
     * So atualiza se existir exatamente um setor ativo com esse nome na empresa
     * (se houver mais de um, nao da pra saber qual atualizar, entao use o update por id)
     * O setor nao pode ser movido para outra empresa nem para outra unidade por este metodo
     * @param sector Os valores do setor para ser atualizado
     * @param name O nome para achar o setor
     * @param companyId O id da empresa dona do setor
     * @return true se foi atualizado e false se os dados forem invalidos, nao achou exatamente um setor ou o setor e de outra unidade
     */
    public boolean updateByName(Sectors sector, String name, int companyId){

        if (!isValid(sector) || sector.getCompanyId() != companyId || name == null || name.isBlank()) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!unitBelongsToCompany(conn, sector.getUnitId(), sector.getCompanyId())) {
                return false;
            }

            Integer id = findSingleIdByName(conn, name, "company_id", companyId);

            if (id == null) {
                return false;
            }

            return updateSector(conn, sector, id, sector.getUnitId());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o setor pelo nome no banco de dados.", e);
        }
    }

    //    ------------- Metodos específicos para admins das units --------------

    /**
     * metodo para cadastrar novos setores no banco de uma unidade
     * @param sector O setor que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false se o setor nao for da unidade informada ou os dados forem invalidos
     */
    public boolean registerPerUnit(Sectors sector, int unitId){

        if (sector == null || unitId != sector.getUnitId()) {
            return false;
        }

        return register(sector);
    }

    /**
     * metodo para registrar mais de um setor numa unidade especifica
     * Tudo ou nada: se algum setor nao for da unidade ou der erro, nenhum e cadastrado
     * @param sectors Uma lista de setores
     * @param unitId O id da unidade para validacao
     * @return true se todos foram registrados e false se algum setor nao for da unidade ou tiver dados invalidos
     */
    public boolean registerLotPerUnit(List<Sectors> sectors, int unitId){

        if (sectors == null || sectors.isEmpty()) {
            return false;
        }

        for (Sectors sector : sectors) {
            if (sector == null || unitId != sector.getUnitId()) {
                return false;
            }
        }

        return registerLot(sectors);
    }

    /**
     * metodo para desativar o setor por id que esta na unidade
     * Nao desativa se ainda houver funcionarios ativos no setor (mover ou desativar os funcionarios primeiro)
     * @param id Valor unico de cada setor
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false se o setor nao existe, ja estava desativado, nao e da unidade ou ainda tem funcionarios ativos
     */
    public boolean deleteByIdPerUnit(int id, int unitId){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp " +
                                "where id = ? and unit_id = ? and is_active = true" + NO_ACTIVE_EMPLOYEES
                )
        ) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar o setor de id " + id + " da unidade " + unitId + ".", e);
        }
    }

    /**
     * metodo que muda os values do setor selecionado pelo id numa unidade especifica
     * O setor nao pode ser movido para outra unidade por este metodo
     * @param sector Os valores do setor para ser atualizado
     * @param id O valor unico do setor que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false se os dados forem invalidos ou o setor nao for da unidade
     */
    public boolean updateByIdPerUnit(Sectors sector, int id, int unitId){

        if (!isValid(sector) || unitId != sector.getUnitId()) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!unitBelongsToCompany(conn, sector.getUnitId(), sector.getCompanyId())) {
                return false;
            }

            return updateSector(conn, sector, id, unitId);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o setor de id " + id + " da unidade " + unitId + ".", e);
        }
    }

    /**
     * metodo que muda os values do setor selecionado pelo nome numa unidade especifica
     * So atualiza se existir exatamente um setor ativo com esse nome na unidade
     * O setor nao pode ser movido para outra unidade por este metodo
     * @param sector Os valores do setor para ser atualizado
     * @param name O nome para achar o setor
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false se os dados forem invalidos ou nao achou exatamente um setor
     */
    public boolean updateByNamePerUnit(Sectors sector, String name, int unitId){

        if (!isValid(sector) || unitId != sector.getUnitId() || name == null || name.isBlank()) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!unitBelongsToCompany(conn, sector.getUnitId(), sector.getCompanyId())) {
                return false;
            }

            Integer id = findSingleIdByName(conn, name, "unit_id", unitId);

            if (id == null) {
                return false;
            }

            return updateSector(conn, sector, id, unitId);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o setor pelo nome na unidade " + unitId + ".", e);
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Monta um setor (com empresa e unidade) a partir da linha atual do ResultSet
     * Como as juncoes sao left join, empresa/unidade ficam null se o setor nao tiver vinculo
     */
    private Sectors mapSector(ResultSet rs) throws SQLException {

        Companies company = null;
        Units unit = null;

        int idCompany = rs.getInt("idCompany");
        if (!rs.wasNull()) {
            company = new Companies(idCompany, rs.getString("companyName"));
        }

        int idUnit = rs.getInt("idUnit");
        if (!rs.wasNull()) {
            unit = new Units(idUnit, rs.getString("unitName"));
        }

        Sectors sector = new Sectors(
                rs.getInt("id"),
                rs.getInt("unitId"),
                rs.getInt("companyId"),
                rs.getString("name"),
                rs.getString("description"),
                company,
                unit,
                rs.getBoolean("is_active")
        );

        sector.setCreatedAt(rs.getTimestamp("created_at"));
        sector.setUpdatedAt(rs.getTimestamp("updated_at"));

        return sector;
    }

    /**
     * Confere os dados do setor antes de ir pro banco (o nome e obrigatorio e os tamanhos seguem a tabela)
     */
    private boolean isValid(Sectors sector){

        if (sector == null || sector.getName() == null || sector.getName().isBlank()) {
            return false;
        }

        if (sector.getName().trim().length() > 150) {
            return false;
        }

        return sector.getDescription() == null || sector.getDescription().trim().length() <= 255;
    }

    /**
     * Devolve a descricao sem espacos nas pontas (ou null se estiver vazia)
     */
    private String descriptionOf(Sectors sector){

        if (sector.getDescription() == null || sector.getDescription().isBlank()) {
            return null;
        }

        return sector.getDescription().trim();
    }

    /**
     * Confere se a unidade existe, esta ativa e pertence a empresa informada
     */
    private boolean unitBelongsToCompany(Connection conn, int unitId, int companyId) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select 1 from units where id = ? and company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, unitId);
            pstmt.setInt(2, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Insere um setor usando a conexao recebida
     */
    private boolean insertSector(Connection conn, Sectors sector) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "insert into sectors (unit_id, company_id, name, description) " +
                        "values (?, ?, ?, ?)"
        )) {

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getName().trim());
            pstmt.setString(4, descriptionOf(sector));

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Atualiza um setor ativo pelo id usando a conexao recebida
     * @param unitScope se for diferente de null, so atualiza se o setor ja estiver nessa unidade (impede mover o setor de unidade)
     */
    private boolean updateSector(Connection conn, Sectors sector, int id, Integer unitScope) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "update sectors set unit_id = ?, " +
                        "company_id = ?, " +
                        "name = ?, " +
                        "description = ?, " +
                        "updated_at = current_timestamp " +
                        "where id = ? and is_active = true"
        );

        if (unitScope != null) {
            sql.append(" and unit_id = ?");
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getName().trim());
            pstmt.setString(4, descriptionOf(sector));
            pstmt.setInt(5, id);

            if (unitScope != null) {
                pstmt.setInt(6, unitScope);
            }

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Procura o id do setor ativo com esse nome (igual, sem diferenciar maiusculas) dentro de uma empresa ou unidade
     * @param scopeColumn Coluna do escopo: use somente "company_id" ou "unit_id" (valores fixos do codigo, nunca texto digitado)
     * @return O id, ou null se nao achou nenhum ou se achou mais de um
     */
    private Integer findSingleIdByName(Connection conn, String name, String scopeColumn, int scopeValue) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select id from sectors " +
                        "where lower(name) = lower(?) and " + scopeColumn + " = ? and is_active = true " +
                        "limit 2"
        )) {

            pstmt.setString(1, name.trim());
            pstmt.setInt(2, scopeValue);

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