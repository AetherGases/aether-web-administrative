package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.dao.PermissionGroupsDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.filter.PermissionGroupsFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.StatusEmployee;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.validation.Validators;
import com.example.servelet1ano.filter.EmployeesFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@WebServlet("/gerente")
public class ManagerRegisterServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ManagerRegisterServlet.class.getName());

    private static final String CADASTRO = "/ManagerRegister.jsp";

    /*
     * GRUPO DE PERMISSÃO DO GERENTE
     *
     * Nome do grupo de administrador da empresa e ids das permissões
     * (tabela permissions) que todo gerente precisa ter:
     *
     * 1 - user:analytics:visibility:high
     * 2 - inventory:analysis
     * 3 - inventory:register
     * 4 - inventory:generate
     * 5 - calculator:gases
     * 6 - calculator:roi
     * 7 - chatbot
     */

    private static final String GRUPO_ADMIN = "Admin da empresa";

    private static final List<Integer> PERMISSOES_ADMIN =
            List.of(1, 2, 3, 4, 5, 6, 7);

    private static final int MAX_NOME = 150;
    private static final int MAX_EMAIL = 255;
    private static final int MAX_TELEFONE = 20;
    private static final int MAX_CPF = 14;
    private static final int MAX_SENHA = 1024;
    private static final int MAX_EMPRESA_ID = 10;

    private static final Set<String> CAMPOS_PERMITIDOS = Set.of(
            "campoNome",
            "campoEmail",
            "campoTelefone",
            "campoSenha",
            "campoConfirmarSenha",
            "campoCPF",
            "empresaSelecionada",
            "csrfToken"
    );

    private static final Pattern CARACTERES_CONTROLE =
            Pattern.compile("[\\p{Cc}\\p{Cf}]");




    /*
     *
     * EXIBIÇÃO DO FORMULÁRIO
     *
     *
     * Antes de encaminhar a requisição ao JSP, o Servlet consulta
     * as empresas disponíveis e disponibiliza a lista por meio do
     * atributo "empresas".
     *
     * Caso o cadastro tenha sido concluído e o usuário tenha sido
     * redirecionado para esta página, o parâmetro "sucesso" permite
     * exibir uma mensagem de confirmação.
     *
     */

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(true);

        session.setAttribute(
                "csrfToken",
                UUID.randomUUID().toString()
        );

        try {
            carregarEmpresas(request);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar empresas para o formulário de cadastro.",
                    e
            );

            request.setAttribute(
                    "erro",
                    "Não foi possível carregar as empresas. Tente novamente mais tarde."
            );
        }

        if ("1".equals(request.getParameter("sucesso"))) {
            request.setAttribute(
                    "sucesso",
                    "Cadastro realizado com sucesso!"
            );
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }


    /*
     * PROCESSAMENTO DO CADASTRO
     *
     * O método doPost recebe os dados enviados pelo formulário.
     *
     * Quando uma validação falha, o método exibirErro encaminha
     * a requisição ao formulário com uma mensagem apropriada.
     *
     */

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        for (String campo : request.getParameterMap().keySet()) {

            if (!CAMPOS_PERMITIDOS.contains(campo)) {
                exibirErro(
                        request,
                        response,
                        "A requisição contém campos não permitidos."
                );
                return;
            }
        }

        for (String campo : CAMPOS_PERMITIDOS) {

            String[] valores = request.getParameterValues(campo);

            if (valores != null && valores.length != 1) {
                exibirErro(
                        request,
                        response,
                        "A requisição contém parâmetros duplicados."
                );
                return;
            }
        }

        HttpSession session = request.getSession(false);

        String tokenSessao = session == null
                ? null
                : (String) session.getAttribute("csrfToken");

        String tokenRecebido = request.getParameter("csrfToken");

        if (!tokenValido(tokenSessao, tokenRecebido)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String nome = request.getParameter("campoNome");
        String email = request.getParameter("campoEmail");
        String telefone = request.getParameter("campoTelefone");
        String senha = request.getParameter("campoSenha");
        String confirmarSenha = request.getParameter("campoConfirmarSenha");
        String empresaParam = request.getParameter("empresaSelecionada");
        String cpf = request.getParameter("campoCPF");

        request.setAttribute("campoNome", nome);
        request.setAttribute("campoEmail", email);
        request.setAttribute("campoTelefone", telefone);
        request.setAttribute("empresaSelecionada", empresaParam);
        request.setAttribute("campoCPF", cpf);


        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || telefone == null || telefone.isBlank()
                || senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()
                || empresaParam == null || empresaParam.isBlank()
                || cpf == null || cpf.isBlank()) {

            exibirErro(
                    request,
                    response,
                    "Todos os campos são obrigatórios."
            );
            return;
        }

        nome = nome.trim();
        email = email.trim();
        telefone = telefone.trim();
        empresaParam = empresaParam.trim();
        cpf = cpf.trim();

        if (nome.length() > MAX_NOME
                || email.length() > MAX_EMAIL
                || telefone.length() > MAX_TELEFONE
                || cpf.length() > MAX_CPF
                || senha.length() > MAX_SENHA
                || confirmarSenha.length() > MAX_SENHA
                || empresaParam.length() > MAX_EMPRESA_ID) {

            exibirErro(
                    request,
                    response,
                    "Um ou mais campos ultrapassaram o tamanho permitido."
            );
            return;
        }

        if (possuiCaracteresDeControle(nome)
                || possuiCaracteresDeControle(email)
                || possuiCaracteresDeControle(telefone)
                || possuiCaracteresDeControle(cpf)
                || possuiCaracteresDeControle(senha)
                || possuiCaracteresDeControle(confirmarSenha)
                || possuiCaracteresDeControle(empresaParam)) {

            exibirErro(
                    request,
                    response,
                    "Um ou mais campos contêm caracteres não permitidos."
            );
            return;
        }


        /*
         * VALIDAÇÃO DE DADOS
         *
         * Verifica email, cpf e telefone de acordo com os validators
         * Verifica senha de acordo com uma validação básica (1024 caracteres)
         *
         */


        nome = Normalizer.normalize(
                nome.strip(),
                Normalizer.Form.NFC
        );

        if (nome.length() < 2
                || !nome.matches("[\\p{L}\\p{M} .'-]+")) {

            exibirErro(
                    request,
                    response,
                    "Informe um nome válido."
            );
            return;
        }

        email = email.strip().toLowerCase(Locale.ROOT);

        if (!Validators.isEmail(email)) {
            exibirErro(
                    request,
                    response,
                    "O e-mail informado não é válido."
            );
            return;
        }

        telefone = Validators.normalizePhone(telefone);

        if (!telefone.matches("[0-9]{11}")
                || !Validators.isMobilePhone(telefone)) {

            exibirErro(
                    request,
                    response,
                    "O telefone digitado não é válido. Verifique seus dados."
            );
            return;
        }


        if (!cpf.matches("[0-9.\\-/]+")
                || !Validators.isCpf(cpf)) {

            exibirErro(
                    request,
                    response,
                    "O CPF digitado não é válido. Verifique seus dados."
            );
            return;
        }

        cpf = cpf.strip();

        empresaParam = empresaParam.strip();

        if (!senha.equals(confirmarSenha)) {
            exibirErro(
                    request,
                    response,
                    "As senhas informadas não coincidem."
            );
            return;
        }


        final int companyId;

        try {
            companyId = Integer.parseInt(empresaParam);

            if (companyId <= 0) {
                throw new NumberFormatException(
                        "O identificador da empresa deve ser positivo."
                );
            }

        } catch (NumberFormatException e) {
            exibirErro(
                    request,
                    response,
                    "A empresa selecionada não é válida. Selecione uma opção disponível."
            );
            return;
        }

        try {
            CompaniesDAO companiesDAO = new CompaniesDAO();

            Companies empresaSelecionada =
                    companiesDAO.searchById(companyId);

            if (empresaSelecionada == null) {
                exibirErro(
                        request,
                        response,
                        "A empresa selecionada não foi encontrada ou não está disponível."
                );
                return;
            }

            StatusEmployee statusGerente = StatusEmployee.ACTIVE;

            String senhaHash = PasswordHasher.hash(senha);


            /*
             * GRUPO DE PERMISSÃO DO GERENTE
             *
             * Procura o grupo "Admin da empresa" da empresa escolhida.
             * Se ainda não existir, cria o grupo já com as 7 permissões
             * (a busca por nome do DAO é parcial, por isso o nome é
             * conferido aqui para achar o grupo exato).
             */

            PermissionGroupsDAO permissionGroupsDAO = new PermissionGroupsDAO();

            PermissionGroupsFilter groupFilter = new PermissionGroupsFilter();
            groupFilter.setCompanyId(companyId);
            groupFilter.setName(GRUPO_ADMIN);

            int permissionGroupId = -1;

            List<PermissionGroups> grupos =
                    permissionGroupsDAO.searchAll(groupFilter);

            if (grupos != null) {

                for (PermissionGroups grupo : grupos) {

                    if (grupo != null
                            && grupo.getName() != null
                            && GRUPO_ADMIN.equalsIgnoreCase(grupo.getName())
                            && grupo.getCompanyId() == companyId) {

                        permissionGroupId = grupo.getId();
                        break;
                    }
                }
            }

            if (permissionGroupId <= 0) {
                permissionGroupId = permissionGroupsDAO.registerWithPermissions(
                        new PermissionGroups(GRUPO_ADMIN, companyId),
                        PERMISSOES_ADMIN
                );

                if (permissionGroupId <= 0) {
                    exibirErro(
                            request,
                            response,
                            "Não foi possível criar o grupo de permissão do gerente."
                    );
                    return;
                }
            }

            Employees employee = new Employees(
                    companyId,
                    permissionGroupId,
                    cpf,
                    nome,
                    email,
                    telefone,
                    senhaHash,
                    statusGerente
            );

            EmployeesDAO employeesDAO = new EmployeesDAO();

            boolean cadastro = employeesDAO.register(employee);

            if (!cadastro) {
                exibirErro(
                        request,
                        response,
                        "Não foi possível cadastrar o funcionário."
                );
                return;
            }


            EmployeesFilter filterEmployee = new EmployeesFilter();
            filterEmployee.setEmail(email);
            filterEmployee.setCompanyId(companyId);
            filterEmployee.setName(nome);

            List<Employees> employees = employeesDAO.searchAll(filterEmployee);

            if (employees == null || employees.size() != 1) {
                throw new RuntimeException(
                        "Não foi possível recuperar o funcionário cadastrado."
                );
            }

            HttpSession sessao = request.getSession(false);

            if (sessao == null) {
                throw new RuntimeException(
                        "A sessão do usuário não está disponível."
                );
            }

            request.changeSessionId();

            sessao.removeAttribute("csrfToken");

            sessao.setAttribute(
                    "employeeId",
                    employees.getFirst().getId()
            );

            sessao.setAttribute("companyId", companyId);

            response.sendRedirect(
                    request.getContextPath() + "/dashboard"
            );

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao processar o cadastro do gerente.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    "Não foi possível concluir seu cadastro. Tente novamente mais tarde."
            );
        }
    }


    /*
     * CARREGAMENTO DAS EMPRESAS
     *
     * Consulta as empresas disponíveis e disponibiliza a lista
     * ao JSP pelo atributo "empresas".
     *
     * Caso a consulta falhe, a exceção é propagada para o método
     * responsável por tratar a falha.
     */

    private void carregarEmpresas(HttpServletRequest request) {

        CompaniesDAO companiesDAO = new CompaniesDAO();

        List<Companies> empresas =
                companiesDAO.searchAll(new CompaniesFilter());

        request.setAttribute("empresas", empresas);
    }


    /*
     * EXIBIÇÃO DE ERROS
     *
     * Recebe a mensagem que deverá ser apresentada ao usuário,
     * disponibiliza-a pelo atributo "erro".
     *
     * O forward encaminha a requisição atual ao JSP, permitindo
     * que a mensagem seja exibida na mesma página do formulário.
     */

    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensagem
    ) throws ServletException, IOException {

        request.setAttribute("erro", mensagem);

        try {
            carregarEmpresas(request);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar empresas para o formulário.",
                    e
            );
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }

    private boolean tokenValido(String esperado, String recebido) {

        if (esperado == null || recebido == null) {
            return false;
        }

        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                recebido.getBytes(StandardCharsets.UTF_8)
        );
    }

    private boolean possuiCaracteresDeControle(String valor) {
        return valor != null
                && CARACTERES_CONTROLE.matcher(valor).find();
    }
}