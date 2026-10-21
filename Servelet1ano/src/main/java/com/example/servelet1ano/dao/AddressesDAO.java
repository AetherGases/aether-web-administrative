
package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.AddressesFilter;
import com.example.servelet1ano.validation.Validators;
import com.example.servelet1ano.model.Addresses;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressesDAO implements DAOI<Addresses, AddressesFilter> {

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement pstmt) throws SQLException;
    }

    private Connection requireConnection() throws SQLException {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

    private boolean isValid(Addresses address) {
        if (address == null) {
            return false;
        }

        String zipCode = address.getZipCode();

        return zipCode == null
                || zipCode.isBlank()
                || Validators.isCep(zipCode);
    }

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

    private boolean executeUpdate(
            String sql,
            StatementBinder binder,
            String errorMessage
    ) {
        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            binder.bind(pstmt);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    private List<Addresses> executeQueryList(
            String sql,
            StatementBinder binder,
            String errorMessage
    ) {
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
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public List<Addresses> searchAll(AddressesFilter filter) {

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM addresses WHERE is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter != null && filter.getId() != null) {
            sql.append(" AND id = ?");
            parametros.add(filter.getId());
        }

        if (filter != null
                && filter.getZipCode() != null
                && !filter.getZipCode().isBlank()) {

            sql.append(" AND zip_code = ?");
            parametros.add(filter.getZipCode());
        }

        if (filter != null
                && filter.getNeighborhood() != null
                && !filter.getNeighborhood().isBlank()) {

            sql.append(" AND neighborhood ILIKE ?");
            parametros.add("%" + filter.getNeighborhood() + "%");
        }

        if (filter != null
                && filter.getCity() != null
                && !filter.getCity().isBlank()) {

            sql.append(" AND city ILIKE ?");
            parametros.add("%" + filter.getCity() + "%");
        }

        if (filter != null
                && filter.getState() != null
                && !filter.getState().isBlank()) {

            sql.append(" AND state ILIKE ?");
            parametros.add("%" + filter.getState() + "%");
        }

        if (filter != null
                && filter.getCountry() != null
                && !filter.getCountry().isBlank()) {

            sql.append(" AND country ILIKE ?");
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

    @Override
    public Addresses searchById(int id) {

        String sql =
                "SELECT * FROM addresses " +
                        "WHERE id = ? AND is_active = true";

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    public List<Addresses> searchByCity(String city) {

        String sql =
                "SELECT * FROM addresses " +
                        "WHERE city ILIKE ? AND is_active = true";

        return executeQueryList(
                sql,
                pstmt -> pstmt.setString(1, city),
                "Erro ao buscar endereços por cidade."
        );
    }

    public List<Addresses> searchByState(String state) {

        String sql =
                "SELECT * FROM addresses " +
                        "WHERE state ILIKE ? AND is_active = true";

        return executeQueryList(
                sql,
                pstmt -> pstmt.setString(1, state),
                "Erro ao buscar endereços por estado."
        );
    }

    public List<Addresses> searchByCountry(String country) {

        String sql =
                "SELECT * FROM addresses " +
                        "WHERE country ILIKE ? AND is_active = true";

        return executeQueryList(
                sql,
                pstmt -> pstmt.setString(1, country),
                "Erro ao buscar endereços por país."
        );
    }

    @Override
    public boolean register(Addresses address) {

        if (!isValid(address)) {
            return false;
        }

        String sql =
                "INSERT INTO addresses " +
                        "(zip_code, street, number, complement, neighborhood, city, state, country) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return executeUpdate(
                sql,
                pstmt -> bindAddress(pstmt, address),
                "Erro ao cadastrar endereço."
        );
    }

    public int registerReturningId(Addresses address) {

        if (!isValid(address)) {
            return -1;
        }

        String sql =
                "INSERT INTO addresses " +
                        "(zip_code, street, number, complement, neighborhood, city, state, country) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                        "RETURNING id";

        try (Connection conn = requireConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

    private void bindAddress(
            PreparedStatement pstmt,
            Addresses address
    ) throws SQLException {

        pstmt.setString(1, address.getZipCode());
        pstmt.setString(2, address.getStreet());
        pstmt.setString(3, address.getNumber());
        pstmt.setString(4, address.getComplement());
        pstmt.setString(5, address.getNeighborhood());
        pstmt.setString(6, address.getCity());
        pstmt.setString(7, address.getState());
        pstmt.setString(8, address.getCountry());
    }

    @Override
    public boolean delete(int id) {

        String sql =
                "UPDATE addresses " +
                        "SET is_active = false, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        return executeUpdate(
                sql,
                pstmt -> pstmt.setInt(1, id),
                "Erro ao desativar endereço."
        );
    }

    @Override
    public boolean update(Addresses address, int id) {

        if (!isValid(address)) {
            return false;
        }

        String sql =
                "UPDATE addresses SET " +
                        "zip_code = ?, " +
                        "street = ?, " +
                        "number = ?, " +
                        "complement = ?, " +
                        "neighborhood = ?, " +
                        "city = ?, " +
                        "state = ?, " +
                        "country = ?, " +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";

        return executeUpdate(
                sql,
                pstmt -> {
                    bindAddress(pstmt, address);
                    pstmt.setInt(9, id);
                },
                "Erro ao atualizar endereço."
        );
    }
}
