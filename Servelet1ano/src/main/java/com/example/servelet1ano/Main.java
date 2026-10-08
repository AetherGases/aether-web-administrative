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

        // cpf: 9 digitos do sufixo + 2 DVs calculados (o Validators.isCpf confere os digitos verificadores)
        String cpfBase = sufixo.substring(sufixo.length() - 9);

        int somaCpf = 0;
        for (int i = 0; i < 9; i++) somaCpf += (cpfBase.charAt(i) - '0') * (10 - i);
        int cpfDv1 = somaCpf % 11 < 2 ? 0 : 11 - somaCpf % 11;

        somaCpf = 0;
        for (int i = 0; i < 9; i++) somaCpf += (cpfBase.charAt(i) - '0') * (11 - i);
        somaCpf += cpfDv1 * 2;
        int cpfDv2 = somaCpf % 11 < 2 ? 0 : 11 - somaCpf % 11;

        String cpfTeste = cpfBase + cpfDv1 + cpfDv2;

        // cnpj: usa o sufixo pra garantir unicidade; calcula os 2 DVs pelo modulo 11
        String cnpjBase = ("000000000000" + sufixo).substring(sufixo.length());
        cnpjBase = cnpjBase.substring(0, 12);

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;
        for (int i = 0; i < 12; i++) soma += (cnpjBase.charAt(i) - '0') * pesos1[i];
        int dv1 = soma % 11 < 2 ? 0 : 11 - soma % 11;

        soma = 0;
        for (int i = 0; i < 12; i++) soma += (cnpjBase.charAt(i) - '0') * pesos2[i];
        soma += dv1 * pesos2[12];
        int dv2 = soma % 11 < 2 ? 0 : 11 - soma % 11;

        String cnpjTeste = cnpjBase + dv1 + dv2;

        // ===================== ADDRESSES =====================
        System.out.println("===== ADDRESSES =====");

        AddressesDAO addressesDAO = new AddressesDAO();

        Addresses novoEndereco = new Addresses(
                0, "01001000", "Rua Teste", "100", "Sala 1", "Centro", "Cidade" + sufixo, "SP", "Brasil"
        );

        boolean okEndereco = addressesDAO.register(novoEndereco);
        System.out.println("register endereco: " + okEndereco);

        AddressesFilter addressFilter = new AddressesFilter();
        addressFilter.setCity("Cidade" + sufixo);

        List<Addresses> enderecos = addressesDAO.searchAll(addressFilter);
        System.out.println("searchAll endereco size: " + enderecos.size());

        int addressId = enderecos.get(0).getId();
        System.out.println("Endereco criado, id: " + addressId);

        // ===================== PERMISSION GROUPS =====================
        System.out.println("\n===== PERMISSION GROUPS =====");

        PermissionGroupsDAO permissionGroupsDAO = new PermissionGroupsDAO();

        PermissionGroups novoGrupo = new PermissionGroups(0, "Grupo" + sufixo);

        boolean okGrupo = permissionGroupsDAO.register(novoGrupo);
        System.out.println("register grupo: " + okGrupo);

        PermissionGroupsFilter permissionGroupFilter = new PermissionGroupsFilter();
        permissionGroupFilter.setName("Grupo" + sufixo);

        List<PermissionGroups> grupos = permissionGroupsDAO.searchAll(permissionGroupFilter);
        System.out.println("searchAll grupo size: " + grupos.size());

        int permissionGroupId = grupos.get(0).getId();
        System.out.println("Grupo de permissao criado, id: " + permissionGroupId);

        // ===================== COMPANIES =====================
        System.out.println("\n===== COMPANIES =====");

        CompaniesDAO companiesDAO = new CompaniesDAO();

        Companies novaEmpresa = new Companies(
                0, addressId, "Empresa" + sufixo, "Fantasia" + sufixo, cnpjTeste,
                CompanySize.PEQUENO, new java.sql.Date(System.currentTimeMillis()),
                "empresa" + sufixo + "@teste.com", null, true, null
        );

        boolean okEmpresa = companiesDAO.register(novaEmpresa);
        System.out.println("register empresa: " + okEmpresa);

        CompaniesFilter companyFilter = new CompaniesFilter();
        companyFilter.setName("Empresa" + sufixo);

        List<Companies> empresas = companiesDAO.searchAll(companyFilter);
        System.out.println("searchAll empresa size: " + empresas.size());

        int companyId = empresas.get(0).getId();
        System.out.println("Empresa criada, id: " + companyId);

        // ===================== UNITS =====================
        System.out.println("\n===== UNITS =====");

        UnitsDAO unitsDAO = new UnitsDAO();

        Units novaUnidade = new Units(
                0, companyId, addressId, "Unidade" + sufixo,
                "12345678000199", "6201-5/01", true, null, null
        );

        boolean okUnidade = unitsDAO.register(novaUnidade);
        System.out.println("register unidade: " + okUnidade);

        UnitsFilter unitFilter = new UnitsFilter();
        unitFilter.setName("Unidade" + sufixo);

        List<Units> unidades = unitsDAO.searchAll(unitFilter);
        System.out.println("searchAll unidade size: " + unidades.size());

        int unitId = unidades.get(0).getId();
        System.out.println("Unidade criada, id: " + unitId);

        // ===================== SECTORS =====================
        // criado antes do funcionario porque employees.sector_id é NOT NULL
        System.out.println("\n===== SECTORS =====");

        SectorsDAO sectorsDAO = new SectorsDAO();

        Sectors novoSetor = new Sectors(
                0, unitId, companyId, "Setor" + sufixo, null, null, null
        );

        boolean okSetor = sectorsDAO.register(novoSetor);
        System.out.println("register setor: " + okSetor);

        SectorsFilter sectorFilter = new SectorsFilter();
        sectorFilter.setName("Setor" + sufixo);

        List<Sectors> setores = sectorsDAO.searchAll(sectorFilter);
        System.out.println("searchAll setor size: " + setores.size());

        int sectorId = setores.get(0).getId();
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

        boolean okFuncionario = employeesDAO.register(novoFuncionario);
        System.out.println("register funcionario: " + okFuncionario);

        EmployeesFilter employeeFilter = new EmployeesFilter();
        employeeFilter.setName("Funcionario" + sufixo);

        List<Employees> funcionarios = employeesDAO.searchAll(employeeFilter);
        System.out.println("searchAll funcionario size: " + funcionarios.size());

        int employeeId = funcionarios.get(0).getId();
        System.out.println("Funcionario criado, id: " + employeeId);

        // ===================== TELEPHONE COMPANIES =====================
        System.out.println("\n===== TELEPHONE COMPANIES =====");

        TelephoneCompaniesDAO telephoneCompaniesDAO = new TelephoneCompaniesDAO();

        TelephoneCompanies novoTelefone = new TelephoneCompanies(
                "11999999999", companyId, 0, null
        );

        boolean okTelefone = telephoneCompaniesDAO.register(novoTelefone);
        System.out.println("register telefone: " + okTelefone);

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

        Subscriptions novaAssinatura = new Subscriptions();
        novaAssinatura.setCompanyId(companyId);
        novaAssinatura.setPlanId(planId);
        novaAssinatura.setActive(true);
        novaAssinatura.setInstallments(false);

        boolean okAssinatura = subscriptionsDAO.register(novaAssinatura);
        System.out.println("register assinatura: " + okAssinatura);

        SubscriptionsFilter subscriptionFilter = new SubscriptionsFilter();
        subscriptionFilter.setCompanyId(companyId);

        List<Subscriptions> assinaturas = subscriptionsDAO.searchAll(subscriptionFilter);
        System.out.println("searchAll assinatura size: " + assinaturas.size());

        int subscriptionId = assinaturas.get(0).getId();
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