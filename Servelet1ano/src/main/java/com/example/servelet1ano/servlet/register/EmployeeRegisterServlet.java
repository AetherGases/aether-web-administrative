package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.dao.PermissionGroupsDAO;
import com.example.servelet1ano.dao.SectorsDAO;
import com.example.servelet1ano.dao.UnitsDAO;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.filter.PermissionGroupsFilter;
import com.example.servelet1ano.filter.SectorsFilter;
import com.example.servelet1ano.filter.UnitsFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.StatusEmployee;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.validation.Validators;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet da tela de funcionarios (/funcionarios)
 * Mostra o formulario com as unidades e os setores da empresa e cadastra novos funcionarios
 */
@WebServlet("/funcionarios")
public class EmployeeRegisterServlet extends HttpServlet {

    /**
     * Registro de logs usado para anotar os erros inesperados
     */
    private static final Logger LOGGER =
            Logger.getLogger(EmployeeRegisterServlet.class.getName());

    /**
     * Pagina da tela de funcionarios
     */
    private static final String CADASTRO = "/Employee.jsp";

    /**
     * Nome do grupo de permissao do funcionario comum
     */
    private static final String GRUPO_FUNCIONARIO = "Funcionário da empresa";

    /**
     * Ids das permissoes (tabela permissions) que o funcionario comum recebe:
     * 3 - inventory:register
     */
    private static final List<Integer> PERMISSOES_FUNCIONARIO =
            List.of(3);

