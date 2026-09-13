package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.PermissionGroups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionGroupsDAO implements DAOI<PermissionGroups> {

    /**
     * Metodo que busca todos os grupos de permissao ativos
     * @return List<PermissionGroups> Lista dos grupos de permissao
     */
    @Override
    public List<PermissionGroups> searchAll(){

        List<PermissionGroups> permissionGroups = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from permission_groups where is_active = true"
                )
        ){

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){

                permissionGroups.add(
                        new PermissionGroups(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            return permissionGroups;
        }
    }

    /**
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do grupo de permissao
     * @return Retorna o grupo de permissao encontrado
     */
    @Override
    public PermissionGroups searchById(int id){

        PermissionGroups permissionGroup = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from permission_groups where id = ? and is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                permissionGroup = new PermissionGroups(
                        rs.getInt("id"),
                        rs.getString("name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return permissionGroup;
        }
    }

    /**
     * metodo para buscar pelo nome do grupo de permissao (somente ativos)
     * @param name O nome do grupo de permissao
     * @return List<PermissionGroups> com os grupos encontrados
     */
    public List<PermissionGroups> searchByName(String name){

        List<PermissionGroups> permissionGroups = new ArrayList<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select * from permission_groups where name ilike ? and is_active = true"
                )
        ){

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                permissionGroups.add(
                        new PermissionGroups(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return permissionGroups;
        }
    }

    /**
     * metodo para cadastrar novos grupos de permissao no banco
     * @param permissionGroup O grupo de permissao que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(PermissionGroups permissionGroup){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into permission_groups (name) values (?)"
                )
        ){

            pstmt.setString(1, permissionGroup.getName());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para registrar mais de um grupo de permissao
     * @param permissionGroups Uma lista de grupos de permissao
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<PermissionGroups> permissionGroups){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into permission_groups (name) values (?)"
                )
        ){

            for (int i = 0; i < permissionGroups.size(); i++) {
                PermissionGroups permissionGroup = permissionGroups.get(i);

                pstmt.setString(1, permissionGroup.getName());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar o grupo de permissao por id (is_active = false)
     * @param id Valor unico de cada grupo de permissao
     * @return true se foi desativado e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update permission_groups set is_active = false, updated_at = current_timestamp where id = ?"
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
     * metodo que desativa pelo nome
     * @param name O nome do grupo de permissao
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByName(String name){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update permission_groups set is_active = false, updated_at = current_timestamp where name ilike ?"
                )
        ) {

            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do grupo de permissao selecionado pelo id
     * @param permissionGroup Os valores do grupo de permissao para ser atualizado
     * @param id O valor unico do grupo de permissao que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(PermissionGroups permissionGroup, int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update permission_groups set name = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setString(1, permissionGroup.getName());
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do grupo de permissao selecionado pelo nome
     * @param permissionGroup Os valores do grupo de permissao para ser atualizado
     * @param name O nome para achar o grupo de permissao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByName(PermissionGroups permissionGroup, String name){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update permission_groups set name = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ){

            pstmt.setString(1, permissionGroup.getName());
            pstmt.setString(2, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}