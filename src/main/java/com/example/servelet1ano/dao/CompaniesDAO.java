package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompaniesDAO implements DAOI<Companies, CompaniesFilter> {

    /**
     * Metodo que busca as empresas ativas aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Companies> com as empresas encontradas
     */
    @Override
    public List<Companies> searchAll(CompaniesFilter filter) {

        List<Companies> companies = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select c.*, c.address_id as addressId, " +
                        "a.id as idAddress, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                        "a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                        "from companies c " +
                        "join addresses a on a.id = c.address_id " +
                        "where c.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and c.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and c.name ilike ?");
            parametros.add("%" + filter.getName() + "%");
        }

        if (filter.getCountry() != null && !filter.getCountry().isBlank()) {
            sql.append(" and a.country ilike ?");
            parametros.add("%" + filter.getCountry() + "%");
        }

        if (filter.getRegistrationDate() != null) {
            sql.append(" and c.registration_date = ?");
            parametros.add(filter.getRegistrationDate());
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

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