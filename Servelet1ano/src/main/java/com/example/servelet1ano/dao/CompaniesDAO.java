package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.CompanySize;
import com.example.servelet1ano.util.SqlUtil;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompaniesDAO implements DAOI<Companies, CompaniesFilter> {

    /**
     * Valida os dados da empresa antes de gravar (nome, cnpj, porte e, se preenchido, email)
     * @param company A empresa a validar
     * @return true se nome, cnpj, porte e o email (quando informado) forem validos e couberem na tabela
     */
    private boolean isValid(Companies company) {

        if (company == null) {
            return false;
        }

        if (company.getName() == null || company.getName().isBlank() || company.getName().trim().length() > 255) {
            return false;
        }

        if (company.getTradeName() != null && company.getTradeName().trim().length() > 150) {
            return false;
        }

        if (!Validators.isCnpj(company.getCnpj())) {
            return false;
        }

        if (company.getSize() == null) {
            return false;
        }

        if (company.getEmail() != null && !company.getEmail().isBlank()
                && (company.getEmail().trim().length() > 255 || !Validators.isEmail(company.getEmail()))) {
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
     * Deixa o email sem espacos nas pontas e em minusculo (ou null se estiver vazio)
     * @param company A empresa
     * @return O email normalizado, ou null
     */
    private String emailOf(Companies company) {

        if (company.getEmail() == null || company.getEmail().isBlank()) {
            return null;
        }

        return company.getEmail().trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Devolve o nome fantasia sem espacos nas pontas (ou null se estiver vazio)
     * @param company A empresa
     * @return O nome fantasia, ou null
     */
    private String tradeNameOf(Companies company) {

        if (company.getTradeName() == null || company.getTradeName().isBlank()) {
            return null;
        }

        return company.getTradeName().trim();
    }

    /**
     * Confere se o erro do banco foi de valor duplicado (unique violation, SQLState 23505), ex: cnpj ja cadastrado
     * @param e O erro do banco
     * @return true se foi duplicidade
     */
    private boolean isUniqueViolation(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

    /**
     * Desfaz a transacao sem esconder o erro original
     */
    private void rollbackQuietly(Connection conn, Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            cause.addSuppressed(rollbackError);
        }
    }

    /**
     * Monta uma empresa a partir da linha atual do ResultSet
     */
    private Companies mapCompany(ResultSet rs) throws SQLException {

        Companies company = new Companies(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("trade_name"),
                rs.getString("cnpj"),
                CompanySize.fromValor(rs.getString("size")),
                rs.getDate("registration_date"),
                rs.getString("email"),
                rs.getTimestamp("created_at"),
                rs.getBoolean("is_active")
        );

        company.setUpdatedAt(rs.getTimestamp("updated_at"));

        return company;
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
            parametros.add("%" + SqlUtil.escapeLike(filter.getName().trim()) + "%");
        }

        if (filter.getCnpj() != null && !filter.getCnpj().isBlank()) {
            sql.append(" and c.cnpj = ?");
            parametros.add(cleanCnpj(filter.getCnpj()));
        }

        if (filter.getRegistrationDate() != null) {
            sql.append(" and c.registration_date = ?");
            parametros.add(filter.getRegistrationDate());
        }

        sql.append(" order by c.id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    companies.add(mapCompany(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar as empresas no banco de dados.", e);
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

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.* " +
                                "from companies c " +
                                "where c.id = ? and c.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return mapCompany(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar a empresa de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * Metodo para cadastrar novas empresas no banco
     * @param company Empresa que sera cadastrada
     * @return true se foi registrada e false se os dados forem invalidos ou o cnpj ja estiver cadastrado
     */
    @Override
    public boolean register(Companies company) {
        return registerReturningId(company) > 0;
    }

    /**
     * metodo para cadastrar uma nova empresa e devolver o id gerado
     * Usado quando a empresa precisa ser vinculada a uma subscription
     * Usa o returning do PostgreSQL em vez de RETURN_GENERATED_KEYS
     * @param company A empresa que sera cadastrada
     * @return O id gerado, ou -1 se os dados forem invalidos ou o cnpj ja estiver cadastrado
     */
    public int registerReturningId(Companies company) {

        if (!isValid(company)) {
            return -1;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            return registerReturningId(company, conn);

        } catch (SQLException e) {

            if (isUniqueViolation(e)) {
                return -1;
            }

            throw new RuntimeException("Erro ao cadastrar a empresa no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar uma nova empresa usando uma conexao ja aberta e devolver o id gerado
     * (permite que quem chamou controle a transacao, ex: cadastrar a empresa e a assinatura juntas)
     * Se o cnpj ja estiver cadastrado, lanca SQLException (SQLState 23505), para a transacao de quem chamou ser desfeita
     * @param company A empresa que sera cadastrada
     * @param conn A conexao (nao e fechada aqui)
     * @return O id gerado, ou -1 se os dados forem invalidos
     * @throws SQLException se der erro no banco
     */
    public int registerReturningId(Companies company, Connection conn) throws SQLException {

        if (!isValid(company)) {
            return -1;
        }

        String sql = "INSERT INTO companies " +
                "(name, trade_name, size, registration_date, cnpj, email) " +
                "VALUES (?, ?, ?::company_size, ?, ?, ?) " +
                "RETURNING id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, company.getName().trim());
            pstmt.setString(2, tradeNameOf(company));
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, cleanCnpj(company.getCnpj()));
            pstmt.setString(6, emailOf(company));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return -1;
    }

    /**
     * Metodo para desativar a empresa por ID (is_active = false)
     * Cascateia a desativacao para as units da empresa (que por sua vez cascateiam para setores e funcionarios),
     * e tambem para funcionarios, setores, telefones, grupos de permissao e assinaturas da empresa
     * Tudo acontece na mesma transacao: ou a empresa e tudo o que depende dela sao desativados juntos, ou nada muda
     * @param id Valor unico de cada empresa
     * @return true se foi desativada e false se a empresa nao existe ou ja estava desativada
     */
    @Override
    public boolean delete(int id) {

        try (Connection conn = ConnectionFactory.connect()) {

            // desliga o salvamento automatico pra tudo ser desativado junto (ou nada)
            conn.setAutoCommit(false);

            try {

                boolean deactivated;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set is_active = false, updated_at = current_timestamp " +
                                "where id = ? and is_active = true"
                )) {
                    pstmt.setInt(1, id);
                    deactivated = pstmt.executeUpdate() > 0;
                }

                if (!deactivated) {
                    conn.rollback();
                    return false;
                }

                new UnitsDAO().deleteByCompanyId(id, conn);
                new EmployeesDAO().deleteByIdCompany(id, conn);
                new SectorsDAO().deleteByCompanyId(id, conn);
                new TelephoneCompaniesDAO().deleteByCompanyId(id, conn);
                new PermissionGroupsDAO().deleteByCompanyId(id, conn);
                new SubscriptionsDAO().deleteByCompanyId(id, conn);

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar a empresa de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * Metodo que atualiza a empresa pelo ID
     * @param company Valores da empresa para atualizar
     * @param id ID da empresa que sera atualizada
     * @return true se foi atualizada e false se os dados forem invalidos, o cnpj ja for de outra empresa ou a empresa nao existir/estiver desativada
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
                                "where id = ? and is_active = true"
                )
        ) {

            pstmt.setString(1, company.getName().trim());
            pstmt.setString(2, tradeNameOf(company));
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, cleanCnpj(company.getCnpj()));
            pstmt.setString(6, emailOf(company));
            pstmt.setInt(7, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isUniqueViolation(e)) {
                return false;
            }

            throw new RuntimeException("Erro ao atualizar a empresa de id " + id + " no banco de dados.", e);
        }
    }

}