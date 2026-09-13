package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompaniesDAO implements DAOI<Companies> {

    /**
     * Metodo que busca todas as empresas ativas
     * @return List<Companies> Lista das empresas
     */
    @Override
    public List<Companies> searchAll() {

        List<Companies> companies = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.*, c.address_id as addressId, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from companies c " +
                                "join addresses a on a.id = c.address_id " +
                                "where c.is_active = true"
                )
        ) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Addresses address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                companies.add(
                        new Companies(
                                rs.getInt("id"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getInt("size"),
                                rs.getDate("registration_date"),
                                rs.getString("tax_id"),
                                rs.getString("email"),
                                rs.getDate("created_at"),
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return companies;
    }

    /**
     * Metodo para buscar por ID (somente ativas)
     * @param id Numero unico da empresa
     * @return Companies encontrada ou null caso nao exista
     */
    @Override
    public Companies searchById(int id) {

        Companies company = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.*, c.address_id as addressId, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from companies c " +
                                "join addresses a on a.id = c.address_id " +
                                "where c.id = ? and c.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                Addresses address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                company = new Companies(
                        rs.getInt("id"),
                        rs.getInt("addressId"),
                        rs.getString("name"),
                        rs.getInt("size"),
                        rs.getDate("registration_date"),
                        rs.getString("tax_id"),
                        rs.getString("email"),
                        rs.getDate("created_at"),
                        address
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return company;
    }

    /**
     * Metodo para buscar por pais (somente ativas)
     * @param country Nome do pais
     * @return List<Companies> com as empresas encontradas
     */
    public List<Companies> searchByCountry(String country) {

        List<Companies> companies = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.*, c.address_id as addressId, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from companies c " +
                                "join addresses a on a.id = c.address_id " +
                                "where a.country ilike ? and c.is_active = true"
                )
        ) {

            pstmt.setString(1, country);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Addresses address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                companies.add(
                        new Companies(
                                rs.getInt("id"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getInt("size"),
                                rs.getDate("registration_date"),
                                rs.getString("tax_id"),
                                rs.getString("email"),
                                rs.getDate("created_at"),
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return companies;
    }

    /**
     * Metodo para buscar pelo nome da empresa (somente ativas)
     * @param name Nome da empresa
     * @return Companies encontrada ou null caso nao exista
     */
    public Companies searchByName(String name) {

        Companies company = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.*, c.address_id as addressId, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from companies c " +
                                "join addresses a on a.id = c.address_id " +
                                "where c.name ilike ? and c.is_active = true"
                )
        ) {

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                Addresses address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                company = new Companies(
                        rs.getInt("id"),
                        rs.getInt("addressId"),
                        rs.getString("name"),
                        rs.getInt("size"),
                        rs.getDate("registration_date"),
                        rs.getString("tax_id"),
                        rs.getString("email"),
                        rs.getDate("created_at"),
                        address
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return company;
    }

    /**
     * Metodo para buscar pela data de registro (somente ativas)
     * @param registrationDate Data de registro
     * @return List<Companies> com as empresas encontradas
     */
    public List<Companies> searchByRegistration(Date registrationDate) {

        List<Companies> companies = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.*, c.address_id as addressId, " +
                                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from companies c " +
                                "join addresses a on a.id = c.address_id " +
                                "where c.registration_date = ? and c.is_active = true"
                )
        ) {

            pstmt.setDate(1, registrationDate);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Addresses address = new Addresses(
                        rs.getInt("idAddress"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                companies.add(
                        new Companies(
                                rs.getInt("id"),
                                rs.getInt("addressId"),
                                rs.getString("name"),
                                rs.getInt("size"),
                                rs.getDate("registration_date"),
                                rs.getString("tax_id"),
                                rs.getString("email"),
                                rs.getDate("created_at"),
                                address
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return companies;
    }

    /**
     * Metodo para cadastrar novas empresas no banco
     * @param company Empresa que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    @Override
    public boolean register(Companies company) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into companies (name, size, registration_date, tax_id, email, address_id) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setInt(2, company.getSize());
            pstmt.setDate(3, company.getRegistrationDate());
            pstmt.setString(4, company.getTaxId());
            pstmt.setString(5, company.getEmail());
            pstmt.setInt(6, company.getAddressId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo para desativar a empresa por ID (is_active = false)
     * Cascateia a desativacao para as units da empresa, que por sua vez cascateiam para os funcionarios de cada unidade
     * @param id Valor unico de cada empresa
     * @return true se foi desativada e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        new UnitsDAO().deleteByCompanyId(id);

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set is_active = false, updated_at = current_timestamp where id = ?"
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
     * Metodo que desativa pelo nome
     * @param name Nome da empresa
     * @return true se foi desativada e false caso tenha dado erro
     */
    public boolean deleteByName(String name) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set is_active = false, updated_at = current_timestamp where name ilike ?"
                )
        ) {

            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que atualiza a empresa pelo ID
     * @param company Valores da empresa para atualizar
     * @param id ID da empresa que sera atualizada
     * @return true se foi atualizada e false caso tenha dado erro
     */
    @Override
    public boolean update(Companies company, int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set name = ?, " +
                                "size = ?, " +
                                "registration_date = ?, " +
                                "tax_id = ?, " +
                                "email = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setInt(2, company.getSize());
            pstmt.setDate(3, company.getRegistrationDate());
            pstmt.setString(4, company.getTaxId());
            pstmt.setString(5, company.getEmail());
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que atualiza a empresa pelo nome
     * @param company Valores da empresa para atualizar
     * @param name Nome da empresa que sera atualizada
     * @return true se foi atualizada e false caso tenha dado erro
     */
    public boolean updateByName(Companies company, String name) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set name = ?, " +
                                "size = ?, " +
                                "registration_date = ?, " +
                                "tax_id = ?, " +
                                "email = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setInt(2, company.getSize());
            pstmt.setDate(3, company.getRegistrationDate());
            pstmt.setString(4, company.getTaxId());
            pstmt.setString(5, company.getEmail());
            pstmt.setString(6, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}