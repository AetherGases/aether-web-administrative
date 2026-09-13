package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.TelephoneCompanies;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TelephoneCompaniesDAO {

    /**
     * Metodo que busca todos os telefones
     *
     * @return List<TelephoneCompanies> Lista dos telefones
     */
    public List<TelephoneCompanies> searchAll(){

        List<TelephoneCompanies> telephones = new ArrayList<>();
        Companies company = null;

        String sql = "select tc.*, tc.company_id as companyId, " +
                "c.id as idCompany, c.name as companyName " +
                "from telephone_companies tc " +
                "join companies c on c.id = tc.company_id";

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

        finally {
            return telephones;
        }
    }

    /**
     * Metodo que busca o telefone por id
     * @param id O identificador do telefone para o buscar
     * @return O telefone encontrado
     */
    public TelephoneCompanies searchById(int id){
        String sql = "select tc.*, tc.company_id as companyId, " +
                "c.id as idCompany, c.name as companyName " +
                "from telephone_companies tc " +
                "join companies c on c.id = tc.company_id " +
                "where tc.id = ?";

        TelephoneCompanies telephoneCompany = null;
        Companies company = null;

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

                telephoneCompany = new TelephoneCompanies(
                        rs.getString("telephone"),
                        rs.getInt("companyId"),
                        rs.getInt("id"),
                        company
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return telephoneCompany;
        }
    }


    /**
     * metodo para buscar os telefones vinculados a empresa
     * @param companyId O id unico da empresa a qual o telefone esta vinculado (pode retornar mais de um)
     * @return List<TelephoneCompanies> com os telefones encontrados
     */
    public List<TelephoneCompanies> searchByCompanyId(int companyId){
        String sql = "select tc.*, tc.company_id as companyId, " +
                "c.id as idCompany, c.name as companyName " +
                "from telephone_companies tc " +
                "join companies c on c.id = tc.company_id " +
                "where tc.company_id = ?";

        List<TelephoneCompanies> telephones = new ArrayList<>();
        Companies company = null;

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
        } finally {
            return telephones;
        }
    }


    /**
     * metodo para buscar os telefones pelo nome da empresa
     * @param name O nome da empresa
     * @return List<TelephoneCompanies> com os telefones encontrados
     */
    public List<TelephoneCompanies> searchByNameCompany(String name){
        String sql = "select tc.*, tc.company_id as companyId, " +
                "c.id as idCompany, c.name as companyName " +
                "from telephone_companies tc " +
                "join companies c on c.id = tc.company_id " +
                "where c.name = ?";

        List<TelephoneCompanies> telephones = new ArrayList<>();
        Companies company = null;

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
        } finally {
            return telephones;
        }
    }


    /**
     * metodo para cadastrar novos telefones no banco
     * @param telephoneCompany O telefone
     * @return true se foi cadastrado e false caso tenha dado erro
     */
    public boolean register(TelephoneCompanies telephoneCompany){

        String sql = "insert into telephone_companies (telephone, company_id) " +
                "values (?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
     * metodo que deleta por id
     * @param id O id unico do telefone
     * @return true se deu certo e false caso tenha algum erro
     */
    public boolean deleteById(int id){
        String sql = "delete from telephone_companies where id = ?";

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
     * @param companyId O id da empresa
     * @return true se deu certo e false caso tenha algum erro
     */
    public boolean deleteByCompanyId(int companyId){
        String sql = "delete from telephone_companies where company_id = ?";

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
     * metodo que muda os values do telefone selecionado pelo id
     * @param telephoneCompany O telefone com os novos valores
     * @param id O id para buscar o telefone
     * @return true se deu certo e false caso tenha algum erro
     */
    public boolean updateById(TelephoneCompanies telephoneCompany, int id){
        String sql = "update telephone_companies set telephone = ?, " +
                "company_id = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

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
