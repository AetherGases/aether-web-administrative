
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.TelephoneCompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.TelephoneCompanies;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TelephoneCompaniesDAO
        implements DAOI<TelephoneCompanies, TelephoneCompaniesFilter> {

    private Connection requireConnection() {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    private static final String SELECT_TELEPHONES =
            "SELECT tc.*, " +
                    "tc.company_id AS companyId, " +
                    "c.id AS idCompany, " +
                    "c.name AS companyName " +
                    "FROM telephone_companies tc " +
                    "JOIN companies c ON c.id = tc.company_id ";

    private TelephoneCompanies mapTelephone(ResultSet rs)
            throws SQLException {

        Companies company = new Companies(
                rs.getInt("idCompany"),
                rs.getString("companyName")
        );

        TelephoneCompanies telephoneCompany = new TelephoneCompanies(
                rs.getString("telephone"),
                rs.getInt("companyId"),
                rs.getInt("id"),
                company
        );

        telephoneCompany.setCreatedAt(
                rs.getTimestamp("created_at")
        );

        telephoneCompany.setUpdatedAt(
                rs.getTimestamp("updated_at")
        );

        return telephoneCompany;
    }

    /*
     * Busca telefones ativos aplicando os filtros informados.
     */
    @Override
    public List<TelephoneCompanies> searchAll(
            TelephoneCompaniesFilter filter) {

        List<TelephoneCompanies> telephones = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                SELECT_TELEPHONES + "WHERE tc.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter != null) {
            if (filter.getId() != null) {
                sql.append(" AND tc.id = ?");
                parametros.add(filter.getId());
            }

            if (filter.getCompanyId() != null) {
                sql.append(" AND tc.company_id = ?");
                parametros.add(filter.getCompanyId());
            }

            if (filter.getCompanyName() != null
                    && !filter.getCompanyName().isBlank()) {

                sql.append(" AND c.name ILIKE ?");
                parametros.add(
                        "%" + filter.getCompanyName() + "%"
                );
            }
        }

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt =
                        conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    telephones.add(mapTelephone(rs));
                }
            }

            return telephones;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar telefones das empresas.", e
            );
        }
    }

    /*
     * Busca um telefone ativo pelo ID.
     */
    @Override
    public TelephoneCompanies searchById(int id) {

        String sql = SELECT_TELEPHONES
                + "WHERE tc.id = ? AND tc.is_active = true";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapTelephone(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar telefone pelo ID: " + id, e
            );
        }
    }

    /*
     * Cadastra um telefone.
     */
    @Override
    public boolean register(TelephoneCompanies telephoneCompany) {

        if (telephoneCompany == null
                || !Validators.isMobilePhone(
                telephoneCompany.getTelephone())) {
            return false;
        }

        String sql =
                "INSERT INTO telephone_companies " +
                        "(telephone, company_id) VALUES (?, ?)";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(
                    1, telephoneCompany.getTelephone()
            );

            pstmt.setInt(
                    2, telephoneCompany.getCompanyId()
            );

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar telefone.", e
            );
        }
    }

    /*
     * Desativa um telefone pelo ID.
     */
    @Override
    public boolean delete(int id) {

        String sql =
                "UPDATE telephone_companies " +
                        "SET is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ? AND is_active = true";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar telefone pelo ID: " + id, e
            );
        }
    }

    /*
     * Desativa os telefones ativos de uma empresa.
     */
    public boolean deleteByCompanyId(int companyId) {

        String sql =
                "UPDATE telephone_companies " +
                        "SET is_active = false, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE company_id = ? AND is_active = true";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar telefones da empresa: "
                            + companyId, e
            );
        }
    }

    /*
     * Atualiza os dados de um telefone.
     */
    @Override
    public boolean update(
            TelephoneCompanies telephoneCompany, int id) {

        if (telephoneCompany == null
                || !Validators.isMobilePhone(
                telephoneCompany.getTelephone())) {
            return false;
        }

        String sql =
                "UPDATE telephone_companies SET " +
                        "telephone = ?, " +
                        "company_id = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(
                    1, telephoneCompany.getTelephone()
            );

            pstmt.setInt(
                    2, telephoneCompany.getCompanyId()
            );

            pstmt.setInt(3, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar telefone pelo ID: " + id, e
            );
        }
    }
}
