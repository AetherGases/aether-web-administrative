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

    /**
     * Valida os dados da empresa antes de gravar (cnpj, porte e, se preenchido, email)
     * @param company A empresa a validar
     * @return true se cnpj, porte e o email (quando informado) forem validos
     */
    private boolean isValid(Companies company) {

        if (company == null) {
            return false;
        }

        if (!Validators.isCnpj(company.getCnpj())) {
            return false;
        }

        if (company.getSize() == null) {
            return false;
        }

        if (company.getEmail() != null && !company.getEmail().isBlank() && !Validators.isEmail(company.getEmail())) {
            return false;
        }

        return true;
    }

    /**
     * Deixa so letras e numeros do cnpj, em maiusculo (tira pontos, barra, traco e espacos)
     * Usado antes de gravar e antes de buscar, pra o cnpj ficar sempre no mesmo formato no banco
     * @param cnpj O cnpj como foi digitado
     * @return O cnpj so com letras e numeros, ou null se vier null
     */
    private String cleanCnpj(String cnpj) {

        if (cnpj == null) {
            return null;
        }

        return cnpj.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }


    /**
     * Metodo que busca as empresas ativas aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Companies> com as empresas encontradas
     */

    @Override
    public List<Companies> searchAll(CompaniesFilter filter) {

        List<Companies> companies = new ArrayList<>();

        if (filter == null) {
            filter = new CompaniesFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select c.* " +
                        "from companies c " +
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
            parametros.add(cleanCnpj(filter.getCnpj()));
        }

        if (filter.getRegistrationDate() != null) {
            sql.append(" and c.registration_date = ?");
            parametros.add(filter.getRegistrationDate());
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return companies;
        }

        try (
                conn;
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

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.* " +
                                "from companies c " +
                                "where c.id = ? and c.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    company = new Companies(
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
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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

        if (!isValid(company)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into companies (name, trade_name, size, registration_date, cnpj, email) " +
                                "values (?, ?, ?::company_size, ?, ?, ?)"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, cleanCnpj(company.getCnpj()));
            pstmt.setString(6, company.getEmail());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }

    /**
     * Cadastra uma empresa e retorna o ID gerado pelo banco.
     *
     * Utiliza uma conexão própria quando o cadastro não faz parte
     * de uma transação externa.
     *
     * @param company empresa que será cadastrada
     * @return ID gerado ou -1 caso os dados sejam inválidos
     */
    public int registerReturningId(Companies company) {

        if (!isValid(company)) {
            return -1;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (conn == null) {
                throw new IllegalStateException(
                        "Não foi possível estabelecer conexão com o banco."
                );
            }

            return registerReturningId(company, conn);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar empresa e recuperar seu ID.",
                    e
            );
        }
    }

    /**
     * Cadastra uma empresa utilizando uma conexão compartilhada.
     *
     * Não abre nem fecha a conexão recebida, permitindo que o service
     * controle a transação e reverta o cadastro caso outra operação falhe.
     *
     * @param company empresa que será cadastrada
     * @param conn conexão pertencente à transação externa
     * @return ID gerado ou -1 caso os dados sejam inválidos
     * @throws SQLException caso ocorra um erro durante a operação SQL
     */
    public int registerReturningId(Companies company, Connection conn) {

        if (!isValid(company)) {
            return -1;
        }

        if (conn == null) {
            throw new IllegalArgumentException(
                    "A conexão com o banco não pode ser nula."
            );
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
            pstmt.setString(5, cleanCnpj(company.getCnpj()));
            pstmt.setString(6, company.getEmail());

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("id");
                }

                return -1;
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao acessar o banco de dados.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set is_active = false, updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
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

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
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
            pstmt.setString(5, cleanCnpj(company.getCnpj()));
            pstmt.setString(6, company.getEmail());
            pstmt.setInt(7, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar o banco de dados.", e);
        }
    }
}