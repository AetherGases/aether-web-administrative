package com.example.servelet1ano.dao;
import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompaniesDAO {

    /**
     * Metodo que busca todas as empresas
     *
     * @return List<Companies> Lista das empresas
     */
    public List<Companies> searchAll() {

        List<Companies> companies = new ArrayList<>();

        String sql = "select c.*, c.address_id as addressId, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from companies c " +
                "join addresses a on a.id = c.address_id";

        try (
                Connection conn = ConnectionFactory.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)
        ) {

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
     * Metodo para buscar por ID
     *
     * @param id Numero unico da empresa
     * @return Companies encontrada ou null caso nao exista
     */
    public Companies searchById(int id) {

        String sql = "select c.*, c.address_id as addressId, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from companies c " +
                "join addresses a on a.id = c.address_id " +
                "where c.id = ?";

        Companies company = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     * Metodo para buscar por pais
     *
     * @param country Nome do pais
     * @return List<Companies> com as empresas encontradas
     */
    public List<Companies> searchByCountry(String country) {

        List<Companies> companies = new ArrayList<>();

        String sql = "select c.*, c.address_id as addressId, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from companies c " +
                "join addresses a on a.id = c.address_id " +
                "where a.country = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     * Metodo para buscar pelo nome da empresa
     *
     * @param name Nome da empresa
     * @return Companies encontrada ou null caso nao exista
     */
    public Companies searchByName(String name) {

        String sql = "select c.*, c.address_id as addressId, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from companies c " +
                "join addresses a on a.id = c.address_id " +
                "where c.name = ?";

        Companies company = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     * Metodo para buscar pela data de registro
     *
     * @param registrationDate Data de registro
     * @return List<Companies> com as empresas encontradas
     */
    public List<Companies> searchByRegistration(Date registrationDate) {

        List<Companies> companies = new ArrayList<>();

        String sql = "select c.*, c.address_id as addressId, " +
                "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                "from companies c " +
                "join addresses a on a.id = c.address_id " +
                "where c.registration_date = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     *
     * @param company Empresa que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    public boolean register(Companies company) {

        String sql = "insert into companies (name, size, registration_date, tax_id, email, address_id) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     * Metodo para deletar a empresa por ID
     *
     * @param id Valor unico de cada empresa
     * @return true se foi deletada e false caso tenha dado erro
     */
    public boolean deleteById(int id) {

        String sql = "delete from companies where id = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que deleta pelo nome
     *
     * @param name Nome da empresa
     * @return true se foi deletada e false caso tenha dado erro
     */
    public boolean deleteByName(String name) {

        String sql = "delete from companies where name like ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     *
     * @param company Valores da empresa para atualizar
     * @param id ID da empresa que sera atualizada
     * @return true se foi atualizada e false caso tenha dado erro
     */
    public boolean updateById(Companies company, int id) {

        String sql = "update companies set name = ?, " +
                "size = ?, " +
                "registration_date = ?, " +
                "tax_id = ?, " +
                "email = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     *
     * @param company Valores da empresa para atualizar
     * @param name Nome da empresa que sera atualizada
     * @return true se foi atualizada e false caso tenha dado erro
     */
    public boolean updateByName(Companies company, String name) {

        String sql = "update companies set name = ?, " +
                "size = ?, " +
                "registration_date = ?, " +
                "tax_id = ?, " +
                "email = ?, " +
                "updated_at = current_timestamp " +
                "where name like ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
