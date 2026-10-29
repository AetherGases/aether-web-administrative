package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.UnitsFilter;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.util.SqlUtil;
import com.example.servelet1ano.validation.Validators;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnitsDAO implements DAOI<Units, UnitsFilter> {

    /**
     * Select base usado na busca geral e na busca por id
     * As juncoes sao left join pra nao sumir uma unit que esteja sem empresa ou sem endereco
     */
    private static final String SELECT_UNITS =
            "select u.*, u.company_id as companyId, u.address_id as addressId, " +
                    "c.name as companyName, c.id as idCompany, " +
                    "a.id as idAddress, a.zip_code as addressZipCode, a.street as addressStreet, a.number as addressNumber, a.complement as addressComplement, " +
                    "a.neighborhood as addressNeighborhood, a.city as addressCity, a.state as addressState, a.country as addressCountry " +
                    "from units u " +
                    "left join companies c on u.company_id = c.id " +
                    "left join addresses a on a.id = u.address_id ";

    /**
     * Metodo que busca as units ativas aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * @param filter Objeto com os campos opcionais para filtrar a busca (se for null, busca todas)
     * @return List<Units> com as units encontradas
     */
    @Override
    public List<Units> searchAll(UnitsFilter filter) {

        if (filter == null) {
            filter = new UnitsFilter();
        }

        List<Units> units = new ArrayList<>();

        StringBuilder sql = new StringBuilder(SELECT_UNITS + "where u.is_active = true");

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
            parametros.add("%" + SqlUtil.escapeLike(filter.getCompanyName().trim()) + "%");
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and u.name ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getName().trim()) + "%");
        }

        if (filter.getCnpj() != null && !filter.getCnpj().isBlank()) {
            sql.append(" and u.cnpj = ?");
            parametros.add(cleanCnpj(filter.getCnpj()));
        }

        if (filter.getCnae() != null && !filter.getCnae().isBlank()) {
            sql.append(" and u.cnae = ?");
            parametros.add(filter.getCnae().trim());
        }

        sql.append(" order by u.id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    units.add(mapUnit(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar as unidades no banco de dados.", e);
        }

        return units;
    }

    /**
     * metodo para buscar por Id (somente ativas)
     * @param id Numero unico da unit
     * @return Retorna a unit encontrada, ou null se nao existir ou estiver desativada
     */
    @Override
    public Units searchById(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        SELECT_UNITS + "where u.id = ? and u.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUnit(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar a unidade de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar novas units no banco (sempre ativa na criacao)
     * O cnpj e gravado so com letras e numeros, em maiusculo
     * @param unit A unit que sera cadastrada
     * @return true se foi registrada e false se os dados forem invalidos (ex: cnpj invalido, empresa ou endereco inexistente)
     */
    @Override
    public boolean register(Units unit) {

        if (!isValid(unit)) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesExist(conn, unit.getCompanyId(), unit.getAddressId())) {
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                            "values (?, ?, ?, ?, ?, ?)"
            )) {

                pstmt.setInt(1, unit.getCompanyId());
                pstmt.setInt(2, unit.getAddressId());
                pstmt.setString(3, unit.getName().trim());
                pstmt.setString(4, cleanCnpj(unit.getCnpj()));
                pstmt.setString(5, cnaeOf(unit));
                pstmt.setBoolean(6, true);

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar a unidade no banco de dados.", e);
        }
    }

    /**
     * metodo para registrar mais de uma unit (sempre ativas na criacao)
     * Tudo ou nada: se alguma unit for invalida ou der erro, nenhuma e cadastrada
     * @param units Uma lista de units
     * @return true se todas foram registradas e false se alguma tiver dados invalidos
     */
    public boolean registerLot(List<Units> units) {

        if (units == null || units.isEmpty()) {
            return false;
        }

        for (Units unit : units) {
            if (!isValid(unit)) {
                return false;
            }
        }

        try (Connection conn = ConnectionFactory.connect()) {

            conn.setAutoCommit(false);

            try {

                Set<String> verificados = new HashSet<>();

                for (Units unit : units) {
                    String chave = unit.getCompanyId() + ":" + unit.getAddressId();

                    if (verificados.add(chave)
                            && !referencesExist(conn, unit.getCompanyId(), unit.getAddressId())) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "insert into units (company_id, address_id, name, cnpj, cnae, is_active) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )) {

                    for (Units unit : units) {
                        pstmt.setInt(1, unit.getCompanyId());
                        pstmt.setInt(2, unit.getAddressId());
                        pstmt.setString(3, unit.getName().trim());
                        pstmt.setString(4, cleanCnpj(unit.getCnpj()));
                        pstmt.setString(5, cnaeOf(unit));
                        pstmt.setBoolean(6, true);

                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar as unidades em lote no banco de dados.", e);
        }
    }

    /**
     * metodo para desativar a unit por id (is_active = false)
     * Cascateia a desativacao para os funcionarios e os setores da unidade
     * Tudo acontece na mesma transacao: ou a unit e seus dependentes sao desativados juntos, ou nada muda
     * @param id Valor unico de cada unit
     * @return true se foi desativada e false se a unit nao existe ou ja estava desativada
     */
    @Override
    public boolean delete(int id) {

        try (Connection conn = ConnectionFactory.connect()) {

            // desliga o salvamento automatico pra tudo ser desativado junto (ou nada)
            conn.setAutoCommit(false);

            try {

                boolean deactivated;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "update units set is_active = false, updated_at = current_timestamp " +
                                "where id = ? and is_active = true"
                )) {
                    pstmt.setInt(1, id);
                    deactivated = pstmt.executeUpdate() > 0;
                }

                if (!deactivated) {
                    conn.rollback();
                    return false;
                }

                new EmployeesDAO().deleteByIdUnit(id, conn);
                new SectorsDAO().deleteByUnitId(id, conn);

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar a unidade de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que desativa as units de uma empresa (para quando uma empresa for desativada)
     * Desativa todas as units ativas da empresa, cascateando para os setores e funcionarios de cada unidade
     * Tudo na mesma transacao: ou desativa tudo, ou nada muda
     * @param companyId O id da empresa
     * @return true se deu certo (inclusive se a empresa nao tinha units ativas); se der erro, lanca excecao
     */
    public boolean deleteByCompanyId(int companyId) {

        try (Connection conn = ConnectionFactory.connect()) {

            // desliga o salvamento automatico pra tudo ser desativado junto (ou nada)
            conn.setAutoCommit(false);

            try {
                deleteByCompanyId(companyId, conn);
                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar as unidades da empresa de id " + companyId + ".", e);
        }
    }

    /**
     * metodo que desativa as units de uma empresa usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao, ex: desativar a empresa e tudo o que depende dela juntos)
     * Cascateia para os funcionarios e os setores de cada unidade
     * @param companyId O id da empresa
     * @param conn A conexao (nao e fechada aqui)
     * @return true se alguma unit foi desativada e false se a empresa nao tinha units ativas
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByCompanyId(int companyId, Connection conn) throws SQLException {

        List<Integer> unitIds = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select id from units where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    unitIds.add(rs.getInt("id"));
                }
            }
        }

        EmployeesDAO employeesDAO = new EmployeesDAO();
        SectorsDAO sectorsDAO = new SectorsDAO();

        for (int unitId : unitIds) {
            employeesDAO.deleteByIdUnit(unitId, conn);
            sectorsDAO.deleteByUnitId(unitId, conn);
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "update units set is_active = false, updated_at = current_timestamp " +
                        "where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que desativa pelo nome, dentro de uma empresa
     * O nome precisa ser igual (sem diferenciar maiusculas de minusculas); nao e mais busca por pedaco do nome
     * Desativa uma por uma, reaproveitando delete() pra cascatear os setores e funcionarios de cada unidade
     * @param name O nome da unit
     * @param companyId O id da empresa dona da unit (evita desativar units de outras empresas)
     * @return true se todas as encontradas foram desativadas e false se nao achou nenhuma ou se alguma nao foi desativada
     */
    public boolean deleteByName(String name, int companyId) {

        if (name == null || name.isBlank()) {
            return false;
        }

        List<Integer> ids = findIdsByName(name, companyId, Integer.MAX_VALUE);

        if (ids.isEmpty()) {
            return false;
        }

        boolean allSuccess = true;

        for (int unitId : ids) {
            if (!delete(unitId)) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * metodo que muda os values da unit selecionada pelo id
     * A unit nao pode mudar de empresa (os setores e funcionarios dela continuariam na empresa antiga)
     * @param unit Os valores da unit para ser atualizada
     * @param id O valor unico da unit que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos ou a unit nao existir/estiver desativada
     */
    @Override
    public boolean update(Units unit, int id) {

        if (!isValid(unit)) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesExist(conn, unit.getCompanyId(), unit.getAddressId())) {
                return false;
            }

            return updateUnit(conn, unit, id);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a unidade de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * Metodo que muda os values da unit selecionada pelo nome, dentro de uma empresa
     * So atualiza se existir exatamente uma unit ativa com esse nome na empresa
     * (se houver mais de uma, nao da pra saber qual atualizar, entao use o update por id)
     * @param unit Os valores da unit para ser atualizada
     * @param name O nome para achar a unit
     * @param companyId O id da empresa dona da unit
     * @return true se foi atualizado e false se os dados forem invalidos ou nao achou exatamente uma unit
     */
    public boolean updateByName(Units unit, String name, int companyId) {

        if (!isValid(unit) || unit.getCompanyId() != companyId || name == null || name.isBlank()) {
            return false;
        }

        List<Integer> ids = findIdsByName(name, companyId, 2);

        if (ids.size() != 1) {
            return false;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            if (!referencesExist(conn, unit.getCompanyId(), unit.getAddressId())) {
                return false;
            }

            return updateUnit(conn, unit, ids.get(0));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a unidade pelo nome no banco de dados.", e);
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Monta uma unit (com empresa e endereco) a partir da linha atual do ResultSet
     * Como as juncoes sao left join, empresa/endereco ficam null se a unit nao tiver vinculo
     */
    private Units mapUnit(ResultSet rs) throws SQLException {

        Companies company = null;
        Addresses address = null;

        int idCompany = rs.getInt("idCompany");
        if (!rs.wasNull()) {
            company = new Companies(idCompany, rs.getString("companyName"));
        }

        int idAddress = rs.getInt("idAddress");
        if (!rs.wasNull()) {
            address = new Addresses(
                    idAddress,
                    rs.getString("addressZipCode"),
                    rs.getString("addressStreet"),
                    rs.getString("addressNumber"),
                    rs.getString("addressComplement"),
                    rs.getString("addressNeighborhood"),
                    rs.getString("addressCity"),
                    rs.getString("addressState"),
                    rs.getString("addressCountry")
            );
        }

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

        return unit;
    }

    /**
     * Valida os dados da unit antes de gravar (nome obrigatorio, cnpj valido e tamanhos conforme a tabela)
     * @param unit A unit a validar
     * @return true se os dados forem validos
     */
    private boolean isValid(Units unit) {

        if (unit == null || unit.getName() == null || unit.getName().isBlank()) {
            return false;
        }

        if (unit.getName().trim().length() > 255) {
            return false;
        }

        if (!Validators.isCnpj(unit.getCnpj())) {
            return false;
        }

        return unit.getCnae() == null || unit.getCnae().trim().length() <= 50;
    }

    /**
     * Deixa so letras e numeros do cnpj, em maiusculo (tira pontos, barra, traco e espacos)
     * Mesmo formato usado no CompaniesDAO, pra o cnpj ficar sempre igual no banco
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
     * Devolve o cnae sem espacos nas pontas (ou null se estiver vazio)
     */
    private String cnaeOf(Units unit) {

        if (unit.getCnae() == null || unit.getCnae().isBlank()) {
            return null;
        }

        return unit.getCnae().trim();
    }

    /**
     * Confere se a empresa e o endereco existem e estao ativos
     */
    private boolean referencesExist(Connection conn, int companyId, int addressId) throws SQLException {
        return existsActive(conn, "companies", companyId) && existsActive(conn, "addresses", addressId);
    }

    /**
     * Confere se existe um registro ativo com esse id
     * @param table Nome da tabela: use somente valores fixos do codigo, nunca texto digitado
     */
    private boolean existsActive(Connection conn, String table, int id) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select 1 from " + table + " where id = ? and is_active = true"
        )) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Atualiza uma unit ativa pelo id usando a conexao recebida
     * A empresa nao e alterada: o where exige que seja a mesma empresa da unit
     */
    private boolean updateUnit(Connection conn, Units unit, int id) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "update units set address_id = ?, " +
                        "name = ?, " +
                        "cnpj = ?, " +
                        "cnae = ?, " +
                        "updated_at = current_timestamp " +
                        "where id = ? and company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, unit.getAddressId());
            pstmt.setString(2, unit.getName().trim());
            pstmt.setString(3, cleanCnpj(unit.getCnpj()));
            pstmt.setString(4, cnaeOf(unit));
            pstmt.setInt(5, id);
            pstmt.setInt(6, unit.getCompanyId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Procura os ids das units ativas com esse nome (igual, sem diferenciar maiusculas) dentro de uma empresa
     * @param max Quantidade maxima de ids a devolver
     */
    private List<Integer> findIdsByName(String name, int companyId, int max) {

        List<Integer> ids = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select id from units " +
                                "where lower(name) = lower(?) and company_id = ? and is_active = true " +
                                "order by id limit ?"
                )
        ) {

            pstmt.setString(1, name.trim());
            pstmt.setInt(2, companyId);
            pstmt.setInt(3, max);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar a unidade pelo nome no banco de dados.", e);
        }

        return ids;
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

}