package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.UnitsFilter;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitsDAO implements DAOI<Units, UnitsFilter> {

    /**
     * Valida os dados da unit antes de gravar (o cnpj é obrigatorio no banco e precisa ser valido)
     * @param unit A unit a validar
     * @return true se a unit existir e o cnpj for valido
     */
    private boolean isValid(Units unit) {

        if (unit == null) {
            return false;
        }

        if (!Validators.isCnpj(unit.getCnpj())) {
            return false;
        }

        return true;
    }

    /**
     * Confere se a unit tem a empresa e o endereco informados (os dois ids sao obrigatorios pra cadastrar ou atualizar)
     * Sem isso o id ficaria 0 e o banco recusaria por causa da chave estrangeira
     * @param unit A unit a conferir
     * @return true se o id da empresa e o id do endereco forem maiores que zero
     */
    private boolean hasParents(Units unit) {

        if (unit == null) {
            return false;
        }

        if (unit.getCompanyId() <= 0 || unit.getAddressId() <= 0) {
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
     * Metodo que busca as units ativas aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Units> com as units encontradas
     */
    @Override
    public List<Units> searchAll(UnitsFilter filter) {

        List<Units> units = new ArrayList<>();

        if (filter == null) {
            filter = new UnitsFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                        "c.name as companyName, c.id as idCompany, " +
                        "a.id as idAddress, a.zip_code as addressZipCode, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                        "a.neighborhood as addressNeighborhood, a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                        "from units u " +
                        "join companies c on u.company_id = c.id " +
                        "join addresses a on a.id = u.address_id " +
                        "where u.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and u.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and u.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getCompanyName() != null && !filter.getCompanyName().isBlank()) {
            sql.append(" and c.name ilike ?");
            parametros.add("%" + filter.getCompanyName() + "%");
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and u.name ilike ?");
            parametros.add("%" + filter.getName() + "%");
        }

        if (filter.getCnpj() != null && !filter.getCnpj().isBlank()) {
            sql.append(" and u.cnpj = ?");
            parametros.add(cleanCnpj(filter.getCnpj()));
        }

        if (filter.getCnae() != null && !filter.getCnae().isBlank()) {
            sql.append(" and u.cnae = ?");
            parametros.add(filter.getCnae());
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return units;
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
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

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

                    Units unit = new Units(
                            rs.getInt("id"),
                            rs.getInt("companyId"),
                            rs.getInt("addressId"),
                            rs.getString("name"),
                            rs.getString("cnpj"),
                            rs.getString("cnae"),
                            rs.getBoolean("is_active"),
                            company,
                            address
                    );

                    unit.setCreatedAt(rs.getTimestamp("created_at"));
                    unit.setUpdatedAt(rs.getTimestamp("updated_at"));

                    units.add(unit);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return units;
    }

    /**
     * metodo para buscar por Id (somente ativas)
     * @param id Numero unico da unit
     * @return Retorna a unit encontrada ou null caso nao exista
     */
    @Override
    public Units searchById(int id) {

        Units unit = null;

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                                "c.name as companyName, c.id as idCompany, " +
                                "a.id as idAddress, a.zip_code as addressZipCode, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                                "a.neighborhood as addressNeighborhood, a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                                "from units u " +
                                "join companies c on u.company_id = c.id " +
                                "join addresses a on a.id = u.address_id " +
                                "where u.id = ? and u.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    Companies company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

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

                    unit = new Units(
                            rs.getInt("id"),
                            rs.getInt("companyId"),
                            rs.getInt("addressId"),
                            rs.getString("name"),
                            rs.getString("cnpj"),
                            rs.getString("cnae"),
                            rs.getBoolean("is_active"),
                            company,
                            address
                    );

                    unit.setCreatedAt(rs.getTimestamp("created_at"));
                    unit.setUpdatedAt(rs.getTimestamp("updated_at"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unit;
    }

    /**
     * metodo para cadastrar novas units no banco (sempre ativa na criacao)
     * @param unit A unit que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    @Override
    public boolean register(Units unit) {

        if (!isValid(unit)) {
            return false;
        }

        if (!hasParents(unit)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, cleanCnpj(unit.getCnpj()));
            pstmt.setString(5, unit.getCnae());
            pstmt.setBoolean(6, true);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de uma unit (sempre ativas na criacao)
     * Todas entram juntas: se alguma der erro, nenhuma fica salva (transacao)
     * @param units Uma lista de units
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Units> units) {

        if (units == null || units.isEmpty()) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

            try {
                // desliga o salvamento automatico pra tudo entrar junto (ou nada entrar)
                conn.setAutoCommit(false);

                for (int i = 0; i < units.size(); i++) {
                    Units unit = units.get(i);

                    if (!isValid(unit)) {
                        return false;
                    }

                    if (!hasParents(unit)) {
                        return false;
                    }

                    pstmt.setInt(1, unit.getCompanyId());
                    pstmt.setInt(2, unit.getAddressId());
                    pstmt.setString(3, unit.getName());
                    pstmt.setString(4, cleanCnpj(unit.getCnpj()));
                    pstmt.setString(5, unit.getCnae());
                    pstmt.setBoolean(6, true);

                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                // o rollback fica aqui dentro porque o catch de fora so roda depois da conexao fechada
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar a unit por id (is_active = false)
     * Cascateia a desativacao para os funcionarios e os setores da unidade
     * Tudo acontece numa transacao: se alguma parte der erro, nada fica desativado
     * @param id Valor unico de cada unit
     * @return true se foi desativada e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (conn) {

            // desliga o salvamento automatico pra tudo ser desativado junto (ou nada ser desativado)
            conn.setAutoCommit(false);

            try {
                // os funcionarios saem antes dos setores, porque o setor com funcionario ativo nao pode ser desativado
                new EmployeesDAO().deleteByIdUnit(conn, id);
                new SectorsDAO().deleteByUnitId(conn, id);

                boolean deactivated;

                try (
                        PreparedStatement pstmt = conn.prepareStatement(
                                "update units set is_active = false, updated_at = current_timestamp where id = ?"
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa as units de uma empresa (para quando uma empresa for desativada)
     * Cascateia a desativacao para os funcionarios e os setores da empresa, tudo numa transacao
     * Se a empresa nao tiver units devolve true, e isso nao e um erro
     * @param companyId O id da empresa
     * @return true se tudo foi desativado com sucesso e false caso tenha dado erro
     */
    public boolean deleteByCompanyId(int companyId) {

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (conn) {

            // desliga o salvamento automatico pra tudo ser desativado junto (ou nada ser desativado)
            conn.setAutoCommit(false);

            try {
                new EmployeesDAO().deleteByIdCompany(conn, companyId);
                new SectorsDAO().deleteByCompanyId(conn, companyId);
                deleteByCompanyId(conn, companyId);

                conn.commit();
                return true;

            } catch (SQLException e) {
                // o rollback fica aqui dentro porque o catch de fora so roda depois da conexao fechada
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa as units de uma empresa dentro de uma transacao que ja esta aberta
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Se a empresa nao tiver units devolve false, e isso nao e um erro
     * Desative os funcionarios e os setores da empresa antes de chamar esse metodo
     * @param conn A conexao da transacao
     * @param companyId O id da empresa
     * @return true se alguma unit foi desativada
     * @throws SQLException se o banco der erro, pra quem chamou desfazer a transacao (rollback)
     */
    public boolean deleteByCompanyId(Connection conn, int companyId) throws SQLException {

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "update units set is_active = false, updated_at = current_timestamp where company_id = ?"
                )
        ) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que muda os values da unit selecionada pelo id
     * @param unit Os valores da unit para ser atualizada
     * @param id O valor unico da unit que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(Units unit, int id) {

        if (!isValid(unit)) {
            return false;
        }

        if (!hasParents(unit)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update units set company_id = ?, " +
                                "address_id = ?, " +
                                "name = ?, " +
                                "cnpj = ?, " +
                                "cnae = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setInt(1, unit.getCompanyId());
            pstmt.setInt(2, unit.getAddressId());
            pstmt.setString(3, unit.getName());
            pstmt.setString(4, cleanCnpj(unit.getCnpj()));
            pstmt.setString(5, unit.getCnae());
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}