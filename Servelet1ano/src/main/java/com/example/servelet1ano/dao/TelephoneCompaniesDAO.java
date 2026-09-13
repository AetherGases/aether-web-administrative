package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.TelephoneCompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.TelephoneCompanies;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TelephoneCompaniesDAO implements DAOI<TelephoneCompanies, TelephoneCompaniesFilter> {

    /**
     * Metodo que busca os telefones ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<TelephoneCompanies> com os telefones encontrados
     */
    @Override
    public List<TelephoneCompanies> searchAll(TelephoneCompaniesFilter filter){

        List<TelephoneCompanies> telephones = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select tc.*, tc.company_id as companyId, " +
                        "c.id as idCompany, c.name as companyName " +
                        "from telephone_companies tc " +
                        "join companies c on c.id = tc.company_id " +
                        "where tc.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and tc.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and tc.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getCompanyName() != null && !filter.getCompanyName().isBlank()) {
            sql.append(" and c.name ilike ?");
            parametros.add("%" + filter.getCompanyName() + "%");
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

                Companies company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                telephones.add(
                        new TelephoneCompanies(
                                rs.getString("telephone"),
                                rs.getInt("companyId"),
                                rs.getInt("id"),
                                company
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return telephones;
    }

    /**
     * Metodo que busca o telefone por id (somente ativos)
     * @param id O identificador do telefone para o buscar
     * @return O telefone encontrado
     */
    @Override
    public TelephoneCompanies searchById(int id){

        TelephoneCompanies telephoneCompany = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select tc.*, tc.company_id as companyId, " +
                                "c.id as idCompany, c.name as companyName " +
                                "from telephone_companies tc " +
                                "join companies c on c.id = tc.company_id " +
                                "where tc.id = ? and tc.is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){

                Companies company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                telephoneCompany = new TelephoneCompanies(
                        rs.getString("telephone"),
                        rs.getInt("companyId"),
                        rs.getInt("id"),
                        company
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return telephoneCompany;
    }

    /**
     * metodo para cadastrar novos telefones no banco
     * @param telephoneCompany O telefone
     * @return true se foi cadastrado e false caso tenha dado erro
     */
    @Override
    public boolean register(TelephoneCompanies telephoneCompany){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into telephone_companies (telephone, company_id) " +
                                "values (?, ?)"
                )
        ){

            pstmt.setString(1, telephoneCompany.getTelephone());
            pstmt.setInt(2, telephoneCompany.getCompanyId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa o telefone por id (is_active = false)
     * @param id O id unico do telefone
     * @return true se foi desativado e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update telephone_companies set is_active = false, updated_at = current_timestamp where id = ?"
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
     * metodo que desativa os telefones de uma empresa (para quando uma empresa for desativada)
     * @param companyId O id da empresa
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByCompanyId(int companyId){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update telephone_companies set is_active = false, updated_at = current_timestamp where company_id = ?"
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
     * metodo que muda os values do telefone selecionado pelo id
     * @param telephoneCompany O telefone com os novos valores
     * @param id O id para buscar o telefone
     * @return true se deu certo e false caso tenha algum erro
     */
    @Override
    public boolean update(TelephoneCompanies telephoneCompany, int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update telephone_companies set telephone = ?, " +
                                "company_id = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setString(1, telephoneCompany.getTelephone());
            pstmt.setInt(2, telephoneCompany.getCompanyId());
            pstmt.setInt(3, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}