    /**
     * metodo que mostra o formulario de cadastro de funcionarios
     * Se nao houver sessao valida, redireciona para o login
     * Busca a empresa da sessao e carrega as unidades e os setores dela
     * Se vier o parametro sucesso=true, mostra a mensagem de dados carregados
     * @param request A requisicao (a empresa vem da sessao)
     * @param response A resposta que encaminha para o formulario ou redireciona para o login
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer)
                || !(session.getAttribute("companyId") instanceof Integer)) {

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }

        int companyId = (Integer) session.getAttribute("companyId");

        try {
            CompaniesDAO companiesDAO = new CompaniesDAO();
            Companies empresa = companiesDAO.searchById(companyId);

            if (empresa == null) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "A empresa vinculada ao usuário não foi encontrada."
                );
                return;
            }

            request.setAttribute("empresa", empresa);

            carregarUnidades(request, companyId);
            carregarSetores(request, companyId);

            if ("true".equals(request.getParameter("sucesso"))) {
                request.setAttribute(
                        "sucesso",
                        "Dados carregados com sucesso."
                );
            }

            request.getRequestDispatcher(CADASTRO)
                    .forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar os dados do formulário.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    companyId,
                    "Não foi possível carregar os dados da empresa."
            );
        }
    }

    /**
     * metodo que cadastra um novo funcionario na empresa do usuario logado
     * Se nao houver sessao valida, redireciona para o login
     * Confere os campos obrigatorios, e-mail, CPF, celular, senhas, unidade, setor e status
     * Confere se a unidade e o setor sao da empresa e se ja nao existe funcionario com o mesmo CPF
     * Procura o grupo "Funcionário da empresa" (se nao existir, cria com as permissoes do funcionario) e cadastra o funcionario nele
     * Se algo estiver errado, volta para o formulario com a mensagem de erro (mantendo o que foi digitado)
     * Se der certo, redireciona para /funcionarios com o aviso de sucesso
     * @param request A requisicao com os dados do funcionario (campoNomeFuncionario, campoEmail, campoTelefone, campoCPF, unitId, sectorId, campoSenha, campoConfirmarSenha e status)
     * @param response A resposta que redireciona ou volta para o formulario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer)
                || !(session.getAttribute("companyId") instanceof Integer)) {

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }


        int companyId = (Integer) session.getAttribute("companyId");

        String nome = request.getParameter("campoNomeFuncionario");
        String email = request.getParameter("campoEmail");
        String telefone = request.getParameter("campoTelefone");
        String cpf = request.getParameter("campoCPF");
        String unitIdParam = request.getParameter("unitId");
        String sectorIdParam = request.getParameter("sectorId");
        String senha = request.getParameter("campoSenha");
        String confirmarSenha = request.getParameter("campoConfirmarSenha");
        String status = request.getParameter("status");


        request.setAttribute("campoNomeFuncionario", nome);
        request.setAttribute("campoEmail", email);
        request.setAttribute("campoTelefone", telefone);
        request.setAttribute("campoCPF", cpf);
        request.setAttribute("campoUnidade", unitIdParam);
        request.setAttribute("campoSetor", sectorIdParam);
        request.setAttribute("campoStatus", status);


        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || telefone == null || telefone.isBlank()
                || cpf == null || cpf.isBlank()
                || unitIdParam == null || unitIdParam.isBlank()
                || sectorIdParam == null || sectorIdParam.isBlank()
                || senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()
                || status == null || status.isBlank()) {

            exibirErro(
                    request,
                    response,
                    companyId,
                    "Todos os campos são obrigatórios."
            );
            return;
        }

        nome = nome.trim();
        email = email.trim();
        telefone = telefone.trim();
        cpf = cpf.trim();
        unitIdParam = unitIdParam.trim();
        sectorIdParam = sectorIdParam.trim();
        status = status.trim();


        if (nome.length() > 255) {
            exibirErro(
                    request, response, companyId,
                    "O nome informado é muito longo."
            );
            return;
        }

        if (email.length() > 255 || !Validators.isEmail(email)) {
            exibirErro(
                    request, response, companyId,
                    "O e-mail digitado não é válido."
            );
            return;
        }

        if (!Validators.isCpf(cpf)) {
            exibirErro(
                    request, response, companyId,
                    "O CPF digitado não é válido."
            );
            return;
        }

        if (!Validators.isMobilePhone(telefone)) {
            exibirErro(
                    request, response, companyId,
                    "O telefone digitado não é válido."
            );
            return;
        }

        if (senha.length() > 1024
                || confirmarSenha.length() > 1024) {
            exibirErro(
                    request, response, companyId,
                    "As senhas devem ter no máximo 1024 caracteres."
            );
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            exibirErro(
                    request, response, companyId,
                    "As senhas não conferem."
            );
            return;
        }


        final int unitId;
        final int sectorId;

        try {
            unitId = Integer.parseInt(unitIdParam);
            sectorId = Integer.parseInt(sectorIdParam);

            if (unitId <= 0 || sectorId <= 0) {
                throw new NumberFormatException(
                        "Os IDs devem ser positivos."
                );
            }

        } catch (NumberFormatException e) {
            exibirErro(
                    request,
                    response,
                    companyId,
                    "Selecione uma unidade e um setor válidos."
            );
            return;
        }

        StatusEmployee statusEmployee;


        switch (status) {
            case "Ativo":
                statusEmployee = StatusEmployee.ACTIVE;
                break;

            case "Inativo":
                statusEmployee = StatusEmployee.INACTIVE;
                break;

            case "Em recesso":
                statusEmployee = StatusEmployee.IN_VACATION;
                break;

            default:
                exibirErro(
                        request,
                        response,
                        companyId,
                        "Selecione um status válido."
                );
                return;
        }

        try {

            UnitsFilter unitsFilter = new UnitsFilter();
            unitsFilter.setCompanyId(companyId);
            unitsFilter.setId(unitId);

            List<Units> units = new UnitsDAO().searchAll(unitsFilter);

            if (units == null || units.isEmpty()) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "A unidade selecionada não pertence à empresa."
                );
                return;
            }


            SectorsFilter sectorsFilter = new SectorsFilter();
            sectorsFilter.setCompanyId(companyId);
            sectorsFilter.setUnitId(unitId);
            sectorsFilter.setId(sectorId);

            List<Sectors> sectors =
                    new SectorsDAO().searchAll(sectorsFilter);

            if (sectors == null || sectors.isEmpty()) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "O setor selecionado não pertence à unidade escolhida."
                );
                return;
            }


            EmployeesDAO employeesDAO = new EmployeesDAO();

            EmployeesFilter employeesFilter = new EmployeesFilter();
            employeesFilter.setCompanyId(companyId);
            employeesFilter.setCpf(cpf);

            List<Employees> existingEmployees =
                    employeesDAO.searchAll(employeesFilter);

            if (existingEmployees != null
                    && !existingEmployees.isEmpty()) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "Já existe um funcionário com esse CPF na empresa."
                );
                return;
            }


            String senhaHash = PasswordHasher.hash(senha);


            // grupo de permissão do funcionário
            // procura o grupo "Funcionário da empresa" da empresa da sessão
            // se ainda não existir, cria o grupo já com as permissões
            // (a busca por nome do DAO é parcial, por isso o nome é
            // conferido aqui para achar o grupo exato)

            PermissionGroupsDAO permissionGroupsDAO = new PermissionGroupsDAO();

            PermissionGroupsFilter groupFilter = new PermissionGroupsFilter();
            groupFilter.setCompanyId(companyId);
            groupFilter.setName(GRUPO_FUNCIONARIO);

            int permissionGroupId = -1;

            for (PermissionGroups grupo : permissionGroupsDAO.searchAll(groupFilter)) {
                if (GRUPO_FUNCIONARIO.equalsIgnoreCase(grupo.getName())) {
                    permissionGroupId = grupo.getId();
                    break;
                }
            }

            if (permissionGroupId <= 0) {
                permissionGroupId = permissionGroupsDAO.registerWithPermissions(
                        new PermissionGroups(GRUPO_FUNCIONARIO, companyId),
                        PERMISSOES_FUNCIONARIO
                );

                if (permissionGroupId <= 0) {
                    exibirErro(
                            request,
                            response,
                            companyId,
                            "Não foi possível criar o grupo de permissão do funcionário."
                    );
                    return;
                }
            }

            Employees employee = new Employees(
                    companyId,
                    permissionGroupId,
                    sectorId,
                    cpf,
                    nome,
                    email,
                    telefone,
                    senhaHash,
                    statusEmployee
            );

            employee.setUnitId(unitId);

            boolean cadastro = employeesDAO.register(employee);

            if (!cadastro) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "Não foi possível cadastrar o funcionário. Verifique se o CPF ou o e-mail já estão cadastrados."
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/funcionarios?sucesso=true"
            );
            return;

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao cadastrar o funcionário.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    companyId,
                    "Não foi possível concluir o cadastro. Tente novamente."
            );
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Carrega as unidades ativas da empresa
     * Guarda a lista no atributo "unidades"
     * @param request A requisicao onde a lista sera guardada
     * @param companyId O id da empresa do usuario logado
     */
    private void carregarUnidades(
            HttpServletRequest request,
            int companyId
    ) {
        UnitsFilter filtro = new UnitsFilter();
        filtro.setCompanyId(companyId);

        List<Units> unidades =
                new UnitsDAO().searchAll(filtro);

        request.setAttribute("unidades", unidades);
    }

