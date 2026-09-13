package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Units;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitsDAO implements DAOI<Units> {

    /**
     * Metodo que busca todas as units ativas
     * @return List<Units> Lista das units
     */
    @Override
    public List<Units> searchAll() {

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where u.is_active = true"
                )
        ) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

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
     * metodo para buscar por Id (somente ativas)
     * @param id Numero unico da unit
     * @return Retorna a unit encontrada
     */
    @Override
    public Units searchById(int id) {

        Units unit = null;
        Companies company = null;
        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where u.id = ? and u.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

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
     * metodo para buscar por Id da empresa (somente ativas)
     * @param companyId O valor unico de cada empresa
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByCompanyId(int companyId) {

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where u.company_id = ? and u.is_active = true"
                )
        ) {

            pstmt.setInt(1, companyId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

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
     * metodo para buscar as units pelo nome da empresa (somente ativas)
     * @param name O nome da empresa
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByNameCompany(String name) {

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where c.name ilike ? and u.is_active = true"
                )
        ) {

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

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
     * metodo para buscar por nome da unit (somente ativas)
     * @param name O nome da unit
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByName(String name) {

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where u.name ilike ? and u.is_active = true"
                )
        ) {

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

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
     * metodo para buscar por cnpj da unit (somente ativas)
     * @param cnpj O cnpj da unit
     * @return Retorna a unit encontrada
     */
    public Units searchByCnpj(String cnpj) {

        Units unit = null;
        Companies company = null;
        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where u.cnpj = ? and u.is_active = true"
                )
        ) {

            pstmt.setString(1, cnpj);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

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
     * metodo para buscar por cnae da unit (somente ativas)
     * @param cnae O cnae (Classificação Nacional de Atividades Econômicas) da unit
     * @return List<Units> com as units encontradas
     */
    public List<Units> searchByCnae(String cnae) {

        List<Units> units = new ArrayList<>();
        Companies company = null;
        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where u.cnae = ? and u.is_active = true"
                )
        ) {

            pstmt.setString(1, cnae);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

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
     * metodo para cadastrar novas units no banco (sempre ativa na criacao)
     * @param unit A unit que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    @Override
    public boolean register(Units unit) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setBoolean(6, true);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de uma unit (sempre ativas na criacao)
     * @param units Uma lista de units
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Units> units) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

            for (int i = 0; i < units.size(); i++) {
                Units unit = units.get(i);

                pstmt.setInt(1, unit.getCompanyId());
                pstmt.setInt(2, unit.getAddressId());
                pstmt.setString(3, unit.getName());
                pstmt.setString(4, unit.getCnpj());
                pstmt.setString(5, unit.getCnae());
                pstmt.setBoolean(6, true);

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
     * metodo para desativar a unit por id (is_active = false)
     * Cascateia a desativacao para os funcionarios da unidade via EmployeesDAO.deleteByIdUnit
     * @param id Valor unico de cada unit
     * @return true se foi desativada e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        new EmployeesDAO().deleteByIdUnit(id);

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update units set is_active = false, updated_at = current_timestamp where id = ?"
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
     * metodo que desativa as units de uma empresa (para quando uma empresa for desativada)
     * Busca as units ativas da empresa e desativa uma por uma, reaproveitando delete() pra cascatear os funcionarios de cada unidade
     * @param companyId O id da empresa
     * @return true se todas foram desativadas com sucesso e false caso alguma tenha dado erro
     */
    public boolean deleteByCompanyId(int companyId) {

        List<Units> units = searchByCompanyId(companyId);
        boolean allSuccess = true;

        for (int i = 0; i < units.size(); i++) {
            if (!delete(units.get(i).getId())) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * metodo que desativa pelo nome
     * Busca as units ativas pelo nome e desativa uma por uma, reaproveitando delete() pra cascatear os funcionarios de cada unidade
     * @param name O nome da unit
     * @return true se todas foram desativadas com sucesso e false caso alguma tenha dado erro
     */
    public boolean deleteByName(String name) {

        List<Units> units = searchByName(name);
        boolean allSuccess = true;

        for (int i = 0; i < units.size(); i++) {
            if (!delete(units.get(i).getId())) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * metodo que muda os values da unit selecionada pelo id
     * @param unit Os valores da unit para ser atualizada
     * @param id O valor unico da unit que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(Units unit, int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update units set company_id = ?, " +
                                "address_id = ?, " +
                                "name = ?, " +
                                "cnpj = ?, " +
                                "cnae = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setInt(6, id);

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
    public boolean updateByName(Units unit, String name) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update units set company_id = ?, " +
                                "address_id = ?, " +
                                "name = ?, " +
                                "cnpj = ?, " +
                                "cnae = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ) {

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, unit.getCnpj());
            pstmt.setString(5, unit.getCnae());
            pstmt.setString(6, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}