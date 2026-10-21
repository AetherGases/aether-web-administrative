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
     * Metodo que busca os setores ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Sectors> com os setores encontrados
     */
    @Override
    public List<Sectors> searchAll(SectorsFilter filter){

        List<Sectors> sectors = new ArrayList<>();
        Companies company = null;
        Units unit = null;

        if (filter == null) {
            filter = new SectorsFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                        "c.id as idCompany, c.name as companyName, " +
                        "u.id as idUnit, u.name as unitName " +
                        "from sectors s " +
                        "join companies c on c.id = s.company_id " +
                        "join units u on u.id = s.unit_id " +
                        "where s.is_active = true"
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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return sectors;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()){

                    company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

                    unit = new Units(
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

                    sectors.add(sector);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            return sectors;
        }
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do setor
     * @return Retorna o setor encontrado ou null caso nao exista
     */
    @Override
    public Sectors searchById(int id){

        Sectors sector = null;
        Companies company = null;
        Units unit = null;

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                                "c.id as idCompany, c.name as companyName, " +
                                "u.id as idUnit, u.name as unitName " +
                                "from sectors s " +
                                "join companies c on c.id = s.company_id " +
                                "join units u on u.id = s.unit_id " +
                                "where s.id = ? and s.is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if(rs.next()){
                    company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

                    unit = new Units(
                            rs.getInt("idUnit"),
                            rs.getString("unitName")
                    );

                    sector = new Sectors(
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
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return sector;
        }
    }

    /**
     * metodo para cadastrar novos setores no banco
     * @param sector O setor que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(Sectors sector){

        if (!isValid(sector)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, name, description) " +
                                "values (?, ?, ?, ?)"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getName());
            pstmt.setString(4, sector.getDescription());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de um setor
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * @param sectors Uma lista de setores
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Sectors> sectors){

        if (sectors == null || sectors.isEmpty()) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, name, description) " +
                                "values (?, ?, ?, ?)"
                )
        ){

            try {
                // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
                conn.setAutoCommit(false);

                for (int i = 0; i < sectors.size(); i++) {
                    Sectors sector = sectors.get(i);

                    if (!isValid(sector)) {
                        return false;
                    }

                    pstmt.setInt(1, sector.getUnitId());
                    pstmt.setInt(2, sector.getCompanyId());
                    pstmt.setString(3, sector.getName());
                    pstmt.setString(4, sector.getDescription());

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
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
     * metodo para desativar o setor por id (is_active = false)
     * @param id Valor unico de cada setor
     * @return true se foi desativado e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id){

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where id = ?"
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
     * metodo que desativa os setores de uma unidade (para quando uma unidade for desativada)
     * Se a unidade nao tiver setores devolve false, e isso nao e um erro
     * @param unitId O id da unidade
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByUnitId(int unitId){

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where unit_id = ?"
                )
        ) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa os setores de uma empresa (para quando uma empresa for desativada)
     * Se a empresa nao tiver setores devolve false, e isso nao e um erro
     * @param companyId O id da empresa
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByCompanyId(int companyId){

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where company_id = ?"
                )
        ) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do setor selecionado pelo id
     * @param sector Os valores do setor para ser atualizado
     * @param id O valor unico do setor que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(Sectors sector, int id){

        if (!isValid(sector)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set unit_id = ?, " +
                                "company_id = ?, " +
                                "name = ?, " +
                                "description = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getName());
            pstmt.setString(4, sector.getDescription());
            pstmt.setInt(5, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //    ------------- Metodos específicos para admins das units --------------

    /**
     * metodo para cadastrar novos setores no banco de uma unidade
     * @param sector O setor que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerPerUnit(Sectors sector, int unitId){

        if (!isValid(sector)) {
            return false;
        }

        if(unitId != sector.getUnitId()){
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, name, description) " +
                                "values (?, ?, ?, ?)"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getName());
            pstmt.setString(4, sector.getDescription());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de um setor numa unidade especifica
     * Todos entram juntos: se algum der erro, nenhum fica salvo (transacao)
     * @param sectors Uma lista de setores
     * @param unitId O id da unidade para validacao
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLotPerUnit(List<Sectors> sectors, int unitId){

        if (sectors == null || sectors.isEmpty()) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, name, description) " +
                                "values (?, ?, ?, ?)"
                )
        ){

            try {
                // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
                conn.setAutoCommit(false);

                for (int i = 0; i < sectors.size(); i++) {
                    Sectors sector = sectors.get(i);

                    if (!isValid(sector)) {
                        return false;
                    }

                    if(unitId != sector.getUnitId()){
                        return false;
                    }

                    pstmt.setInt(1, sector.getUnitId());
                    pstmt.setInt(2, sector.getCompanyId());
                    pstmt.setString(3, sector.getName());
                    pstmt.setString(4, sector.getDescription());

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
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
     * metodo para desativar o setor por id que esta na unidade
     * @param id Valor unico de cada setor
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByIdPerUnit(int id, int unitId){

        Sectors sector = searchById(id);

        if(sector == null || unitId != sector.getUnitId()){
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where id = ?"
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
     * metodo que muda os values do setor selecionado pelo id numa unidade especifica
     * O setor nao pode ser mudado para outra unidade
     * @param sector Os valores do setor para ser atualizado
     * @param id O valor unico do setor que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false caso tenha dado erro
     */
    public boolean updateByIdPerUnit(Sectors sector, int id, int unitId){

        if (!isValid(sector)) {
            return false;
        }

        if(unitId != sector.getUnitId()){
            return false;
        }

        Sectors sectorFound = searchById(id);

        if(sectorFound == null || unitId != sectorFound.getUnitId()){
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set unit_id = ?, " +
                                "company_id = ?, " +
                                "name = ?, " +
                                "description = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getName());
            pstmt.setString(4, sector.getDescription());
            pstmt.setInt(5, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do setor selecionado pelo nome numa unidade especifica
     * O setor nao pode ser mudado para outra unidade
     * @param sector Os valores do setor para ser atualizado
     * @param name O nome para achar o setor
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByNamePerUnit(Sectors sector, String name, int unitId){

        if (!isValid(sector)) {
            return false;
        }

        if(unitId != sector.getUnitId()){
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set unit_id = ?, " +
                                "company_id = ?, " +
                                "name = ?, " +
                                "description = ?, " +
                                "updated_at = current_timestamp " +
                                "where lower(name) = lower(?) and unit_id = ?"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getName());
            pstmt.setString(4, sector.getDescription());
            pstmt.setString(5, name);
            pstmt.setInt(6, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}