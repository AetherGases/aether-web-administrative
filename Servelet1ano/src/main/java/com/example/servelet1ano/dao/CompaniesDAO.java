package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.CompanySize;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompaniesDAO implements DAOI<Companies, CompaniesFilter> {

    /**
     * Valida os dados da empresa antes de gravar (cnpj e, se preenchido, email)
     * @param company A empresa a validar
     * @return true se cnpj for valido e o email (quando informado) tambem
     */
    private boolean isValid(Companies company) {
        if (!Validators.isCnpj(company.getCnpj())) {
            return false;
        }
        if (company.getEmail() != null && !company.getEmail().isBlank() && !Validators.isEmail(company.getEmail())) {
            return false;
        }
        return true;
    }


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
                        "a.id as idAddress, a.zip_code as addressZipCode, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                        "a.neighborhood as addressNeighborhood, a.city as addressCity, a.state as addressState, a.country as addressCountry " +
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

        if (filter.getCnpj() != null && !filter.getCnpj().isBlank()) {
            sql.append(" and c.cnpj = ?");
            parametros.add(filter.getCnpj());
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
                        rs.getString("addressZipCode"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressNeighborhood"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                Companies company = new Companies(
                        rs.getInt("id"),
                        rs.getInt("addressId"),
                        rs.getString("name"),
                        rs.getString("trade_name"),
                        rs.getString("cnpj"),
                        CompanySize.fromValor(rs.getString("size")),
                        rs.getDate("registration_date"),
                        rs.getString("email"),
                        rs.getDate("created_at"),
                        rs.getBoolean("is_active"),
                        address
                );

                company.setUpdatedAt(rs.getDate("updated_at"));

                companies.add(company);

                company.setTradeName(rs.getString("trade_name"));
                company.setUpdatedAt(rs.getDate("updated_at"));

                companies.add(company);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return companies;
        }
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
                                "a.id as idAddress, a.zip_code as addressZipCode, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.neighborhood as addressNeighborhood, a.city as addressCity, a.state as addressState, a.country as addressCountry " +
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
                        rs.getString("addressZipCode"),
                        rs.getString("addressStreet"),
                        rs.getString("addressNumber"),
                        rs.getString("addressComplement"),
                        rs.getString("addressNeighborhood"),
                        rs.getString("addressCity"),
                        rs.getString("addressState"),
                        rs.getString("addressCountry")
                );

                company = new Companies(
                        rs.getInt("id"),
                        rs.getInt("addressId"),
                        rs.getString("name"),
                        rs.getString("trade_name"),
                        rs.getString("cnpj"),
                        CompanySize.fromValor(rs.getString("size")),
                        rs.getDate("registration_date"),
                        rs.getString("email"),
                        rs.getDate("created_at"),
                        rs.getBoolean("is_active"),
                        address
                );

                company.setUpdatedAt(rs.getDate("updated_at"));

                company.setTradeName(rs.getString("trade_name"));
                company.setUpdatedAt(rs.getDate("updated_at"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return company;
        }
    }

    /**
     * Metodo para cadastrar novas empresas no banco
     * @param company Empresa que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    @Override
    public boolean register(Companies company) {

        if (!isValid(company)) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into companies (name, trade_name, size, registration_date, cnpj, email, address_id) " +
                                "values (?, ?, ?::company_size, ?, ?, ?, ?)"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, company.getCnpj());
            pstmt.setString(6, company.getEmail());
            pstmt.setInt(7, company.getAddressId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo para desativar a empresa por ID (is_active = false)
     * Cascateia a desativacao para as units da empresa (que por sua vez cascateiam para setores e funcionarios),
     * e tambem para setores, telefones, grupos de permissao e assinaturas da empresa
     * @param id Valor unico de cada empresa
     * @return true se foi desativada e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        new UnitsDAO().deleteByCompanyId(id);
        new SectorsDAO().deleteByCompanyId(id);
        new TelephoneCompaniesDAO().deleteByCompanyId(id);
        new PermissionGroupsDAO().deleteByCompanyId(id);
        new SubscriptionsDAO().deleteByCompanyId(id);

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

        if (!isValid(company)) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set name = ?, " +
                                "trade_name = ?, " +
                                "size = ?::company_size, " +
                                "registration_date = ?, " +
                                "cnpj = ?, " +
                                "email = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, company.getCnpj());
            pstmt.setString(6, company.getEmail());
            pstmt.setInt(7, id);

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

        if (!isValid(company)) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set name = ?, " +
                                "trade_name = ?, " +
                                "size = ?::company_size, " +
                                "registration_date = ?, " +
                                "cnpj = ?, " +
                                "email = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, company.getCnpj());
            pstmt.setString(6, company.getEmail());
            pstmt.setString(7, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}