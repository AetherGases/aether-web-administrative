package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.Units;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeesDAO {


    /**
     * Metodo que busca todos os funcionarios
     *
     * @return List<Employees> Lista dos funcionários
     */
    public List<Employees> searchAll(){

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join companies c on e.company_id = c.id " +
                "join permission_groups pg on e.permission_group_id = pg.id " +
                "join units u on u.id = e.unit_id";

        try (
                Connection conn = ConnectionFactory.connect();
                Statement stmt = conn.createStatement()
        ){

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employees.add(
                        new Employees(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("permissionGroupId"),
                                rs.getInt("unitId"),
                                rs.getString("email"),
                                rs.getString("name"),
                                permissionGroup,
                                company,
                                unit
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            return employees;
        }
    }

    /**
     * metodo para buscar por Id
     * @param id Numero unico do empregado
     * @return Retorna o empregado encontrado
     */
    public Employees searchById(int id){
        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join permission_groups pg on pg.id = e.permission_group_id " +
                "join companies c on e.company_id = c.id " +
                "join units u on u.id = e.unit_id " +
                "where e.id = ?";

        Employees employee = null;
        PermissionGroups permissionGroup = null;
        Companies company = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employee = new Employees(
                        rs.getInt("id"),
                        rs.getInt("companyId"),
                        rs.getInt("permissionGroupId"),
                        rs.getInt("unitId"),
                        rs.getString("email"),
                        rs.getString("name"),
                        permissionGroup,
                        company,
                        unit
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employee;
        }
    }


    /**
     * metodo para buscar por Id da empresa
     * @param idCompany O valor unico de cada empresa
     * @return List<Employees> com os empregados encontrados
     */
    public List<Employees> searchByIdCompany(int idCompany){
        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join permission_groups pg on pg.id = e.permission_group_id " +
                "join companies c on e.company_id = c.id " +
                "join units u on u.id = e.unit_id " +
                "where e.company_id = ?";

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, idCompany);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employees.add(
                        new Employees(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("permissionGroupId"),
                                rs.getInt("unitId"),
                                rs.getString("email"),
                                rs.getString("name"),
                                permissionGroup,
                                company,
                                unit
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employees;
        }
    }

    /**
     * metodo para buscar os empregados pelo nome da empresa
     * @param name O nome da empresa
     * @return List<Employees> com os empregados encontrados
     */
    public List<Employees> searchByNameCompany(String name){
        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join permission_groups pg on pg.id = e.permission_group_id " +
                "join companies c on e.company_id = c.id " +
                "join units u on u.id = e.unit_id " +
                "where c.name = ?";

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employees.add(
                        new Employees(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("permissionGroupId"),
                                rs.getInt("unitId"),
                                rs.getString("email"),
                                rs.getString("name"),
                                permissionGroup,
                                company,
                                unit
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employees;
        }
    }

    /**
     * metodo para buscar os empregados pelo id da unidade
     * @param idUnit O valor unico de cada unidade
     * @return List<Employees> com os empregados encontrados
     */
    public List<Employees> searchByIdUnit(int idUnit){
        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join permission_groups pg on pg.id = e.permission_group_id " +
                "join companies c on e.company_id = c.id " +
                "join units u on u.id = e.unit_id " +
                "where u.id = ?";

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, idUnit);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employees.add(
                        new Employees(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("permissionGroupId"),
                                rs.getInt("unitId"),
                                rs.getString("email"),
                                rs.getString("name"),
                                permissionGroup,
                                company,
                                unit
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employees;
        }
    }

    /**
     * metodo para buscar os empregados pelo nome da unidade
     * @param name O nome da unidade
     * @return List<Employees> com os empregados encontrados
     */
    public List<Employees> searchByNameUnit(String name){
        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join permission_groups pg on pg.id = e.permission_group_id " +
                "join companies c on e.company_id = c.id " +
                "join units u on u.id = e.unit_id " +
                "where u.name = ?";

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employees.add(
                        new Employees(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("permissionGroupId"),
                                rs.getInt("unitId"),
                                rs.getString("email"),
                                rs.getString("name"),
                                permissionGroup,
                                company,
                                unit
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employees;
        }
    }


    /**
     * metodo para buscar por nome do empregado
     * @param name O nome do funcionario
     * @return List<Employees> com os empregados encontrados
     */
    public List<Employees> searchByName(String name){
        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join permission_groups pg on pg.id = e.permission_group_id " +
                "join companies c on e.company_id = c.id " +
                "join units u on u.id = e.unit_id " +
                "where e.name like ?";

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setString(1, name);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employees.add(
                        new Employees(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("permissionGroupId"),
                                rs.getInt("unitId"),
                                rs.getString("email"),
                                rs.getString("name"),
                                permissionGroup,
                                company,
                                unit
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employees;
        }
    }


    /**
     * metodo para cadastrar novos empregados no banco
     * @param employee O funcionario que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean register(Employees employee){

        String sql = "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getName());
            pstmt.setString(6, "active");

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * metodo para registrar mais de um empregado
     * @param employees Uma lista de empregados
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLot(List<Employees> employees){
        String sql = "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            for (int i = 0; i < employees.size(); i++) {
                Employees employee = employees.get(i);

                pstmt.setInt(1, employee.getCompanyId());
                pstmt.setInt(2, employee.getPermissionGroupId());
                pstmt.setInt(3, employee.getUnitId());
                pstmt.setString(4, employee.getEmail());
                pstmt.setString(5, employee.getName());
                pstmt.setString(6, "active");

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
     * metodo para deletar o empregado por id
     * @param id Valor unico de cada empregado
     * @return true se foi deletado e false caso tenha dado erro
     */
    public boolean deleteById(int id){
        String sql = "delete from employees where id = ?";

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
     * metodo que deleta por id da empresa (para quando uma empresa for deletada)
     * @param idCompany
     * @return
     */
    public boolean deleteByIdCompany(int idCompany){
        String sql = "delete from employees where company_id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCompany);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que marca os empregados como 'dismissed' quando uma unidade for deletada
     * @param unitId numero unico da unidade
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByIdUnit(int unitId){
        String sql = "update employees set status = 'dismissed' where unit_id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que marca como 'dismissed' os empregados de uma unidade pelo nome
     * @param name O nome da unidade
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByNameUnit(String name){
        String sql = "update employees e set status = 'dismissed' " +
                "from units u " +
                "where e.unit_id = u.id and u.name like ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que deleta pelo nome
     * @param name O nome do funcionario
     * @return true se foi deletado e false caso tenha dado erro
     */
    public boolean deleteByName(String name){
        String sql = "delete from employees where name like ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    public boolean updateById(Employees employee, int id){
        String sql = "update employees set company_id = ?, " +
                "permission_group_id = ?, " +
                "unit_id = ?, " +
                "email = ?, " +
                "name = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getName());
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Metodo que muda os values do empregado selecionado pelo nome
     * @param employee Os valores do empregado para ser atualizado
     * @param name O nome para achar o funcionario
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByName(Employees employee, String name){
        String sql = "update employees set company_id = ?, " +
                "permission_group_id = ?, " +
                "unit_id = ?, " +
                "email = ?, " +
                "name = ?, " +
                "updated_at = current_timestamp " +
                "where name like ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getName());
            pstmt.setString(6, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'on vacation'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnVacation(int id){
        String sql = "update employees set status = 'on vacation', updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'on leave'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnLeave(int id){
        String sql = "update employees set status = 'on leave', updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'active'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActive(int id){
        String sql = "update employees set status = 'active', updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


//    ------------- Metodos específicos para admins das units --------------

    /**
     * Metodo que busca todos os funcionarios da unidade do usuario
     *
     * @param unitId id unico da unidade
     * @return List<Employees> Lista dos funcionários
     */
    public List<Employees> searchAllPerUnit(int unitId){

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join companies c on e.company_id = c.id " +
                "join permission_groups pg on e.permission_group_id = pg.id " +
                "join units u on u.id = e.unit_id " +
                "where u.id = ?";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, unitId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employees.add(
                        new Employees(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("permissionGroupId"),
                                rs.getInt("unitId"),
                                rs.getString("email"),
                                rs.getString("name"),
                                permissionGroup,
                                company,
                                unit
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            return employees;
        }
    }

    /**
     * metodo para buscar por Id se estiver na unidade
     * @param id Numero unico do empregado
     * @param unitId numero unico da unidade
     * @return Retorna o empregado encontrado
     */
    public Employees searchByIdPerUnit(int id, int unitId){
        String sql = "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                "u.name as unitName, u.id as idUnit " +
                "from employees e " +
                "join permission_groups pg on pg.id = e.permission_group_id " +
                "join companies c on e.company_id = c.id " +
                "join units u on u.id = e.unit_id " +
                "where e.id = ? and u.id = ?";

        Employees employee = null;
        PermissionGroups permissionGroup = null;
        Companies company = null;
        Units unit = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, id);
            pstmt.setInt(2, unitId);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                permissionGroup = new PermissionGroups(
                        rs.getInt("idPermissionGroup"),
                        rs.getString("permissionGroup")
                );

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                unit = new Units(
                        rs.getInt("idUnit"),
                        rs.getString("unitName")
                );

                employee = new Employees(
                        rs.getInt("id"),
                        rs.getInt("companyId"),
                        rs.getInt("permissionGroupId"),
                        rs.getInt("unitId"),
                        rs.getString("email"),
                        rs.getString("name"),
                        permissionGroup,
                        company,
                        unit
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employee;
        }
    }


    /**
     * metodo para cadastrar novos empregados no banco
     * @param employee O funcionario que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerPerUnit(Employees employee, int unitId){

        String sql = "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ){
            if(unitId != employee.getUnitId()){
                return false;
            }

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getName());
            pstmt.setString(6, "active");

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * metodo para registrar mais de um empregado numa unidade especifica
     * @param employees Uma lista de empregados
     * @param unitId O id da unidade para validacao
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerLotPerUnit(List<Employees> employees, int unitId){
        String sql = "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                "values (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            for (int i = 0; i < employees.size(); i++) {
                Employees employee = employees.get(i);

                if(unitId != employee.getUnitId()){
                    return false;
                }

                pstmt.setInt(1, employee.getCompanyId());
                pstmt.setInt(2, employee.getPermissionGroupId());
                pstmt.setInt(3, employee.getUnitId());
                pstmt.setString(4, employee.getEmail());
                pstmt.setString(5, employee.getName());
                pstmt.setString(6, "active");

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
     * metodo para deletar o empregado por id que esta na unidade
     * @param id Valor unico de cada empregado
     * @param unitId Id da unidade para validacao
     * @return true se foi deletado e false caso tenha dado erro
     */
    public boolean deleteByIdPerUnit(int id, int unitId){
        String sql = "update employees set status = 'dismissed' where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Employees employee = searchById(id);

            if(employee == null || unitId != employee.getUnitId()){
                return false;
            }

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * metodo que muda os values do empregado selecionado pelo id numa unidade especifica
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @param unitId O id da unidade para validacao
     * @return true se foi mudado e false caso tenha dado erro
     */
    public boolean updateByIdPerUnit(Employees employee, int id, int unitId){
        Employees employeeFound = searchById(id);

        if(employeeFound == null || unitId != employeeFound.getUnitId()){
            return false;
        }

        String sql = "update employees set company_id = ?, " +
                "permission_group_id = ?, " +
                "unit_id = ?, " +
                "email = ?, " +
                "name = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getName());
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Metodo que muda os values do empregado selecionado pelo nome numa unidade especifica
     * @param employee Os valores do empregado para ser atualizado
     * @param name O nome para achar o funcionario
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByNamePerUnit(Employees employee, String name, int unitId){
        String sql = "update employees set company_id = ?, " +
                "permission_group_id = ?, " +
                "unit_id = ?, " +
                "email = ?, " +
                "name = ?, " +
                "updated_at = current_timestamp " +
                "where name like ? and unit_id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setString(4, employee.getEmail());
            pstmt.setString(5, employee.getName());
            pstmt.setString(6, name);
            pstmt.setInt(7, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'on vacation' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnVacationPerUnit(int id, int unitId){
        Employees employee = searchById(id);

        if(employee == null || unitId != employee.getUnitId()){
            return false;
        }

        String sql = "update employees set status = 'on vacation', updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'on leave' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnLeavePerUnit(int id, int unitId){
        Employees employee = searchById(id);

        if(employee == null || unitId != employee.getUnitId()){
            return false;
        }

        String sql = "update employees set status = 'on leave', updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'active' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActivePerUnit(int id, int unitId){
        Employees employee = searchById(id);

        if(employee == null || unitId != employee.getUnitId()){
            return false;
        }

        String sql = "update employees set status = 'active', updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
