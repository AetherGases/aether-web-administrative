package com.example.servelet1ano;

import com.example.servelet1ano.dao.*;
import com.example.servelet1ano.filter.*;
import com.example.servelet1ano.model.*;

import java.sql.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String sufixo = String.valueOf(System.currentTimeMillis());

        // ===================== ADDRESSES =====================
        System.out.println("===== ADDRESSES =====");

        AddressesDAO addressesDAO = new AddressesDAO();

        Addresses novoEndereco = new Addresses(
                0, "Rua Teste", "100", "Sala 1", "Cidade" + sufixo, "SP", "Brasil"
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
                0, addressId, "Empresa" + sufixo, 50,
                new Date(System.currentTimeMillis()), "12345678900010",
                "empresa" + sufixo + "@teste.com", null, null
        );
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

        // ===================== EMPLOYEES =====================
        System.out.println("\n===== EMPLOYEES =====");

        EmployeesDAO employeesDAO = new EmployeesDAO();

        Employees novoFuncionario = new Employees();
        novoFuncionario.setCompanyId(companyId);
        novoFuncionario.setPermissionGroupId(permissionGroupId);
        novoFuncionario.setUnitId(unitId);
        novoFuncionario.setName("Funcionario" + sufixo);
        novoFuncionario.setEmail("funcionario" + sufixo + "@teste.com");

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

        // ===================== SECTORS =====================
        System.out.println("\n===== SECTORS =====");

        SectorsDAO sectorsDAO = new SectorsDAO();

        Sectors novoSetor = new Sectors(
                0, unitId, companyId, "Setor" + sufixo, null, null
        );
        sectorsDAO.register(novoSetor);

        SectorsFilter sectorFilter = new SectorsFilter();
        sectorFilter.setDescription("Setor" + sufixo);

        int sectorId = sectorsDAO.searchAll(sectorFilter).get(0).getId();
        System.out.println("Setor criado, id: " + sectorId);

        // ===================== SUBSCRIPTIONS =====================
        // ATENCAO: assume que ja existe uma linha com id = 1 na tabela plans
        System.out.println("\n===== SUBSCRIPTIONS =====");

        SubscriptionsDAO subscriptionsDAO = new SubscriptionsDAO();

        Subscriptions novaAssinatura = new Subscriptions(
                0, companyId, 1, true, false, (Companies)null
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