    /**
     * Carrega os setores da empresa
     * Guarda a lista no atributo "setores"
     * Se a consulta falhar, a excecao e repassada para quem chamou tratar o erro
     * @param request A requisicao onde a lista sera guardada
     * @param companyId O id da empresa do usuario logado
     */
    private void carregarSetores(
            HttpServletRequest request,
            int companyId
    ) {
        SectorsFilter filtro = new SectorsFilter();
        filtro.setCompanyId(companyId);

        List<Sectors> setores =
                new SectorsDAO().searchAll(filtro);

        request.setAttribute("setores", setores);
    }

    /**
     * Mostra uma mensagem de erro no formulario de funcionarios
     * Guarda a mensagem no atributo "erro", recarrega a empresa, as unidades e os setores
     * e encaminha a mesma requisicao de volta para a pagina, assim o que o usuario digitou continua preenchido
     * Se nao for possivel recarregar os dados, as listas de unidades e setores ficam vazias
     * @param request A requisicao atual
     * @param response A resposta que encaminha para o formulario
     * @param companyId O id da empresa do usuario logado
     * @param mensagem O texto do erro que sera mostrado ao usuario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId,
            String mensagem
    ) throws ServletException, IOException {

        request.setAttribute("erro", mensagem);

        try {
            Companies empresa =
                    new CompaniesDAO().searchById(companyId);

            request.setAttribute("empresa", empresa);
            carregarUnidades(request, companyId);
            carregarSetores(request, companyId);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao recarregar os dados do formulário.",
                    e
            );

            request.setAttribute("unidades", List.of());
            request.setAttribute("setores", List.of());
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }
}