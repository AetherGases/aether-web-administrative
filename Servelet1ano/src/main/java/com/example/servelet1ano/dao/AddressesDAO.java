package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.AddressesFilter;
import com.example.servelet1ano.validation.Validators;
import com.example.servelet1ano.model.Addresses;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AddressesDAO implements DAOI<Addresses, AddressesFilter> {

    /**
     * Interface usada pra passar os parametros do PreparedStatement para os metodos auxiliares
     */
    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }

    /**
     * Abre a conexao com o banco
     * @return A conexao aberta
     * @throws IllegalStateException se nao for possivel conectar
     */
    private Connection requireConnection() throws SQLException {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    /**
     * Valida os dados do endereco antes de gravar
     * Rua, numero, bairro, cidade e estado sao obrigatorios no banco
     * O CEP é opcional no banco, entao so é validado quando vier preenchido
     * @param address O endereco a validar
     * @return true se os campos obrigatorios estiverem preenchidos e o CEP (quando informado) for valido
     */
    private boolean isValid(Addresses address) {

        if (address == null) {
            return false;
        }

        if (isEmpty(address.getStreet()) || isEmpty(address.getNumber()) || isEmpty(address.getNeighborhood())
                || isEmpty(address.getCity()) || isEmpty(address.getState())) {
            return false;
        }

        if (!isEmpty(address.getZipCode()) && !Validators.isCep(address.getZipCode())) {
            return false;
        }

        return true;
    }

    /**
     * Confere se o texto esta nulo ou so com espacos
     * @param text O texto a conferir
     * @return true se estiver vazio
     */
    private boolean isEmpty(String text) {
        return text == null || text.isBlank();
    }

    /**
     * Deixa so os digitos do cep (tira hifen, pontos e espacos)
     * Usado antes de gravar e antes de buscar, pra o cep ficar sempre no mesmo formato no banco (zip_code é CHAR(8))
     * @param zipCode O cep como foi digitado
     * @return O cep so com os digitos, ou null se vier null ou vazio
     */
    private String cleanCep(String zipCode) {

        if (zipCode == null) {
            return null;
        }

        String digits = zipCode.replaceAll("\\D", "");

        if (digits.isEmpty()) {
            return null;
        }

        return digits;
    }

    /**
     * Monta um objeto Addresses a partir da linha atual do ResultSet
     * @param rs O ResultSet posicionado na linha que sera lida
     * @return O Addresses com os dados da linha
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
     * Coloca os valores do endereco no PreparedStatement (insert e update usam a mesma ordem)
     * O cep é gravado so com os digitos
     * @param pstmt O PreparedStatement que vai receber os valores
     * @param address O endereco com os valores
     */
    private void bindAddress(PreparedStatement pstmt, Addresses address) throws SQLException {

        pstmt.setString(1, cleanCep(address.getZipCode()));
        pstmt.setString(2, address.getStreet());
        pstmt.setString(3, address.getNumber());
        pstmt.setString(4, address.getComplement());
        pstmt.setString(5, address.getNeighborhood());
        pstmt.setString(6, address.getCity());
        pstmt.setString(7, address.getState());
        pstmt.setString(8, address.getCountry());
    }

    /**
     * Executa um insert/update/delete e diz se alguma linha foi afetada
     * @param sql O comando SQL
     * @param binder Quem preenche os parametros do comando
     * @param errorMessage Mensagem da excecao caso de erro
     * @return true se alguma linha foi afetada
     * @throws RuntimeException se ocorrer erro de SQL
     */
    private boolean executeUpdate(String sql, StatementBinder binder, String errorMessage) {

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            binder.bind(pstmt);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    errorMessage,
                    e
            );
        }
    }

    /**
     * Executa um select e devolve a lista de enderecos encontrados
     * @param sql O comando SQL
     * @param binder Quem preenche os parametros do comando
     * @param errorMessage Mensagem da excecao caso de erro
     * @return List<Addresses> com os enderecos encontrados
     * @throws RuntimeException se ocorrer erro de SQL
     */
    private List<Addresses> executeQueryList(String sql, StatementBinder binder, String errorMessage) {

        List<Addresses> addresses = new ArrayList<>();

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            binder.bind(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapAddress(rs));
                }
            }

            return addresses;

        } catch (SQLException e) {
            throw new RuntimeException(
                    errorMessage,
                    e
            );
        }
    }


    /**
     * Metodo que busca os enderecos ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Addresses> com os enderecos encontrados
     */
    @Override
    public List<Addresses> searchAll(AddressesFilter filter) {

        if (filter == null) {
            filter = new AddressesFilter();
        }

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
            String zipCode = cleanCep(filter.getZipCode());
            parametros.add(zipCode == null ? "" : zipCode);
        }

        if (filter.getNeighborhood() != null && !filter.getNeighborhood().isBlank()) {
            sql.append(" and neighborhood ilike ?");
            parametros.add("%" + filter.getNeighborhood() + "%");
        }

        if (filter.getCity() != null && !filter.getCity().isBlank()) {
            sql.append(" and city ilike ?");
            parametros.add("%" + filter.getCity() + "%");
        }

        if (filter.getState() != null && !filter.getState().isBlank()) {
            sql.append(" and state ilike ?");
            parametros.add("%" + filter.getState() + "%");
        }

        if (filter.getCountry() != null && !filter.getCountry().isBlank()) {
            sql.append(" and country ilike ?");
            parametros.add("%" + filter.getCountry() + "%");
        }

        return executeQueryList(
                sql.toString(),
                pstmt -> {
                    for (int i = 0; i < parametros.size(); i++) {
                        pstmt.setObject(i + 1, parametros.get(i));
                    }
                },
                "Erro ao buscar endereços com filtros."
        );
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do endereco
     * @return Retorna o endereco encontrado ou null caso nao exista
     */
    @Override
    public Addresses searchById(int id) {

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "select * from addresses where id = ? and is_active = true"
             )) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapAddress(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar endereço por ID.",
                    e
            );
        }
    }

    /**
     * metodo para buscar enderecos pela cidade (somente ativos)
     * @param city A cidade do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCity(String city) {

        return executeQueryList(
                "select * from addresses where city ilike ? and is_active = true",
                pstmt -> pstmt.setString(1, city),
                "Erro ao buscar endereços por cidade."
        );
    }

    /**
     * metodo para buscar enderecos pelo estado (somente ativos)
     * @param state O estado do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByState(String state) {

        return executeQueryList(
                "select * from addresses where state ilike ? and is_active = true",
                pstmt -> pstmt.setString(1, state),
                "Erro ao buscar endereços por estado."
        );
    }

    /**
     * metodo para buscar enderecos pelo pais (somente ativos)
     * @param country O pais do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCountry(String country) {

        return executeQueryList(
                "select * from addresses where country ilike ? and is_active = true",
                pstmt -> pstmt.setString(1, country),
                "Erro ao buscar endereços por país."
        );
    }

    /**
     * metodo para cadastrar novos enderecos no banco
     * @param address O endereco que sera cadastrado
     * @return true se foi registrado e false se os dados forem invalidos
     */
    @Override
    public boolean register(Addresses address) {

        if (!isValid(address)) {
            return false;
        }

        return executeUpdate(
                "insert into addresses (zip_code, street, number, complement, neighborhood, city, state, country) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?)",
                pstmt -> bindAddress(pstmt, address),
                "Erro ao cadastrar endereço."
        );
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

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "insert into addresses (zip_code, street, number, complement, neighborhood, city, state, country) " +
                             "values (?, ?, ?, ?, ?, ?, ?, ?) " +
                             "returning id"
             )) {

            bindAddress(pstmt, address);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

            return -1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar endereço e obter seu ID.",
                    e
            );
        }
    }


    /**
     * metodo para desativar o endereco por id (is_active = false)
     * Nao desativa se alguma unit ou empresa ativa ainda usar esse endereco (a conferencia e a desativacao
     * acontecem no mesmo comando, entao nao da pra um vinculo novo entrar no meio)
     * @param id Valor unico de cada endereco
     * @return true se foi desativado e false caso o endereco ainda esteja em uso (ou nao exista)
     */
    @Override
    public boolean delete(int id) {

        return executeUpdate(
                "update addresses a set is_active = false, updated_at = current_timestamp " +
                        "where a.id = ? and a.is_active = true " +
                        "and not exists (select 1 from units u where u.address_id = a.id and u.is_active = true) " +
                        "and not exists (select 1 from companies c where c.address_id = a.id and c.is_active = true)",
                pstmt -> pstmt.setInt(1, id),
                "Erro ao desativar endereço."
        );
    }

    /**
     * metodo que muda os values do endereco selecionado pelo id
     * Nao atualiza endereco que ja foi desativado
     * @param address Os valores do endereco para ser atualizado
     * @param id O valor unico do endereco que quer atualizar
     * @return true se foi mudado e false se os dados forem invalidos ou o endereco nao existir/estiver desativado
     */
    @Override
    public boolean update(Addresses address, int id) {

        if (!isValid(address)) {
            return false;
        }

        return executeUpdate(
                "update addresses set zip_code = ?, " +
                        "street = ?, " +
                        "number = ?, " +
                        "complement = ?, " +
                        "neighborhood = ?, " +
                        "city = ?, " +
                        "state = ?, " +
                        "country = ?, " +
                        "updated_at = current_timestamp " +
                        "where id = ? and is_active = true",
                pstmt -> {
                    bindAddress(pstmt, address);
                    pstmt.setInt(9, id);
                },
                "Erro ao atualizar endereço."
        );
    }

}