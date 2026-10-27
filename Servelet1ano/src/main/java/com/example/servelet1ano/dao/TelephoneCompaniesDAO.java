package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.TelephoneCompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.TelephoneCompanies;
import com.example.servelet1ano.util.SqlUtil;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TelephoneCompaniesDAO implements DAOI<TelephoneCompanies, TelephoneCompaniesFilter> {

    /**
     * Select base usado na busca geral e na busca por id
     * A juncao e left join pra nao sumir um telefone que esteja sem empresa
     */
    private static final String SELECT_TELEPHONES =
            "select tc.*, tc.company_id as companyId, " +
                    "c.id as idCompany, c.name as companyName " +
                    "from telephone_companies tc " +
                    "left join companies c on c.id = tc.company_id ";

    /**
     * Metodo que busca os telefones ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * @param filter Objeto com os campos opcionais para filtrar a busca (se for null, busca todos)
     * @return List<TelephoneCompanies> com os telefones encontrados
     */
    @Override
    public List<TelephoneCompanies> searchAll(TelephoneCompaniesFilter filter){

        if (filter == null) {
            filter = new TelephoneCompaniesFilter();
        }

        List<TelephoneCompanies> telephones = new ArrayList<>();

        StringBuilder sql = new StringBuilder(SELECT_TELEPHONES + "where tc.is_active = true");

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
            parametros.add("%" + SqlUtil.escapeLike(filter.getCompanyName().trim()) + "%");
        }

        sql.append(" order by tc.id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()){
                    telephones.add(mapTelephone(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os telefones das empresas no banco de dados.", e);
        }

        return telephones;
    }

    /**
     * Metodo que busca o telefone por id (somente ativos)
     * @param id O identificador do telefone para o buscar
     * @return O telefone encontrado, ou null se nao existir ou estiver desativado
     */
    @Override
    public TelephoneCompanies searchById(int id){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        SELECT_TELEPHONES + "where tc.id = ? and tc.is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()){
                    return mapTelephone(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o telefone de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar novos telefones no banco
     * Aceita celular ou fixo, com DDD, com ou sem pontuacao e com ou sem +55
     * O telefone e gravado so com DDD + numero
     * @param telephoneCompany O telefone
     * @return true se foi cadastrado e false se o telefone for invalido ou a empresa nao existir
     */
    @Override
    public boolean register(TelephoneCompanies telephoneCompany){

        if (telephoneCompany == null || !Validators.isPhone(telephoneCompany.getTelephone())) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()){

            if (!companyExists(conn, telephoneCompany.getCompanyId())) {
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "insert into telephone_companies (telephone, company_id) " +
                            "values (?, ?)"
            )) {

                pstmt.setString(1, Validators.normalizePhone(telephoneCompany.getTelephone()));
                pstmt.setInt(2, telephoneCompany.getCompanyId());

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar o telefone da empresa no banco de dados.", e);
        }
    }

    /**
     * metodo que desativa o telefone por id (is_active = false)
     * @param id O id unico do telefone
     * @return true se foi desativado e false se o telefone nao existe ou ja estava desativado
     */
    @Override
    public boolean delete(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update telephone_companies set is_active = false, updated_at = current_timestamp " +
                                "where id = ? and is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar o telefone de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que desativa os telefones de uma empresa (para quando uma empresa for desativada)
     * @param companyId O id da empresa
     * @return true se algum telefone foi desativado e false se a empresa nao tinha telefones ativos
     */
    public boolean deleteByCompanyId(int companyId){

        try(Connection conn = ConnectionFactory.connect()) {

            return deleteByCompanyId(companyId, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar os telefones da empresa de id " + companyId + ".", e);
        }
    }

    /**
     * metodo que desativa os telefones de uma empresa usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao, ex: desativar a empresa e tudo o que depende dela juntos)
     * @param companyId O id da empresa
     * @param conn A conexao (nao e fechada aqui)
     * @return true se algum telefone foi desativado e false se a empresa nao tinha telefones ativos
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByCompanyId(int companyId, Connection conn) throws SQLException {

        try(PreparedStatement pstmt = conn.prepareStatement(
                "update telephone_companies set is_active = false, updated_at = current_timestamp " +
                        "where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que muda os values do telefone selecionado pelo id
     * @param telephoneCompany O telefone com os novos valores
     * @param id O id para buscar o telefone
     * @return true se deu certo e false se o telefone for invalido, a empresa nao existir ou o telefone nao existir/estiver desativado
     */
    @Override
    public boolean update(TelephoneCompanies telephoneCompany, int id){

        if (telephoneCompany == null || !Validators.isPhone(telephoneCompany.getTelephone())) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!companyExists(conn, telephoneCompany.getCompanyId())) {
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "update telephone_companies set telephone = ?, " +
                            "company_id = ?, " +
                            "updated_at = current_timestamp " +
                            "where id = ? and is_active = true"
            )) {

                pstmt.setString(1, Validators.normalizePhone(telephoneCompany.getTelephone()));
                pstmt.setInt(2, telephoneCompany.getCompanyId());
                pstmt.setInt(3, id);

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o telefone de id " + id + " no banco de dados.", e);
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Monta um telefone (com a empresa) a partir da linha atual do ResultSet
     * Como a juncao e left join, a empresa fica null se o telefone nao tiver vinculo
     */
    private TelephoneCompanies mapTelephone(ResultSet rs) throws SQLException {

        Companies company = null;

        int idCompany = rs.getInt("idCompany");
        if (!rs.wasNull()) {
            company = new Companies(idCompany, rs.getString("companyName"));
        }

        TelephoneCompanies telephoneCompany = new TelephoneCompanies(
                rs.getString("telephone"),
                rs.getInt("companyId"),
                rs.getInt("id"),
                company
        );

        telephoneCompany.setCreatedAt(rs.getTimestamp("created_at"));
        telephoneCompany.setUpdatedAt(rs.getTimestamp("updated_at"));

        return telephoneCompany;
    }

    /**
     * Confere se a empresa existe e esta ativa
     */
    private boolean companyExists(Connection conn, int companyId) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select 1 from companies where id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

}