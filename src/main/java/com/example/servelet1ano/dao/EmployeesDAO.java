package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.Units;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeesDAO implements DAOI<Employees, EmployeesFilter> {

    /**
     * Metodo que busca os funcionarios ativos aplicando os filtros informados
     * Os campos do filter que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Usado tanto pra busca geral (admin Aether) quanto pra busca por unidade (admin da unidade, preenchendo unitId)
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Employees> com os empregados encontrados
     */
    @Override
    public List<Employees> searchAll(EmployeesFilter filter) {

        List<Employees> employees = new ArrayList<>();
        Companies company = null;
        PermissionGroups permissionGroup = null;
        Units unit = null;

        StringBuilder sql = new StringBuilder(
                "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                        "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                        "u.name as unitName, u.id as idUnit " +
                        "from employees e " +
                        "join companies c on e.company_id = c.id " +
                        "join permission_groups pg on e.permission_group_id = pg.id " +
                        "join units u on u.id = e.unit_id " +
                        "where e.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and e.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            sql.append(" and e.name ilike ?");
            parametros.add("%" + filter.getName() + "%");
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and e.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        if (filter.getCompanyName() != null && !filter.getCompanyName().isBlank()) {
            sql.append(" and c.name ilike ?");
            parametros.add("%" + filter.getCompanyName() + "%");
        }

        if (filter.getUnitId() != null) {
            sql.append(" and u.id = ?");
            parametros.add(filter.getUnitId());
        }

        if (filter.getUnitName() != null && !filter.getUnitName().isBlank()) {
            sql.append(" and u.name ilike ?");
            parametros.add("%" + filter.getUnitName() + "%");
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

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
     * metodo para buscar por Id (somente ativos)
     * @param id Numero unico do empregado
     * @return Retorna o empregado encontrado
     */
    @Override
    public Employees searchById(int id) {

        Employees employee = null;
        PermissionGroups permissionGroup = null;
        Companies company = null;
        Units unit = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, " +
                                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                                "u.name as unitName, u.id as idUnit " +
                                "from employees e " +
                                "join permission_groups pg on pg.id = e.permission_group_id " +
                                "join companies c on e.company_id = c.id " +
                                "join units u on u.id = e.unit_id " +
                                "where e.id = ? and e.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
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
     * @param employee O funcionario que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(Employees employee) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                                "values (?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {

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
    public boolean registerLot(List<Employees> employees) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

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
     * metodo para desativar o empregado por id (is_active = false, status = 'dismissed')
     * @param id Valor unico de cada empregado
     * @return true se foi desativado e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set is_active = false, status = 'dismissed', updated_at = current_timestamp where id = ?"
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
     * metodo que desativa por id da empresa (para quando uma empresa for desativada)
     * @param idCompany
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByIdCompany(int idCompany) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set is_active = false, status = 'dismissed', updated_at = current_timestamp where company_id = ?"
                )
        ) {

            pstmt.setInt(1, idCompany);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa os empregados quando uma unidade for desativada
     * @param unitId numero unico da unidade
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByIdUnit(int unitId) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'dismissed', is_active = false, updated_at = current_timestamp where unit_id = ?"
                )
        ) {

            pstmt.setInt(1, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa os empregados de uma unidade pelo nome
     * @param name O nome da unidade
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByNameUnit(String name) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees e set status = 'dismissed', is_active = false, updated_at = current_timestamp " +
                                "from units u " +
                                "where e.unit_id = u.id and u.name ilike ?"
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
     * metodo que desativa pelo nome
     * @param name O nome do funcionario
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByName(String name) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'dismissed', is_active = false, updated_at = current_timestamp where name ilike ?"
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
     * metodo que muda os values do empregado selecionado pelo id
     * @param employee Os valores do empregado para ser atualizado
     * @param id O valor unico do empregado que quer atualizar
     * @return true se foi mudado e false caso tenha dado erro
     */
    @Override
    public boolean update(Employees employee, int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "email = ?, " +
                                "name = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

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
    public boolean updateByName(Employees employee, String name) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "email = ?, " +
                                "name = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ) {

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
    public boolean updateStatusOnVacation(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'on vacation', updated_at = current_timestamp where id = ?"
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
     * Metodo que altera o status do empregado para 'on leave'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnLeave(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'on leave', updated_at = current_timestamp where id = ?"
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
     * Metodo que altera o status do empregado para 'active'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActive(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'active', updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


//    ------------- Metodos específicos para admins das units --------------

    /**
     * metodo para cadastrar novos empregados no banco
     * @param employee O funcionario que sera cadastrado na unidade
     * @param unitId numero da unidade
     * @return true se foi registrado e false caso tenha dado erro
     */
    public boolean registerPerUnit(Employees employee, int unitId) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {
            if (unitId != employee.getUnitId()) {
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
    public boolean registerLotPerUnit(List<Employees> employees, int unitId) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, email, name, status) " +
                                "values (?, ?, ?, ?, ?, ?)"
                )
        ) {

            for (int i = 0; i < employees.size(); i++) {
                Employees employee = employees.get(i);

                if (unitId != employee.getUnitId()) {
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
     * metodo para desativar o empregado por id que esta na unidade
     * @param id Valor unico de cada empregado
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByIdPerUnit(int id, int unitId) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'dismissed', is_active = false, updated_at = current_timestamp where id = ?"
                )
        ) {

            Employees employee = searchById(id);

            if (employee == null || unitId != employee.getUnitId()) {
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
    public boolean updateByIdPerUnit(Employees employee, int id, int unitId) {
        Employees employeeFound = searchById(id);

        if (employeeFound == null || unitId != employeeFound.getUnitId()) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "email = ?, " +
                                "name = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

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
    public boolean updateByNamePerUnit(Employees employee, String name, int unitId) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "email = ?, " +
                                "name = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ? and unit_id = ?"
                )
        ) {

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
    public boolean updateStatusOnVacationPerUnit(int id, int unitId) {
        Employees employee = searchById(id);

        if (employee == null || unitId != employee.getUnitId()) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'on vacation', updated_at = current_timestamp where id = ?"
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
     * Metodo que altera o status do empregado para 'on leave' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnLeavePerUnit(int id, int unitId) {
        Employees employee = searchById(id);

        if (employee == null || unitId != employee.getUnitId()) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'on leave', updated_at = current_timestamp where id = ?"
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
     * Metodo que altera o status do empregado para 'active' (scoped per unit)
     * @param id O id do empregado
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActivePerUnit(int id, int unitId) {
        Employees employee = searchById(id);

        if (employee == null || unitId != employee.getUnitId()) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'active', updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}