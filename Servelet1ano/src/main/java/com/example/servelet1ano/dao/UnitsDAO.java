package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Units;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitsDAO {


    /**
     * Metodo que busca todas as units
     *
     * @return List<Units> Lista das units
     */
    public List<Units> searchAll(){

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        String sql = "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                "c.name as companyName, c.id as idCompany, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from units u " +
                "join companies c on u.company_id = c.id " +
                "join addresses a on a.id = u.address_id";

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

                address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                units.add(
                        new Units(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getString("cnpj"),
                                rs.getString("cnae"),
                                rs.getBoolean("is_active"),
                                company,
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            return units;
        }
    }

    /**
     * metodo para buscar por Id
     * @param id Numero unico da unit
     * @return Retorna a unit encontrada
     */
    public Units searchById(int id){
        String sql = "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                "c.name as companyName, c.id as idCompany, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from units u " +
                "join companies c on u.company_id = c.id " +
                "join addresses a on a.id = u.address_id " +
                "where u.id = ?";

        Units unit = null;
        Companies company = null;
        Addresses address = null;

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

                address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                unit = new Units(
                        rs.getInt("id"),
                        rs.getInt("companyId"),
                        rs.getInt("addressId"),
                        rs.getString("name"),
                        rs.getString("cnpj"),
                        rs.getString("cnae"),
                        rs.getBoolean("is_active"),
                        company,
                        address
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return unit;
        }
    }


    /**
     * metodo para buscar por Id da empresa
     * @param companyId O valor unico de cada empresa
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByCompanyId(int companyId){
        String sql = "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                "c.name as companyName, c.id as idCompany, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from units u " +
                "join companies c on u.company_id = c.id " +
                "join addresses a on a.id = u.address_id " +
                "where u.company_id = ?";

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

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

                address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                units.add(
                        new Units(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getString("cnpj"),
                                rs.getString("cnae"),
                                rs.getBoolean("is_active"),
                                company,
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return units;
        }
    }


    /**
     * metodo para buscar as units pelo nome da empresa
     * @param name O nome da empresa
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByNameCompany(String name){
        String sql = "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                "c.name as companyName, c.id as idCompany, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from units u " +
                "join companies c on u.company_id = c.id " +
                "join addresses a on a.id = u.address_id " +
                "where c.name = ?";

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                units.add(
                        new Units(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getString("cnpj"),
                                rs.getString("cnae"),
                                rs.getBoolean("is_active"),
                                company,
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return units;
        }
    }


    /**
     * metodo para buscar por nome da unit
     * @param name O nome da unit
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByName(String name){
        String sql = "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                "c.name as companyName, c.id as idCompany, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from units u " +
                "join companies c on u.company_id = c.id " +
                "join addresses a on a.id = u.address_id " +
                "where u.name like ?";

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                units.add(
                        new Units(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getString("cnpj"),
                                rs.getString("cnae"),
                                rs.getBoolean("is_active"),
                                company,
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return units;
        }
    }


    /**
     * metodo para buscar por cnpj da unit
     * @param cnpj O cnpj da unit
     * @return Retorna a unit encontrada
     */
    public Units searchByCnpj(String cnpj){
        String sql = "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                "c.name as companyName, c.id as idCompany, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from units u " +
                "join companies c on u.company_id = c.id " +
                "join addresses a on a.id = u.address_id " +
                "where u.cnpj = ?";

        Units unit = null;
        Companies company = null;
        Addresses address = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, cnpj);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                unit = new Units(
                        rs.getInt("id"),
                        rs.getInt("companyId"),
                        rs.getInt("addressId"),
                        rs.getString("name"),
                        rs.getString("cnpj"),
                        rs.getString("cnae"),
                        rs.getBoolean("is_active"),
                        company,
                        address
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return unit;
        }
    }

    /**
     * metodo para buscar por cnae da unit
     * @param cnae O cnae (Classificação Nacional de Atividades Econômicas) da unit
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByCnae(String cnae){
        String sql = "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                "c.name as companyName, c.id as idCompany, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from units u " +
                "join companies c on u.company_id = c.id " +
                "join addresses a on a.id = u.address_id " +
                "where u.cnae = ?";

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, cnae);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                units.add(
                        new Units(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getString("cnpj"),
                                rs.getString("cnae"),
                                rs.getBoolean("is_active"),
                                company,
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return units;
        }
    }


    /**
     * metodo para cadastrar novas units no banco
     * @param unit A unit que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    public boolean register(Units unit){

        String sql = "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setBoolean(6, unit.isActive());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * metodo para registrar mais de uma unit
     * @param units Uma lista de units
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Units> units){
        String sql = "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            for (int i = 0; i < units.size(); i++) {
                Units unit = units.get(i);

                pstmt.setInt(1, unit.getCompanyId());
                pstmt.setInt(2, unit.getAddressId());
                pstmt.setString(3, unit.getName());
                pstmt.setString(4, unit.getCnpj());
                pstmt.setString(5, unit.getCnae());
                pstmt.setBoolean(6, unit.isActive());

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
     * metodo para deletar a unit por id
     * @param id Valor unico de cada unit
     * @return true se foi deletada e false caso tenha dado erro
     */
    public boolean deleteById(int id){
        String sql = "delete from units where id = ?";

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
     * metodo que deleta por id da empresa (para quando uma empresa for deletada)
     * @param companyId
     * @return
     */
    public boolean deleteByCompanyId(int companyId){
        String sql = "delete from units where company_id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * metodo que deleta pelo nome
     * @param name O nome da unit
     * @return true se foi deletada e false caso tenha dado erro
     */
    public boolean deleteByName(String name){
        String sql = "delete from units where name like ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values da unit selecionada pelo id
     * @param unit Os valores da unit para ser atualizada
     * @param id O valor unico da unit que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    public boolean updateById(Units unit, int id){
        String sql = "update units set company_id = ?, " +
                "address_id = ?, " +
                "name = ?, " +
                "cnpj = ?, " +
                "cnae = ?, " +
                "is_active = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setBoolean(6, unit.isActive());
            pstmt.setInt(7, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Metodo que muda os values da unit selecionada pelo nome
     * @param unit Os valores da unit para ser atualizada
     * @param name O nome para achar a unit
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByName(Units unit, String name){
        String sql = "update units set company_id = ?, " +
                "address_id = ?, " +
                "name = ?, " +
                "cnpj = ?, " +
                "cnae = ?, " +
                "is_active = ?, " +
                "updated_at = current_timestamp " +
                "where name like ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setBoolean(6, unit.isActive());
            pstmt.setString(7, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
