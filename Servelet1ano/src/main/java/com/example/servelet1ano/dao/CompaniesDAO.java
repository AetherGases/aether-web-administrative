
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.CompanySize;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompaniesDAO implements DAOI<Companies, CompaniesFilter> {

    private Connection getConnection() {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    /**
     * Valida o CNPJ e, quando informado, o e-mail.
     */
    private boolean isValid(Companies company) {
        if (company == null) {
            return false;
        }

        if (!Validators.isCnpj(company.getCnpj())) {
            return false;
        }

        if (company.getEmail() != null
                && !company.getEmail().isBlank()
                && !Validators.isEmail(company.getEmail())) {
            return false;
        }

        return true;
    }

    /**
     * Busca empresas ativas aplicando os filtros informados.
     */
    @Override
    public List<Companies> searchAll(CompaniesFilter filter) {

        List<Companies> companies = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.* " +
                        "FROM companies c " +
                        "WHERE c.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter != null) {
            if (filter.getId() != null) {
                sql.append(" AND c.id = ?");
                parametros.add(filter.getId());
            }

            if (filter.getName() != null
                    && !filter.getName().isBlank()) {
                sql.append(" AND c.name ILIKE ?");
                parametros.add("%" + filter.getName().trim() + "%");
            }

            if (filter.getCnpj() != null
                    && !filter.getCnpj().isBlank()) {
                sql.append(" AND c.cnpj = ?");
                parametros.add(filter.getCnpj());
            }

            if (filter.getRegistrationDate() != null) {
                sql.append(" AND c.registration_date = ?");
                parametros.add(filter.getRegistrationDate());
            }
        }

        sql.append(" ORDER BY c.name ASC");

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Companies company = new Companies(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("trade_name"),
                            rs.getString("cnpj"),
                            CompanySize.fromValor(rs.getString("size")),
                            rs.getDate("registration_date"),
                            rs.getString("email"),
                            rs.getDate("created_at"),
                            rs.getBoolean("is_active")
                    );

                    company.setUpdatedAt(rs.getDate("updated_at"));
                    companies.add(company);
                }
            }

            return companies;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar empresas.",
                    e
            );
        }
    }

    /**
     * Busca uma empresa ativa pelo ID.
     */
    @Override
    public Companies searchById(int id) {

        String sql =
                "SELECT c.* " +
                        "FROM companies c " +
                        "WHERE c.id = ? AND c.is_active = true";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Companies company = new Companies(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("trade_name"),
                            rs.getString("cnpj"),
                            CompanySize.fromValor(rs.getString("size")),
                            rs.getDate("registration_date"),
                            rs.getString("email"),
                            rs.getDate("created_at"),
                            rs.getBoolean("is_active")
                    );

                    company.setUpdatedAt(rs.getDate("updated_at"));

                    return company;
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar empresa pelo ID.",
                    e
            );
        }
    }

    /**
     * Cadastra uma nova empresa.
     */
    @Override
    public boolean register(Companies company) {

        if (!isValid(company)) {
            return false;
        }

        String sql =
                "INSERT INTO companies " +
                        "(name, trade_name, size, registration_date, cnpj, email) " +
                        "VALUES (?, ?, ?::company_size, ?, ?, ?)";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, company.getCnpj());
            pstmt.setString(6, company.getEmail());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar empresa.",
                    e
            );
        }
    }

    /**
     * Cadastra uma empresa e retorna o ID gerado.
     * Retorna -1 quando os dados não passam na validação.
     */
    public int registerReturningId(
            Companies company,
            Connection conn
    ) {
        if (!isValid(company)) {
            return -1;
        }

        String sql =
                "INSERT INTO companies " +
                        "(name, trade_name, size, registration_date, cnpj, email) " +
                        "VALUES (?, ?, ?::company_size, ?, ?, ?) " +
                        "RETURNING id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, company.getCnpj());
            pstmt.setString(6, company.getEmail());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

            throw new IllegalStateException(
                    "Nenhum ID foi retornado ao cadastrar a empresa."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar empresa.",
                    e
            );
        }
    }

    /**
     * Desativa uma empresa e seus registros relacionados.
     */
    @Override
    public boolean delete(int id) {

        new UnitsDAO().deleteByCompanyId(id);
        new SectorsDAO().deleteByCompanyId(id);
        new TelephoneCompaniesDAO().deleteByCompanyId(id);
        new PermissionGroupsDAO().deleteByCompanyId(id);
        new SubscriptionsDAO().deleteByCompanyId(id);

        String sql =
                "UPDATE companies " +
                        "SET is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar empresa pelo ID.",
                    e
            );
        }
    }

    /**
     * Desativa empresas pelo nome.
     */
    public boolean deleteByName(String name) {

        String sql =
                "UPDATE companies " +
                        "SET is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ?";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar empresa pelo nome.",
                    e
            );
        }
    }

    /**
     * Atualiza uma empresa pelo ID.
     */
    @Override
    public boolean update(Companies company, int id) {

        if (!isValid(company)) {
            return false;
        }

        String sql =
                "UPDATE companies SET " +
                        "name = ?, " +
                        "trade_name = ?, " +
                        "size = ?::company_size, " +
                        "registration_date = ?, " +
                        "cnpj = ?, " +
                        "email = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
            throw new RuntimeException(
                    "Erro ao atualizar empresa pelo ID.",
                    e
            );
        }
    }

    /**
     * Atualiza uma empresa pelo nome atual.
     */
    public boolean updateByName(Companies company, String name) {

        if (!isValid(company)) {
            return false;
        }

        String sql =
                "UPDATE companies SET " +
                        "name = ?, " +
                        "trade_name = ?, " +
                        "size = ?::company_size, " +
                        "registration_date = ?, " +
                        "cnpj = ?, " +
                        "email = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE name ILIKE ?";

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
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
            throw new RuntimeException(
                    "Erro ao atualizar empresa pelo nome.",
                    e
            );
        }
    }
}
