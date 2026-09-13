package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Addresses;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AddressesDAO {


    /**
     * Metodo que busca todos os enderecos
     *
     * @return List<Addresses> Lista dos enderecos
     */
    public List<Addresses> searchAll(){

        List<Addresses> addresses = new ArrayList<>();

        String sql = "select * from addresses";

        try (
                Connection conn = ConnectionFactory.connect();
                Statement stmt = conn.createStatement()
        ){

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()){

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
        }

        finally {
            return addresses;
        }
    }

    /**
     * metodo para buscar por Id
     * @param id Numero unico do endereco
     * @return Retorna o endereco encontrado
     */
    public Addresses searchById(int id){
        String sql = "select * from addresses where id = ?";

        Addresses address = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
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
     * metodo para buscar enderecos pela cidade
     * @param city A cidade do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCity(String city){
        String sql = "select * from addresses where city like ?";

        List<Addresses> addresses = new ArrayList<>();

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, city);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

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
     * metodo para buscar enderecos pelo estado
     * @param state O estado do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByState(String state){
        String sql = "select * from addresses where state like ?";

        List<Addresses> addresses = new ArrayList<>();

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, state);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

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
     * metodo para buscar enderecos pelo pais
     * @param country O pais do endereco
     * @return List<Addresses> com os enderecos encontrados
     */
    public List<Addresses> searchByCountry(String country){
        String sql = "select * from addresses where country like ?";

        List<Addresses> addresses = new ArrayList<>();

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, country);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

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
    public boolean register(Addresses address){

        String sql = "insert into addresses (street, number, complement, city, state, country) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

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
     * metodo para deletar o endereco por id
     * @param id Valor unico de cada endereco
     * @return true se foi deletado e false caso tenha dado erro
     */
    public boolean deleteById(int id){
        String sql = "delete from addresses where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
    public boolean updateById(Addresses address, int id){
        String sql = "update addresses set street = ?, " +
                "number = ?, " +
                "complement = ?, " +
                "city = ?, " +
                "state = ?, " +
                "country = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

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

