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
     * Metodo que busca os setores ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Sectors> com os setores encontrados
     */
    @Override
    public List<Sectors> searchAll(SectorsFilter filter){

        List<Sectors> sectors = new ArrayList<>();
        Companies company = null;
        Units unit = null;

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
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                sectors.add(
                        new Sectors(
                                rs.getInt("id"),
                                rs.getInt("unitId"),
                                rs.getInt("companyId"),
                                rs.getString("description"),
                                company,
                                unit
                        )
                );
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
     * @return Retorna o setor encontrado
     */
    @Override
    public Sectors searchById(int id){

        Sectors sector = null;
        Companies company = null;
        Units unit = null;

        try (
                Connection conn = ConnectionFactory.connect();
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

            ResultSet rs = pstmt.executeQuery();

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
                        rs.getString("description"),
                        company,
                        unit
                );
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

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, description) " +
                                "values (?, ?, ?)"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getDescription());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de um setor
     * @param sectors Uma lista de setores
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Sectors> sectors){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, description) " +
                                "values (?, ?, ?)"
                )
        ){

            for (int i = 0; i < sectors.size(); i++) {
                Sectors sector = sectors.get(i);

                pstmt.setInt(1, sector.getUnitId());
                pstmt.setInt(2, sector.getCompanyId());
                pstmt.setString(3, sector.getDescription());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

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

        try(
                Connection conn = ConnectionFactory.connect();
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
     * metodo que desativa pelo nome/descricao
     * @param description A descricao do setor
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByDescription(String description){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where description ilike ?"
                )
        ) {

            pstmt.setString(1, description);

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

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set unit_id = ?, " +
                                "company_id = ?, " +
                                "description = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getDescription());
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do setor selecionado pela descricao
     * @param sector Os valores do setor para ser atualizado
     * @param description A descricao para achar o setor
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByDescription(Sectors sector, String description){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set unit_id = ?, " +
                                "company_id = ?, " +
                                "description = ?, " +
                                "updated_at = current_timestamp " +
                                "where description ilike ?"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getDescription());
            pstmt.setString(4, description);

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

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, description) " +
                                "values (?, ?, ?)"
                )
        ){
            if(unitId != sector.getUnitId()){
                return false;
            }

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getDescription());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de um setor numa unidade especifica
     * @param sectors Uma lista de setores
     * @param unitId O id da unidade para validacao
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLotPerUnit(List<Sectors> sectors, int unitId){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into sectors (unit_id, company_id, description) " +
                                "values (?, ?, ?)"
                )
        ){

            for (int i = 0; i < sectors.size(); i++) {
                Sectors sector = sectors.get(i);

                if(unitId != sector.getUnitId()){
                    return false;
                }

                pstmt.setInt(1, sector.getUnitId());
                pstmt.setInt(2, sector.getCompanyId());
                pstmt.setString(3, sector.getDescription());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

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

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set is_active = false, updated_at = current_timestamp where id = ?"
                )
        ) {

            Sectors sector = searchById(id);

            if(sector == null || unitId != sector.getUnitId()){
                return false;
            }

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do setor selecionado pelo id numa unidade especifica
     * @param sector Os valores do setor para ser atualizado
     * @param id O valor unico do setor que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false caso tenha dado erro
     */
    public boolean updateByIdPerUnit(Sectors sector, int id, int unitId){
        Sectors sectorFound = searchById(id);

        if(sectorFound == null || unitId != sectorFound.getUnitId()){
            return false;
        }

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set unit_id = ?, " +
                                "company_id = ?, " +
                                "description = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getDescription());
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do setor selecionado pela descricao numa unidade especifica
     * @param sector Os valores do setor para ser atualizado
     * @param description A descricao para achar o setor
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByDescriptionPerUnit(Sectors sector, String description, int unitId){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update sectors set unit_id = ?, " +
                                "company_id = ?, " +
                                "description = ?, " +
                                "updated_at = current_timestamp " +
                                "where description ilike ? and unit_id = ?"
                )
        ){

            pstmt.setInt(1, sector.getUnitId());
            pstmt.setInt(2, sector.getCompanyId());
            pstmt.setString(3, sector.getDescription());
            pstmt.setString(4, description);
            pstmt.setInt(5, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}