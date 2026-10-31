package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.AddressesFilter;
import com.example.servelet1ano.util.SqlUtil;
import com.example.servelet1ano.validation.Validators;
import com.example.servelet1ano.model.Addresses;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AddressesDAO implements DAOI<Addresses, AddressesFilter> {

    /**
     * Comando de insert do endereco, usado no cadastro simples e no cadastro que devolve o id gerado
     */
    private static final String INSERT_ADDRESS =
            "insert into addresses (zip_code, street, number, complement, neighborhood, city, state, country) " +
                    "values (?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Condicao usada no update que desativa endereco: so desativa se nenhuma unidade ou empresa ativa usar o endereco
     */
    private static final String NOT_IN_USE =
            " and not exists (select 1 from units u where u.address_id = addresses.id and u.is_active = true)" +
                    " and not exists (select 1 from companies c where c.address_id = addresses.id and c.is_active = true)";

    /**
     * Valida os dados do endereco antes de gravar
     * Rua, numero, bairro, cidade e estado sao obrigatorios (NOT NULL no banco) e todos os campos precisam caber na tabela
     * O CEP é opcional no banco, entao so é validado quando vier preenchido
     * @param address O endereco a validar
     * @return true se os campos obrigatorios estiverem preenchidos, os tamanhos couberem e o CEP (quando informado) for valido
     */
    private boolean isValid(Addresses address) {

        if (address == null) {
            return false;
        }

        if (address.getZipCode() != null && !address.getZipCode().isBlank() && !Validators.isCep(address.getZipCode())) {
            return false;
        }

        return fits(address.getStreet(), 255, true)
                && fits(address.getNumber(), 20, true)
                && fits(address.getNeighborhood(), 150, true)
                && fits(address.getCity(), 100, true)
                && fits(address.getState(), 100, true)
                && fits(address.getComplement(), 100, false)
                && fits(address.getCountry(), 100, false);
    }

    /**
     * Confere se o texto cabe no campo do banco
     * @param value O texto
     * @param max O tamanho maximo da coluna
     * @param required true se o campo for obrigatorio (nao pode ser nulo nem vazio)
     * @return true se estiver ok
     */
    private boolean fits(String value, int max, boolean required) {

        if (value == null || value.isBlank()) {
            return !required;
        }

        return value.trim().length() <= max;
    }

    /**
     * Devolve o texto sem espacos nas pontas, ou null se estiver vazio
     */
    private String trimOrNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /**
     * Deixa so os digitos do CEP (tira o traco e espacos), pra o CEP ficar sempre no mesmo formato no banco (CHAR(8))
     * Usado antes de gravar e antes de buscar
     * @param zipCode O CEP como foi digitado
     * @return O CEP so com os 8 digitos, ou null se vier vazio
     */
    private String cleanZipCode(String zipCode) {

        if (zipCode == null || zipCode.isBlank()) {
            return null;
        }

        return zipCode.replaceAll("\\D", "");
    }

    /**
     * Preenche os 8 parametros do insert/update do endereco (cep, rua, numero, complemento, bairro, cidade, estado e pais)
     */
    private void bindAddress(PreparedStatement pstmt, Addresses address) throws SQLException {

        pstmt.setString(1, cleanZipCode(address.getZipCode()));
        pstmt.setString(2, address.getStreet().trim());
        pstmt.setString(3, address.getNumber().trim());
        pstmt.setString(4, trimOrNull(address.getComplement()));
        pstmt.setString(5, address.getNeighborhood().trim());
        pstmt.setString(6, address.getCity().trim());
        pstmt.setString(7, address.getState().trim());
        pstmt.setString(8, trimOrNull(address.getCountry()));
    }

    /**
     * Monta um endereco a partir da linha atual do ResultSet
     */
    private Addresses mapAddress(ResultSet rs) throws SQLException {

        Addresses address = new Addresses(
                rs.getInt("id"),
                rs.getString("zip_code"),
                rs.getString("street"),
                rs.getString("number"),
                rs.getString("complement"),
                rs.getString("neighborhood"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("country")
        );

        address.setCreatedAt(rs.getTimestamp("created_at"));
        address.setUpdatedAt(rs.getTimestamp("updated_at"));

        return address;
    }


    /**
     * Metodo que busca os enderecos ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Bairro, cidade, estado e pais buscam por pedaco do texto (sem diferenciar maiusculas de minusculas)
     * @param filter Objeto com os campos opcionais para filtrar a busca (se for null, busca todos)
     * @return List<Addresses> com os enderecos encontrados
     */

    @Override
    public List<Addresses> searchAll(AddressesFilter filter) {

        if (filter == null) {
            filter = new AddressesFilter();
        }

        List<Addresses> addresses = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select * from addresses where is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getZipCode() != null && !filter.getZipCode().isBlank()) {
            sql.append(" and zip_code = ?");
            parametros.add(cleanZipCode(filter.getZipCode()));
        }

        if (filter.getNeighborhood() != null && !filter.getNeighborhood().isBlank()) {
            sql.append(" and neighborhood ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getNeighborhood().trim()) + "%");
        }

        if (filter.getCity() != null && !filter.getCity().isBlank()) {
            sql.append(" and city ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getCity().trim()) + "%");
        }

        if (filter.getState() != null && !filter.getState().isBlank()) {
            sql.append(" and state ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getState().trim()) + "%");
        }

        if (filter.getCountry() != null && !filter.getCountry().isBlank()) {
            sql.append(" and country ilike ?");
            parametros.add("%" + SqlUtil.escapeLike(filter.getCountry().trim()) + "%");
        }

        sql.append(" order by id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapAddress(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os endereços no banco de dados.", e);
        }

        return addresses;
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do endereco
     * @return Retorna o endereco encontrado, ou null se nao existir ou estiver desativado
     */
    @Override
    public Addresses searchById(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from addresses where id = ? and is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapAddress(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar o endereço de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para buscar enderecos pela cidade (somente ativos)
     * O nome da cidade precisa ser igual (sem diferenciar maiusculas de minusculas)
     * @param city A cidade do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCity(String city) {
        return searchByExactValue("city", city);
    }

    /**
     * metodo para buscar enderecos pelo estado (somente ativos)
     * O nome do estado precisa ser igual (sem diferenciar maiusculas de minusculas)
     * @param state O estado do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByState(String state) {
        return searchByExactValue("state", state);
    }

    /**
     * metodo para buscar enderecos pelo pais (somente ativos)
     * O nome do pais precisa ser igual (sem diferenciar maiusculas de minusculas)
     * @param country O pais do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCountry(String country) {
        return searchByExactValue("country", country);
    }

    /**
     * metodo para cadastrar novos enderecos no banco
     * O CEP e gravado so com os digitos
     * @param address O endereco que sera cadastrado
     * @return true se foi registrado e false se os dados forem invalidos
     */
    @Override
    public boolean register(Addresses address) {

        if (!isValid(address)) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(INSERT_ADDRESS)
        ) {

            bindAddress(pstmt, address);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar o endereço no banco de dados.", e);
        }
    }


    /**
     * metodo para cadastrar um novo endereco e devolver o id gerado
     * Usado quando o endereco precisa ser vinculado na hora a uma empresa ou unit
     * Usa o returning do PostgreSQL em vez de RETURN_GENERATED_KEYS
     * @param address O endereco que sera cadastrado
     * @return O id gerado, ou -1 se os dados forem invalidos
     */
    public int registerReturningId(Addresses address) {

        if (!isValid(address)) {
            return -1;
        }

        try (Connection conn = ConnectionFactory.connect()) {

            return registerReturningId(address, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar o endereço no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar um novo endereco usando uma conexao ja aberta e devolver o id gerado
     * (permite que quem chamou controle a transacao, ex: cadastrar o endereco e a empresa juntos)
     * @param address O endereco que sera cadastrado
     * @param conn A conexao (nao e fechada aqui)
     * @return O id gerado, ou -1 se os dados forem invalidos
     * @throws SQLException se der erro no banco
     */
    public int registerReturningId(Addresses address, Connection conn) throws SQLException {

        if (!isValid(address)) {
            return -1;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_ADDRESS + " returning id")) {

            bindAddress(pstmt, address);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return -1;
    }


    /**
     * metodo para desativar o endereco por id (is_active = false)
     * Nao desativa se uma unidade ou empresa ativa ainda usar o endereco
     * (use isInUse para saber o motivo quando devolver false)
     * @param id Valor unico de cada endereco
     * @return true se foi desativado e false se o endereco nao existe, ja estava desativado ou ainda esta em uso
     */
    @Override
    public boolean delete(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update addresses set is_active = false, updated_at = current_timestamp " +
                                "where id = ? and is_active = true" + NOT_IN_USE
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar o endereço de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que confere se o endereco ainda esta sendo usado por alguma unidade ou empresa ativa
     * Util para o servlet avisar o usuario antes de desativar o endereco
     * @param id O id do endereco
     * @return true se alguma unidade ou empresa ativa usa o endereco
     */
    public boolean isInUse(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select 1 where exists (select 1 from units where address_id = ? and is_active = true) " +
                                "or exists (select 1 from companies where address_id = ? and is_active = true)"
                )
        ) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conferir o uso do endereço de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo que muda os values do endereco selecionado pelo id
     * @param address Os valores do endereco para ser atualizado
     * @param id O valor unico do endereco que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos ou o endereco nao existir/estiver desativado
     */
    @Override
    public boolean update(Addresses address, int id) {

        if (!isValid(address)) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update addresses set zip_code = ?, " +
                                "street = ?, " +
                                "number = ?, " +
                                "complement = ?, " +
                                "neighborhood = ?, " +
                                "city = ?, " +
                                "state = ?, " +
                                "country = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ? and is_active = true"
                )
        ) {

            bindAddress(pstmt, address);
            pstmt.setInt(9, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar o endereço de id " + id + " no banco de dados.", e);
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Busca enderecos ativos em que a coluna e igual ao valor (sem diferenciar maiusculas de minusculas)
     * @param column Nome da coluna: use somente valores fixos do codigo ("city", "state" ou "country"), nunca texto digitado
     * @param value O valor procurado
     * @return List<Addresses> com os enderecos encontrados (vazia se o valor vier vazio)
     */
    private List<Addresses> searchByExactValue(String column, String value) {

        List<Addresses> addresses = new ArrayList<>();

        if (value == null || value.isBlank()) {
            return addresses;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from addresses where lower(" + column + ") = lower(?) and is_active = true order by id"
                )
        ) {

            pstmt.setString(1, value.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapAddress(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar os endereços pelo campo " + column + " no banco de dados.", e);
        }

        return addresses;
    }

}