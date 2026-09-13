package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.Units;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectorsDAO {

    /**
     * Metodo que busca todos os setores
     *
     * @return List<Sectors> Lista dos setores
     */
    public List<Sectors> searchAll(){

        List<Sectors> sectors = new ArrayList<>();
        Companies company = null;
        Units unit = null;

        String sql = "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                "c.id as idCompany, c.name as companyName, " +
                "u.id as idUnit, u.name as unitName " +
                "from sectors s " +
                "join companies c on c.id = s.company_id " +
                "join units u on u.id = s.unit_id";

        try (
                Connection conn = ConnectionFactory.connect();
                Statement stmt = conn.createStatement()
        ){

            ResultSet rs = stmt.executeQuery(sql);

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
     * metodo para buscar por Id
     * @param id Numero unico do setor
     * @return Retorna o setor encontrado
     */
    public Sectors searchById(int id){
        String sql = "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                "c.id as idCompany, c.name as companyName, " +
                "u.id as idUnit, u.name as unitName " +
                "from sectors s " +
                "join companies c on c.id = s.company_id " +
                "join units u on u.id = s.unit_id " +
                "where s.id = ?";

        Sectors sector = null;
        Companies company = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
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
     * metodo para buscar setores pela descricao
     * @param description A descricao do setor
     * @return List<Sectors> com os setores encontrados
     */
    public List<Sectors> searchByDescription(String description){
        String sql = "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                "c.id as idCompany, c.name as companyName, " +
                "u.id as idUnit, u.name as unitName " +
                "from sectors s " +
                "join companies c on c.id = s.company_id " +
                "join units u on u.id = s.unit_id " +
                "where s.description like ?";

        List<Sectors> sectors = new ArrayList<>();
        Companies company = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, description);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

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
        } finally {
            return sectors;
        }
    }

    /**
     * metodo para buscar setores pelo id da unidade
     * @param unitId O valor unico de cada unidade
     * @return List<Sectors> com os setores encontrados
     */
    public List<Sectors> searchByUnitId(int unitId){
        String sql = "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                "c.id as idCompany, c.name as companyName, " +
                "u.id as idUnit, u.name as unitName " +
                "from sectors s " +
                "join companies c on c.id = s.company_id " +
                "join units u on u.id = s.unit_id " +
                "where u.id = ?";

        List<Sectors> sectors = new ArrayList<>();
        Companies company = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, unitId);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

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
        } finally {
            return sectors;
        }
    }

    /**
     * metodo para buscar setores pelo id da empresa
     * @param companyId O valor unico de cada empresa
     * @return List<Sectors> com os setores encontrados
     */
    public List<Sectors> searchByCompanyId(int companyId){
        String sql = "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                "c.id as idCompany, c.name as companyName, " +
                "u.id as idUnit, u.name as unitName " +
                "from sectors s " +
                "join companies c on c.id = s.company_id " +
                "join units u on u.id = s.unit_id " +
                "where c.id = ?";

        List<Sectors> sectors = new ArrayList<>();
        Companies company = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, companyId);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

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
        } finally {
            return sectors;
        }
    }

    /**
     * metodo para cadastrar novos setores no banco
     * @param sector O setor que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean register(Sectors sector){

        String sql = "insert into sectors (unit_id, company_id, description) " +
                "values (?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
        String sql = "insert into sectors (unit_id, company_id, description) " +
                "values (?, ?, ?)";

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

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
     * metodo para deletar o setor por id
     * @param id Valor unico de cada setor
     * @return true se foi deletado e false caso tenha dado erro
     */
    public boolean deleteById(int id){
        String sql = "delete from sectors where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que deleta pelo nome/descricao
     * @param description A descricao do setor
     * @return true se foi deletado e false caso tenha dado erro
     */
    public boolean deleteByDescription(String description){
        String sql = "delete from sectors where description like ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
    public boolean updateById(Sectors sector, int id){
        String sql = "update sectors set unit_id = ?, " +
                "company_id = ?, " +
                "description = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

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
        String sql = "update sectors set unit_id = ?, " +
                "company_id = ?, " +
                "description = ?, " +
                "updated_at = current_timestamp " +
                "where description like ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

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
     * Metodo que busca todos os setores da unidade do usuario
     *
     * @param unitId id unico da unidade
     * @return List<Sectors> Lista dos setores
     */
    public List<Sectors> searchAllPerUnit(int unitId){

        List<Sectors> sectors = new ArrayList<>();
        Companies company = null;
        Units unit = null;

        String sql = "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                "c.id as idCompany, c.name as companyName, " +
                "u.id as idUnit, u.name as unitName " +
                "from sectors s " +
                "join companies c on c.id = s.company_id " +
                "join units u on u.id = s.unit_id " +
                "where u.id = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, unitId);

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
     * metodo para buscar por Id se estiver na unidade
     * @param id Numero unico do setor
     * @param unitId numero unico da unidade
     * @return Retorna o setor encontrado
     */
    public Sectors searchByIdPerUnit(int id, int unitId){
        String sql = "select s.*, s.company_id as companyId, s.unit_id as unitId, " +
                "c.id as idCompany, c.name as companyName, " +
                "u.id as idUnit, u.name as unitName " +
                "from sectors s " +
                "join companies c on c.id = s.company_id " +
                "join units u on u.id = s.unit_id " +
                "where s.id = ? and u.id = ?";

        Sectors sector = null;
        Companies company = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, id);
            pstmt.setInt(2, unitId);

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
     * metodo para cadastrar novos setores no banco de uma unidade
     * @param sector O setor que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerPerUnit(Sectors sector, int unitId){

        String sql = "insert into sectors (unit_id, company_id, description) " +
                "values (?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
        String sql = "insert into sectors (unit_id, company_id, description) " +
                "values (?, ?, ?)";

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

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
     * metodo para deletar o setor por id que esta na unidade
     * @param id Valor unico de cada setor
     * @param unitId Id da unidade para validacao
     * @return true se foi deletado e false caso tenha dado erro
     */
    public boolean deleteByIdPerUnit(int id, int unitId){
        String sql = "delete from sectors where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

        String sql = "update sectors set unit_id = ?, " +
                "company_id = ?, " +
                "description = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

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
        String sql = "update sectors set unit_id = ?, " +
                "company_id = ?, " +
                "description = ?, " +
                "updated_at = current_timestamp " +
                "where description like ? and unit_id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

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
