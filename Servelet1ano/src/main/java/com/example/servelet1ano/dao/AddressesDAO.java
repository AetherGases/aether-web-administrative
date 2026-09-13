package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Addresses;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AddressesDAO implements DAOI<Addresses> {

    /**
     * Metodo que busca todos os enderecos ativos
     * @return List<Addresses> Lista dos enderecos
     */
    @Override
    public List<Addresses> searchAll() {

        List<Addresses> addresses = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from addresses where is_active = true"
                )
        ) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                addresses.add(
                        new Addresses(
                                rs.getInt("id"),
                                rs.getString("street"),
                                rs.getString("number"),
                                rs.getString("complement"),
                                rs.getString("city"),
                                rs.getString("state"),
                                rs.getString("country")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return addresses;
        }
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do endereco
     * @return Retorna o endereco encontrado
     */
    @Override
    public Addresses searchById(int id) {

        Addresses address = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from addresses where id = ? and is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                address = new Addresses(
                        rs.getInt("id"),
                        rs.getString("street"),
                        rs.getString("number"),
                        rs.getString("complement"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("country")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return address;
        }
    }

    /**
     * metodo para buscar enderecos pela cidade (somente ativos)
     * @param city A cidade do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCity(String city) {

        List<Addresses> addresses = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from addresses where city ilike ? and is_active = true"
                )
        ) {

            pstmt.setString(1, city);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                addresses.add(
                        new Addresses(
                                rs.getInt("id"),
                                rs.getString("street"),
                                rs.getString("number"),
                                rs.getString("complement"),
                                rs.getString("city"),
                                rs.getString("state"),
                                rs.getString("country")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return addresses;
        }
    }

    /**
     * metodo para buscar enderecos pelo estado (somente ativos)
     * @param state O estado do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByState(String state) {

        List<Addresses> addresses = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from addresses where state ilike ? and is_active = true"
                )
        ) {

            pstmt.setString(1, state);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                addresses.add(
                        new Addresses(
                                rs.getInt("id"),
                                rs.getString("street"),
                                rs.getString("number"),
                                rs.getString("complement"),
                                rs.getString("city"),
                                rs.getString("state"),
                                rs.getString("country")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return addresses;
        }
    }

    /**
     * metodo para buscar enderecos pelo pais (somente ativos)
     * @param country O pais do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCountry(String country) {

        List<Addresses> addresses = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from addresses where country ilike ? and is_active = true"
                )
        ) {

            pstmt.setString(1, country);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                addresses.add(
                        new Addresses(
                                rs.getInt("id"),
                                rs.getString("street"),
                                rs.getString("number"),
                                rs.getString("complement"),
                                rs.getString("city"),
                                rs.getString("state"),
                                rs.getString("country")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return addresses;
        }
    }

    /**
     * metodo para cadastrar novos enderecos no banco
     * @param address O endereco que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(Addresses address) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into addresses (street, number, complement, city, state, country) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

            pstmt.setString(1, address.getStreet());
            pstmt.setString(2, address.getNumber());
            pstmt.setString(3, address.getComplement());
            pstmt.setString(4, address.getCity());
            pstmt.setString(5, address.getState());
            pstmt.setString(6, address.getCountry());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar o endereco por id (is_active = false)
     * @param id Valor unico de cada endereco
     * @return true se foi desativado e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update addresses set is_active = false, updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do endereco selecionado pelo id
     * @param address Os valores do endereco para ser atualizado
     * @param id O valor unico do endereco que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(Addresses address, int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update addresses set street = ?, " +
                                "number = ?, " +
                                "complement = ?, " +
                                "city = ?, " +
                                "state = ?, " +
                                "country = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setString(1, address.getStreet());
            pstmt.setString(2, address.getNumber());
            pstmt.setString(3, address.getComplement());
            pstmt.setString(4, address.getCity());
            pstmt.setString(5, address.getState());
            pstmt.setString(6, address.getCountry());
            pstmt.setInt(7, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}