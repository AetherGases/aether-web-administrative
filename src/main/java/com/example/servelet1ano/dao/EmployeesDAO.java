package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.StatusEmployee;
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
        Sectors sector = null;

        StringBuilder sql = new StringBuilder(
                "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, e.sector_id as sectorId, " +
                        "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                        "u.name as unitName, u.id as idUnit, sec.id as idSector, sec.name as sectorName " +
                        "from employees e " +
                        "join companies c on e.company_id = c.id " +
                        "join permission_groups pg on e.permission_group_id = pg.id " +
                        "join units u on u.id = e.unit_id " +
                        "join sectors sec on sec.id = e.sector_id " +
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

        if (filter.getCpf() != null && !filter.getCpf().isBlank()) {
            sql.append(" and e.cpf = ?");
            parametros.add(filter.getCpf());
        }

        if (filter.getStatus() != null) {
            sql.append(" and e.status = ?::status_employee");
            parametros.add(filter.getStatus().getValor());
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

        if (filter.getSectorId() != null) {
            sql.append(" and sec.id = ?");
            parametros.add(filter.getSectorId());
        }

        if (filter.getSectorName() != null && !filter.getSectorName().isBlank()) {
            sql.append(" and sec.name ilike ?");
            parametros.add("%" + filter.getSectorName() + "%");
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

                sector = new Sectors(
                        rs.getInt("idSector"),
                        rs.getString("sectorName")
                );

                Employees employee = new Employees(
                        rs.getInt("id"),
                        rs.getInt("companyId"),
                        rs.getInt("permissionGroupId"),
                        rs.getInt("unitId"),
                        rs.getInt("sectorId"),
                        rs.getString("cpf"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        StatusEmployee.fromValor(rs.getString("status")),
                        permissionGroup,
                        company,
                        unit,
                        sector
                );

                employee.setPasswordHash(rs.getString("password_hash"));
                employee.setStorageFileId(rs.getInt("storage_file_id"));
                employee.setCreatedAt(rs.getTimestamp("created_at"));
                employee.setUpdatedAt(rs.getTimestamp("updated_at"));

                employees.add(employee);
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
        Sectors sector = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, e.sector_id as sectorId, " +
                                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                                "u.name as unitName, u.id as idUnit, sec.id as idSector, sec.name as sectorName " +
                                "from employees e " +
                                "join permission_groups pg on pg.id = e.permission_group_id " +
                                "join companies c on e.company_id = c.id " +
                                "join units u on u.id = e.unit_id " +
                                "join sectors sec on sec.id = e.sector_id " +
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

                sector = new Sectors(
                        rs.getInt("idSector"),
                        rs.getString("sectorName")
                );

                employee = new Employees(
                        rs.getInt("id"),
                        rs.getInt("companyId"),
                        rs.getInt("permissionGroupId"),
                        rs.getInt("unitId"),
                        rs.getInt("sectorId"),
                        rs.getString("cpf"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        StatusEmployee.fromValor(rs.getString("status")),
                        permissionGroup,
                        company,
                        unit,
                        sector
                );

                employee.setPasswordHash(rs.getString("password_hash"));
                employee.setStorageFileId(rs.getInt("storage_file_id"));
                employee.setCreatedAt(rs.getTimestamp("created_at"));
                employee.setUpdatedAt(rs.getTimestamp("updated_at"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employee;
        }
    }

    /**
     * metodo para buscar o empregado pelo cpf (somente ativos)
     * O cpf é unico no banco, entao devolve só um registro
     * @param cpf O cpf do empregado, somente com os digitos
     * @return Retorna o empregado encontrado
     */
    public Employees searchByCpf(String cpf) {

        Employees employee = null;
        PermissionGroups permissionGroup = null;
        Companies company = null;
        Units unit = null;
        Sectors sector = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select e.*, e.company_id as companyId, e.permission_group_id as permissionGroupId, e.unit_id as unitId, e.sector_id as sectorId, " +
                                "c.name as companyName, c.id as idCompany, pg.id as idPermissionGroup, pg.name as permissionGroup, " +
                                "u.name as unitName, u.id as idUnit, sec.id as idSector, sec.name as sectorName " +
                                "from employees e " +
                                "join permission_groups pg on pg.id = e.permission_group_id " +
                                "join companies c on e.company_id = c.id " +
                                "join units u on u.id = e.unit_id " +
                                "join sectors sec on sec.id = e.sector_id " +
                                "where e.cpf = ? and e.is_active = true"
                )
        ) {

            pstmt.setString(1, cpf);

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

                sector = new Sectors(
                        rs.getInt("idSector"),
                        rs.getString("sectorName")
                );

                employee = new Employees(
                        rs.getInt("id"),
                        rs.getInt("companyId"),
                        rs.getInt("permissionGroupId"),
                        rs.getInt("unitId"),
                        rs.getInt("sectorId"),
                        rs.getString("cpf"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        StatusEmployee.fromValor(rs.getString("status")),
                        permissionGroup,
                        company,
                        unit,
                        sector
                );

                employee.setPasswordHash(rs.getString("password_hash"));
                employee.setStorageFileId(rs.getInt("storage_file_id"));
                employee.setCreatedAt(rs.getTimestamp("created_at"));
                employee.setUpdatedAt(rs.getTimestamp("updated_at"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return employee;
        }
    }

    /**
     * metodo para cadastrar novos empregados no banco (sempre ativo na criacao)
     * A senha ja deve vir como hash no passwordHash (gerado pelo PasswordHasher)
     * @param employee O funcionario que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    @Override
    public boolean register(Employees employee) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, employee.getPasswordHash());
            pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

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
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {

            for (int i = 0; i < employees.size(); i++) {
                Employees employee = employees.get(i);

                pstmt.setInt(1, employee.getCompanyId());
                pstmt.setInt(2, employee.getPermissionGroupId());
                pstmt.setInt(3, employee.getUnitId());
                pstmt.setInt(4, employee.getSectorId());
                pstmt.setString(5, employee.getCpf());
                pstmt.setString(6, employee.getName());
                pstmt.setString(7, employee.getEmail());
                pstmt.setString(8, employee.getPhone());
                pstmt.setString(9, employee.getPasswordHash());
                pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

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
     * metodo para desativar o empregado por id (is_active = false, status = 'INACTIVE')
     * @param id Valor unico de cada empregado
     * @return true se foi desativado e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set is_active = false, status = 'INACTIVE', updated_at = current_timestamp where id = ?"
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
                        "update employees set is_active = false, status = 'INACTIVE', updated_at = current_timestamp where company_id = ?"
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
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where unit_id = ?"
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
                        "update employees e set status = 'INACTIVE', is_active = false, updated_at = current_timestamp " +
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
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where name ilike ?"
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
     * metodo que desativa pelo cpf
     * @param cpf O cpf do funcionario, somente com os digitos
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean deleteByCpf(String cpf) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where cpf = ?"
                )
        ) {

            pstmt.setString(1, cpf);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values do empregado selecionado pelo id
     * Nao altera a senha (use updatePasswordHash) nem o status (use os metodos de status)
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
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setInt(9, id);

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
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, name);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario, somente com os digitos
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByCpf(Employees employee, String cpf) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where cpf = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, cpf);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que atualiza a senha (hash) do empregado pelo id
     * O passwordHash ja deve vir pronto (gerado pelo PasswordHasher)
     * @param id O id do empregado
     * @param passwordHash O hash da nova senha
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updatePasswordHash(int id, String passwordHash) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set password_hash = ?, updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setString(1, passwordHash);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusOnVacation(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'IN_VACATION', updated_at = current_timestamp where id = ?"
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
     * Metodo que altera o status do empregado para 'ACTIVE'
     * @param id O id do empregado
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateStatusActive(int id) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'ACTIVE', updated_at = current_timestamp where id = ?"
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
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
                )
        ) {
            if (unitId != employee.getUnitId()) {
                return false;
            }

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, employee.getPasswordHash());
            pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

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
                        "insert into employees (company_id, permission_group_id, unit_id, sector_id, cpf, name, email, phone, password_hash, status) " +
                                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::status_employee)"
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
                pstmt.setInt(4, employee.getSectorId());
                pstmt.setString(5, employee.getCpf());
                pstmt.setString(6, employee.getName());
                pstmt.setString(7, employee.getEmail());
                pstmt.setString(8, employee.getPhone());
                pstmt.setString(9, employee.getPasswordHash());
                pstmt.setString(10, StatusEmployee.ACTIVE.getValor());

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
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where id = ?"
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
     * metodo para desativar o empregado pelo cpf que esta na unidade
     * @param cpf O cpf do empregado, somente com os digitos
     * @param unitId Id da unidade para validacao
     * @return true se foi desativado e false caso tenha dado erro
     */
    public boolean deleteByCpfPerUnit(String cpf, int unitId) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set status = 'INACTIVE', is_active = false, updated_at = current_timestamp where cpf = ?"
                )
        ) {

            Employees employee = searchByCpf(cpf);

            if (employee == null || unitId != employee.getUnitId()) {
                return false;
            }

            pstmt.setString(1, cpf);

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
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setInt(9, id);

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
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where name ilike ? and unit_id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, name);
            pstmt.setInt(10, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que muda os values do empregado selecionado pelo cpf numa unidade especifica
     * @param employee Os valores do empregado para ser atualizado
     * @param cpf O cpf para achar o funcionario, somente com os digitos
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updateByCpfPerUnit(Employees employee, String cpf, int unitId) {

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set company_id = ?, " +
                                "permission_group_id = ?, " +
                                "unit_id = ?, " +
                                "sector_id = ?, " +
                                "cpf = ?, " +
                                "name = ?, " +
                                "email = ?, " +
                                "phone = ?, " +
                                "updated_at = current_timestamp " +
                                "where cpf = ? and unit_id = ?"
                )
        ) {

            pstmt.setInt(1, employee.getCompanyId());
            pstmt.setInt(2, employee.getPermissionGroupId());
            pstmt.setInt(3, employee.getUnitId());
            pstmt.setInt(4, employee.getSectorId());
            pstmt.setString(5, employee.getCpf());
            pstmt.setString(6, employee.getName());
            pstmt.setString(7, employee.getEmail());
            pstmt.setString(8, employee.getPhone());
            pstmt.setString(9, cpf);
            pstmt.setInt(10, unitId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que atualiza a senha (hash) do empregado pelo id numa unidade especifica
     * @param id O id do empregado
     * @param passwordHash O hash da nova senha
     * @param unitId O id da unidade para validacao
     * @return true se foi atualizado e false caso tenha dado erro
     */
    public boolean updatePasswordHashPerUnit(int id, String passwordHash, int unitId) {
        Employees employee = searchById(id);

        if (employee == null || unitId != employee.getUnitId()) {
            return false;
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update employees set password_hash = ?, updated_at = current_timestamp where id = ?"
                )
        ) {

            pstmt.setString(1, passwordHash);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Metodo que altera o status do empregado para 'IN_VACATION' (scoped per unit)
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
                        "update employees set status = 'IN_VACATION', updated_at = current_timestamp where id = ?"
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
     * Metodo que altera o status do empregado para 'ACTIVE' (scoped per unit)
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
                        "update employees set status = 'ACTIVE', updated_at = current_timestamp where id = ?"
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