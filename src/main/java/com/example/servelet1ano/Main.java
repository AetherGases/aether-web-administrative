package com.example.servelet1ano;

import com.example.servelet1ano.dao.*;
import com.example.servelet1ano.filter.*;
import com.example.servelet1ano.model.*;
import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.util.PasswordHasher;

import java.sql.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String sufixo = String.valueOf(System.currentTimeMillis());

        // cpf tem CHAR(11): pego os 11 ultimos digitos do sufixo pra ficar unico e no tamanho certo
        String cpfTeste = sufixo.substring(sufixo.length() - 11);

        // cnpj da empresa tem CHAR(14): "0" + sufixo (13 digitos) = 14 caracteres
        String cnpjTeste = "0" + sufixo;

        // ===================== ADDRESSES =====================
        System.out.println("===== ADDRESSES =====");

        AddressesDAO addressesDAO = new AddressesDAO();

        Addresses novoEndereco = new Addresses(
                0, "01001000", "Rua Teste", "100", "Sala 1", "Centro", "Cidade" + sufixo, "SP", "Brasil"
        );
        addressesDAO.register(novoEndereco);

        // register() nao devolve o id gerado -- buscamos de volta pelo filtro,
        // usando um valor unico (a cidade com sufixo) pra garantir que so existe um resultado
        AddressesFilter addressFilter = new AddressesFilter();
        addressFilter.setCity("Cidade" + sufixo);

        int addressId = addressesDAO.searchAll(addressFilter).get(0).getId();
        System.out.println("Endereco criado, id: " + addressId);

        // ===================== PERMISSION GROUPS =====================
        System.out.println("\n===== PERMISSION GROUPS =====");

        PermissionGroupsDAO permissionGroupsDAO = new PermissionGroupsDAO();

        PermissionGroups novoGrupo = new PermissionGroups(0, "Grupo" + sufixo);
        permissionGroupsDAO.register(novoGrupo);

        PermissionGroupsFilter permissionGroupFilter = new PermissionGroupsFilter();
        permissionGroupFilter.setName("Grupo" + sufixo);

        int permissionGroupId = permissionGroupsDAO.searchAll(permissionGroupFilter).get(0).getId();
        System.out.println("Grupo de permissao criado, id: " + permissionGroupId);

        // ===================== COMPANIES =====================
        System.out.println("\n===== COMPANIES =====");

        CompaniesDAO companiesDAO = new CompaniesDAO();

        Companies novaEmpresa = new Companies(
                0, addressId, "Empresa" + sufixo, CompanySize.PEQUENO,
                new java.sql.Date(System.currentTimeMillis()), cnpjTeste,
                "empresa" + sufixo + "@teste.com", null, null
        );
        novaEmpresa.setTradeName("Fantasia" + sufixo);
        companiesDAO.register(novaEmpresa);

        CompaniesFilter companyFilter = new CompaniesFilter();
        companyFilter.setName("Empresa" + sufixo);

        int companyId = companiesDAO.searchAll(companyFilter).get(0).getId();
        System.out.println("Empresa criada, id: " + companyId);

        // ===================== UNITS =====================
        System.out.println("\n===== UNITS =====");

        UnitsDAO unitsDAO = new UnitsDAO();

        Units novaUnidade = new Units(
                0, companyId, addressId, "Unidade" + sufixo,
                "12345678000199", "6201-5/01", true, null, null
        );
        unitsDAO.register(novaUnidade);

        UnitsFilter unitFilter = new UnitsFilter();
        unitFilter.setName("Unidade" + sufixo);

        int unitId = unitsDAO.searchAll(unitFilter).get(0).getId();
        System.out.println("Unidade criada, id: " + unitId);

        // ===================== SECTORS =====================
        // criado antes do funcionario porque employees.sector_id é NOT NULL
        System.out.println("\n===== SECTORS =====");

        SectorsDAO sectorsDAO = new SectorsDAO();

        Sectors novoSetor = new Sectors(
                0, unitId, companyId, "Setor" + sufixo, null, null, null
        );
        sectorsDAO.register(novoSetor);

        SectorsFilter sectorFilter = new SectorsFilter();
        sectorFilter.setName("Setor" + sufixo);

        int sectorId = sectorsDAO.searchAll(sectorFilter).get(0).getId();
        System.out.println("Setor criado, id: " + sectorId);

        // ===================== EMPLOYEES =====================
        System.out.println("\n===== EMPLOYEES =====");

        EmployeesDAO employeesDAO = new EmployeesDAO();

        Employees novoFuncionario = new Employees();
        novoFuncionario.setCompanyId(companyId);
        novoFuncionario.setPermissionGroupId(permissionGroupId);
        novoFuncionario.setUnitId(unitId);
        novoFuncionario.setSectorId(sectorId);
        novoFuncionario.setCpf(cpfTeste);
        novoFuncionario.setName("Funcionario" + sufixo);
        novoFuncionario.setEmail("funcionario" + sufixo + "@teste.com");
        novoFuncionario.setPhone("11988887777");
        novoFuncionario.setPasswordHash(PasswordHasher.hash("senha123"));

        employeesDAO.register(novoFuncionario);

        EmployeesFilter employeeFilter = new EmployeesFilter();
        employeeFilter.setName("Funcionario" + sufixo);

        int employeeId = employeesDAO.searchAll(employeeFilter).get(0).getId();
        System.out.println("Funcionario criado, id: " + employeeId);

        // ===================== TELEPHONE COMPANIES =====================
        System.out.println("\n===== TELEPHONE COMPANIES =====");

        TelephoneCompaniesDAO telephoneCompaniesDAO = new TelephoneCompaniesDAO();

        TelephoneCompanies novoTelefone = new TelephoneCompanies(
                "11999999999", companyId, 0, null
        );
        telephoneCompaniesDAO.register(novoTelefone);

        TelephoneCompaniesFilter telephoneFilter = new TelephoneCompaniesFilter();
        telephoneFilter.setCompanyId(companyId);

        List<TelephoneCompanies> telefonesEncontrados = telephoneCompaniesDAO.searchAll(telephoneFilter);
        System.out.println("Telefones da empresa: " + telefonesEncontrados.size());

        // ===================== PLANS (nao existe PlansDAO, insere direto so pra teste) =====================
        System.out.println("\n===== PLANS (insercao direta, sem DAO) =====");

        int planId = 0;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into plans (name, description, price, duration_days) values (?, ?, ?, ?)"
                )
        ) {
            pstmt.setString(1, "Plano" + sufixo);
            pstmt.setString(2, "Plano de teste");
            pstmt.setBigDecimal(3, new java.math.BigDecimal("99.90"));
            pstmt.setInt(4, 30);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select id from plans where name = ?"
                )
        ) {
            pstmt.setString(1, "Plano" + sufixo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                planId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Plano criado, id: " + planId);

        // ===================== SUBSCRIPTIONS =====================
        System.out.println("\n===== SUBSCRIPTIONS =====");

        SubscriptionsDAO subscriptionsDAO = new SubscriptionsDAO();

        Subscriptions novaAssinatura = new Subscriptions(
                0, companyId, planId, true, false, (Companies)null
        );
        subscriptionsDAO.register(novaAssinatura);

        SubscriptionsFilter subscriptionFilter = new SubscriptionsFilter();
        subscriptionFilter.setCompanyId(companyId);

        int subscriptionId = subscriptionsDAO.searchAll(subscriptionFilter).get(0).getId();
        System.out.println("Assinatura criada, id: " + subscriptionId);

        // ===================== FILTRO COMBINADO =====================
        System.out.println("\n===== TESTE: filtro combinado (name + companyId) =====");

        EmployeesFilter filtroCombinado = new EmployeesFilter();
        filtroCombinado.setName("Funcionario" + sufixo);
        filtroCombinado.setCompanyId(companyId);

        List<Employees> resultadoCombinado = employeesDAO.searchAll(filtroCombinado);
        System.out.println("Encontrados com os dois filtros juntos: " + resultadoCombinado.size());

        // ===================== UPDATE =====================
        System.out.println("\n===== TESTE: update =====");

        Employees funcionarioAtualizado = employeesDAO.searchById(employeeId);
        funcionarioAtualizado.setName("Funcionario" + sufixo + "_Atualizado");

        boolean atualizou = employeesDAO.update(funcionarioAtualizado, employeeId);
        System.out.println("Update deu certo: " + atualizou);

        Employees confereUpdate = employeesDAO.searchById(employeeId);
        System.out.println("Nome depois do update: " + confereUpdate.getName());

        // ===================== DELETE (desativacao) =====================
        System.out.println("\n===== TESTE: delete (soft delete) =====");

        boolean deletou = employeesDAO.delete(employeeId);
        System.out.println("Delete deu certo: " + deletou);

        Employees confereDelete = employeesDAO.searchById(employeeId);
        System.out.println("searchById depois do delete (deve ser null): " + confereDelete);
    }
}