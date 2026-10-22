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
     * Abre a conexao com o banco
     * @return A conexao aberta
     * @throws IllegalStateException se nao for possivel conectar
     */
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
     * Valida os dados da empresa antes de gravar (nome, cnpj, porte e, se preenchido, email)
     * @param company A empresa a validar
     * @return true se nome, cnpj, porte e o email (quando informado) forem validos
     */
    private boolean isValid(Companies company) {

        if (company == null) {
            return false;
        }

        if (company.getName() == null || company.getName().isBlank()) {
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
     * O resultado vem ordenado pelo nome da empresa
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
            parametros.add("%" + filter.getName().trim() + "%");
        }

        if (filter.getCnpj() != null && !filter.getCnpj().isBlank()) {
            sql.append(" and c.cnpj = ?");
            parametros.add(cleanCnpj(filter.getCnpj()));
        }

        if (filter.getRegistrationDate() != null) {
            sql.append(" and c.registration_date = ?");
            parametros.add(filter.getRegistrationDate());
        }

        sql.append(" order by c.name asc");

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
     * Metodo para buscar por ID (somente ativas)
     * @param id Numero unico da empresa
     * @return Companies encontrada ou null caso nao exista
     */
    @Override
    public Companies searchById(int id) {

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select c.* " +
                                "from companies c " +
                                "where c.id = ? and c.is_active = true"
                )
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
     * Metodo para cadastrar novas empresas no banco
     * Nao cadastra se ja existir uma empresa ativa com o mesmo cnpj
     * Se o cnpj for de uma empresa que foi desativada, o cadastro antigo é reativado com os dados novos
     * @param company Empresa que sera cadastrada
     * @return true se foi registrada e false se os dados forem invalidos ou o cnpj ja for de uma empresa ativa
     */
    @Override
    public boolean register(Companies company) {

        if (!isValid(company)) {
            return false;
        }

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into companies (name, trade_name, size, registration_date, cnpj, email) " +
                                "values (?, ?, ?::company_size, ?, ?, ?) " +
                                "on conflict (cnpj) do update set name = excluded.name, " +
                                "trade_name = excluded.trade_name, " +
                                "size = excluded.size, " +
                                "registration_date = excluded.registration_date, " +
                                "email = excluded.email, " +
                                "is_active = true, " +
                                "updated_at = current_timestamp " +
                                "where companies.is_active = false"
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
            throw new RuntimeException(
                    "Erro ao cadastrar empresa.",
                    e
            );
        }
    }

    /**
     * metodo para cadastrar uma nova empresa e devolver o id gerado
     * Usado quando a empresa precisa ser vinculada a uma subscription
     * Usa o returning do PostgreSQL em vez de RETURN_GENERATED_KEYS
     * Mesmas regras do register: se o cnpj for de uma empresa ativa nao cadastra e devolve -1,
     * e se for de uma empresa desativada reativa o cadastro antigo e devolve o id dele
     * @param company A empresa que sera cadastrada
     * @return O id gerado, ou -1 se os dados forem invalidos ou o cnpj ja for de uma empresa ativa
     */
    public int registerReturningId(Companies company) {

        if (!isValid(company)) {
            return -1;
        }

        try (Connection conn = getConnection()) {

            return registerReturningId(company, conn);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar empresa.",
                    e
            );
        }
    }

    /**
     * metodo para cadastrar uma nova empresa e devolver o id gerado, usando uma conexao que ja foi aberta
     * Usado quando o cadastro faz parte de uma transacao maior (a conexao nao é fechada aqui, quem chamou fecha)
     * Mesmas regras do registerReturningId sem a conexao
     * @param company A empresa que sera cadastrada
     * @param conn A conexao ja aberta que sera usada
     * @return O id gerado, ou -1 se os dados forem invalidos ou o cnpj ja for de uma empresa ativa
     */
    public int registerReturningId(Companies company, Connection conn) {

        if (!isValid(company)) {
            return -1;
        }

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO companies " +
                                "(name, trade_name, size, registration_date, cnpj, email) " +
                                "VALUES (?, ?, ?::company_size, ?, ?, ?) " +
                                "ON CONFLICT (cnpj) DO UPDATE SET name = excluded.name, " +
                                "trade_name = excluded.trade_name, " +
                                "size = excluded.size, " +
                                "registration_date = excluded.registration_date, " +
                                "email = excluded.email, " +
                                "is_active = true, " +
                                "updated_at = current_timestamp " +
                                "WHERE companies.is_active = false " +
                                "RETURNING id"
                )
        ) {
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
            }

            return -1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar empresa.",
                    e
            );
        }
    }

    /**
     * Metodo para desativar a empresa por ID (is_active = false)
     * Cascateia a desativacao para os funcionarios, setores e units da empresa,
     * e tambem para telefones, grupos de permissao e assinaturas da empresa
     * Tudo acontece numa transacao: se alguma parte der erro, nada fica desativado
     * @param id Valor unico de cada empresa
     * @return true se foi desativada e false caso a empresa nao exista (ou ja esteja desativada)
     */
    @Override
    public boolean delete(int id) {

        try (Connection conn = getConnection()) {

            // desliga o salvamento automatico pra tudo ser desativado junto (ou nada ser desativado)
            conn.setAutoCommit(false);

            try {
                // os funcionarios saem antes dos setores, os setores antes das units
                new EmployeesDAO().deleteByIdCompany(conn, id);
                new SectorsDAO().deleteByCompanyId(conn, id);
                new UnitsDAO().deleteByCompanyId(conn, id);
                new TelephoneCompaniesDAO().deleteByCompanyId(conn, id);
                new PermissionGroupsDAO().deleteByCompanyId(conn, id);
                new SubscriptionsDAO().deleteByCompanyId(conn, id);

                boolean deactivated;

                try (
                        PreparedStatement pstmt = conn.prepareStatement(
                                "update companies set is_active = false, updated_at = current_timestamp where id = ? and is_active = true"
                        )
                ) {

                    pstmt.setInt(1, id);

                    deactivated = pstmt.executeUpdate() > 0;
                }

                if (deactivated) {
                    conn.commit();
                } else {
                    conn.rollback();
                }

                return deactivated;

            } catch (SQLException e) {
                // o rollback fica aqui dentro porque o catch de fora so roda depois da conexao fechada
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar empresa pelo ID.",
                    e
            );
        }
    }

    /**
     * Metodo para desativar empresas pelo nome (is_active = false)
     * Atencao: so desativa a empresa, nao cascateia para setores, units etc. como o delete por ID
     * @param name Nome da empresa
     * @return true se alguma empresa foi desativada e false caso nenhuma seja encontrada
     */
    public boolean deleteByName(String name) {

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies " +
                                "set is_active = false, updated_at = current_timestamp " +
                                "where name ilike ?"
                )
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
     * Metodo que atualiza a empresa pelo ID
     * Nao atualiza empresa que ja foi desativada
     * @param company Valores da empresa para atualizar
     * @param id ID da empresa que sera atualizada
     * @return true se foi atualizada e false se os dados forem invalidos ou a empresa nao existir/estiver desativada
     */
    @Override
    public boolean update(Companies company, int id) {

        if (!isValid(company)) {
            return false;
        }

        try (
                Connection conn = getConnection();
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

            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, cleanCnpj(company.getCnpj()));
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
     * Metodo que atualiza a empresa pelo nome atual
     * Nao atualiza empresa que ja foi desativada
     * @param company Valores da empresa para atualizar
     * @param name Nome atual da empresa que sera atualizada
     * @return true se foi atualizada e false se os dados forem invalidos ou a empresa nao existir/estiver desativada
     */
    public boolean updateByName(Companies company, String name) {

        if (!isValid(company)) {
            return false;
        }

        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update companies set name = ?, " +
                                "trade_name = ?, " +
                                "size = ?::company_size, " +
                                "registration_date = ?, " +
                                "cnpj = ?, " +
                                "email = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ? and is_active = true"
                )
        ) {

            pstmt.setString(1, company.getName());
            pstmt.setString(2, company.getTradeName());
            pstmt.setString(3, company.getSize().getValor());
            pstmt.setDate(4, company.getRegistrationDate());
            pstmt.setString(5, cleanCnpj(company.getCnpj()));
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