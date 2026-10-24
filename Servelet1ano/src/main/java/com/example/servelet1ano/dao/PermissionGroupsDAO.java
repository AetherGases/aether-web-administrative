package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.PermissionGroupsFilter;
import com.example.servelet1ano.model.PermissionGroups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionGroupsDAO implements DAOI<PermissionGroups, PermissionGroupsFilter> {

    /**
     * Metodo que busca os grupos de permissao ativos aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<PermissionGroups> com os grupos encontrados
     */
    @Override
    public List<PermissionGroups> searchAll(PermissionGroupsFilter filter){

        List<PermissionGroups> permissionGroups = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select * from permission_groups where is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getCompanyId() == null) {
            sql.append(" and company_id is null");
        } else {
            sql.append(" and company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getId() != null) {
            sql.append(" and id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and name ilike ?");
            parametros.add("%" + filter.getName() + "%");
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){

                permissionGroups.add(
                        new PermissionGroups(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getObject("company_id", Integer.class)
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("ERRO searchAll permission_groups: " + e.getMessage());
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
                        rs.getString("name"),
                        rs.getObject("company_id", Integer.class)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return permissionGroup;
        }
    }

    /**
     * metodo para cadastrar novos grupos de permissao no banco
     * O companyId pode vir nulo, quando o grupo for global (da Aether)
     * @param permissionGroup O grupo de permissao que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(PermissionGroups permissionGroup){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into permission_groups (name, company_id) values (?, ?)"
                )
        ){

            pstmt.setString(1, permissionGroup.getName());
            pstmt.setObject(2, permissionGroup.getCompanyId());

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
                        "insert into permission_groups (name, company_id) values (?, ?)"
                )
        ){

            for (int i = 0; i < permissionGroups.size(); i++) {
                PermissionGroups permissionGroup = permissionGroups.get(i);

                pstmt.setString(1, permissionGroup.getName());
                pstmt.setObject(2, permissionGroup.getCompanyId());

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
     * metodo que desativa os grupos de permissao de uma empresa (para quando uma empresa for desativada)
     * @param companyId O id da empresa
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByCompanyId(int companyId){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update permission_groups set is_active = false, updated_at = current_timestamp where company_id = ?"
                )
        ) {

            pstmt.setInt(1, companyId);

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
                                "company_id = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setString(1, permissionGroup.getName());
            pstmt.setObject(2, permissionGroup.getCompanyId());
            pstmt.setInt(3, id);

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
                                "company_id = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ){

            pstmt.setString(1, permissionGroup.getName());
            pstmt.setObject(2, permissionGroup.getCompanyId());
            pstmt.setString(3, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}