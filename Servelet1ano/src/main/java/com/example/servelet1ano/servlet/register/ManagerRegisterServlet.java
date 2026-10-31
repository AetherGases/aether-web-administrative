package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.dao.PermissionGroupsDAO;
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

/**
 * Servlet do cadastro do gerente da empresa (/gerente)
 * Cadastra o gerente no grupo de administrador da empresa e ja inicia a sessao dele
 */
@WebServlet("/gerente")
public class ManagerRegisterServlet extends HttpServlet {

    /**
     * Registro de logs usado para anotar os erros inesperados
     */
    private static final Logger LOGGER = Logger.getLogger(ManagerRegisterServlet.class.getName());

    /**
     * Pagina do formulario de cadastro do gerente
     */
    private static final String CADASTRO = "/ManagerRegister.jsp";

    /**
     * Nome do grupo de administrador da empresa, usado pelo gerente
     */
    private static final String GRUPO_ADMIN = "Admin da empresa";

    /**
     * Ids das permissoes (tabela permissions) que todo gerente precisa ter:
     * 1 - user:analytics:visibility:high
     * 2 - inventory:analysis
     * 3 - inventory:register
     * 4 - inventory:generate
     * 5 - calculator:gases
     * 6 - calculator:roi
     * 7 - chatbot
     */
    private static final List<Integer> PERMISSOES_ADMIN =
            List.of(1, 2, 3, 4, 5, 6, 7);

    /**
     * Tamanho maximo do nome
     */
    private static final int MAX_NOME = 150;

    /**
     * Tamanho maximo do e-mail
     */
    private static final int MAX_EMAIL = 255;

    /**
     * Tamanho maximo do telefone (como foi digitado)
     */
    private static final int MAX_TELEFONE = 20;

    /**
     * Tamanho maximo do CPF (como foi digitado, com pontuacao)
     */
    private static final int MAX_CPF = 14;

    /**
     * Tamanho maximo da senha e da confirmacao
     */
    private static final int MAX_SENHA = 1024;

    /**
     * Nomes dos unicos campos que o formulario pode enviar
     * Qualquer outro campo faz a requisicao ser recusada
     */
    private static final Set<String> CAMPOS_PERMITIDOS = Set.of(
            "campoNome",
            "campoEmail",
            "campoTelefone",
            "campoSenha",
            "campoConfirmarSenha",
            "campoCPF",
            "csrfToken"
    );

    /**
     * Padrao que encontra caracteres de controle e de formatacao invisiveis, que nao sao aceitos nos campos
     */
    private static final Pattern CARACTERES_CONTROLE =
            Pattern.compile("[\\p{Cc}\\p{Cf}]");

    /**
     * metodo que mostra o formulario de cadastro do gerente
     * Cria um novo token anti-CSRF na sessao
     * Se o parametro sucesso=1 vier na url, mostra a mensagem de cadastro realizado
     * Se nao for possivel carregar a sessão, mostra a mensagem de erro no proprio formulario
     * @param request A requisicao
     * @param response A resposta que encaminha para o formulario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */

        @Override
        protected void doGet(
                HttpServletRequest request,
                HttpServletResponse response
        ) throws ServletException, IOException {

            request.setCharacterEncoding("UTF-8");

            response.setHeader("Cache-Control", "no-store");

            HttpSession session = request.getSession(false);

            if (session == null
                    || !(session.getAttribute("pendingCompanyId") instanceof Integer companyId)
                    || companyId <= 0) {

                response.sendRedirect(
                        request.getContextPath() + "/empresa"
                );
                return;
            }

            try {
                CompaniesDAO companiesDAO = new CompaniesDAO();

                Companies empresa = companiesDAO.searchById(companyId);

                if (empresa == null) {
                    session.removeAttribute("pendingCompanyId");

                    response.sendRedirect(
                            request.getContextPath() + "/empresa"
                    );
                    return;
                }

                request.setAttribute("empresaCadastrada", empresa);

                session.setAttribute(
                        "csrfToken",
                        UUID.randomUUID().toString()
                );

                request.getRequestDispatcher(CADASTRO)
                        .forward(request, response);

            } catch (RuntimeException e) {

                LOGGER.log(
                        Level.SEVERE,
                        "Erro ao carregar cadastro do gerente.",
                        e
                );

                request.setAttribute(
                        "erro",
                        "Não foi possível carregar o cadastro do gerente."
                );

                request.getRequestDispatcher(CADASTRO)
                        .forward(request, response);
            }
        }


        /**
         * metodo que cadastra o gerente da empresa e inicia a sessao dele
         * Confere os campos permitidos, parametros duplicados e o token anti-CSRF (se o token for invalido responde 403)
         * Depois valida tamanho e caracteres de cada campo, nome, e-mail, telefone, CPF, senha e empresa
         * Procura o grupo "Admin da empresa" da empresa escolhida (se nao existir, cria com as 7 permissoes) e cadastra o gerente nele
         * Se algo estiver errado, volta para o formulario com a mensagem de erro (mantendo o que foi digitado)
         * Se der certo, troca o id da sessao, guarda employeeId e companyId nela e redireciona para /dashboard
         * @param request A requisicao com os dados do gerente (campoNome, campoEmail, campoTelefone, campoSenha, campoConfirmarSenha, campoCPF, empresaSelecionada e csrfToken)
         * @param response A resposta que redireciona para o dashboard ou volta para o formulario
         * @throws ServletException se der erro no encaminhamento para a pagina
         * @throws IOException se der erro de entrada ou saida
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

        Object empresaPendente = session == null
                ? null
                : session.getAttribute("pendingCompanyId");

        if (!(empresaPendente instanceof Integer companyId)
                || companyId <= 0) {

            exibirErro(
                    request,
                    response,
                    "Sua sessão de cadastro expirou. "
                            + "Inicie novamente o processo."
            );
            return;
        }

        if (!tokenValido(tokenSessao, tokenRecebido)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String nome = request.getParameter("campoNome");
        String email = request.getParameter("campoEmail");
        String telefone = request.getParameter("campoTelefone");
        String senha = request.getParameter("campoSenha");
        String confirmarSenha = request.getParameter("campoConfirmarSenha");
        String cpf = request.getParameter("campoCPF");

        request.setAttribute("campoNome", nome);
        request.setAttribute("campoEmail", email);
        request.setAttribute("campoTelefone", telefone);
        request.setAttribute("campoCPF", cpf);


        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || telefone == null || telefone.isBlank()
                || senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()
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
        cpf = cpf.trim();

        if (nome.length() > MAX_NOME
                || email.length() > MAX_EMAIL
                || telefone.length() > MAX_TELEFONE
                || cpf.length() > MAX_CPF
                || senha.length() > MAX_SENHA
                || confirmarSenha.length() > MAX_SENHA) {

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
                || possuiCaracteresDeControle(confirmarSenha)) {

            exibirErro(
                    request,
                    response,
                    "Um ou mais campos contêm caracteres não permitidos."
            );
            return;
        }


        // validação de dados
        // verifica email, cpf e telefone de acordo com os validators
        // verifica senha de acordo com uma validação básica (1024 caracteres)


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

        if (!senha.equals(confirmarSenha)) {
            exibirErro(
                    request,
                    response,
                    "As senhas informadas não coincidem."
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


            // grupo de permissão do gerente
            // procura o grupo "Admin da empresa" da empresa escolhida
            // se ainda não existir, cria o grupo já com as 7 permissões
            // Busca o grupo administrativo da empresa pelo nome exato.
            PermissionGroupsDAO permissionGroupsDAO =
                    new PermissionGroupsDAO();

            PermissionGroups grupo =
                    permissionGroupsDAO.searchByNameAndCompany(
                            GRUPO_ADMIN,
                            companyId
                    );

            int permissionGroupId =
                    grupo == null ? -1 : grupo.getId();

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


            Employees gerenteCadastrado =
                    employeesDAO.searchByEmailAndCompany(email, companyId);

            if (gerenteCadastrado == null) {
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

            sessao.invalidate();

            HttpSession sessaoAutenticada = request.getSession(true);
            request.changeSessionId();


            sessaoAutenticada.setAttribute(
                    "employeeId",
                    gerenteCadastrado.getId()
            );

            sessaoAutenticada.setAttribute(
                    "companyId",
                    companyId
            );

            sessaoAutenticada.setAttribute(
                    "permissionGroupId",
                    permissionGroupId
            );

            sessaoAutenticada.setAttribute(
                    "unitId",
                    gerenteCadastrado.getUnitId()
            );

            sessaoAutenticada.setAttribute(
                    "sectorId",
                    gerenteCadastrado.getSectorId()
            );

            sessaoAutenticada.setMaxInactiveInterval(3600);

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

    //    ------------- Metodos auxiliares --------------

    /**
     * Mostra uma mensagem de erro no formulario de cadastro do gerente
     * Guarda a mensagem no atributo "erro", recarrega a lista de empresas
     * e encaminha a mesma requisicao de volta para a pagina, assim o que o usuario digitou continua preenchido
     * @param request A requisicao atual
     * @param response A resposta que encaminha para o formulario
     * @param mensagem O texto do erro que sera mostrado ao usuario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensagem
    ) throws ServletException, IOException {

        request.setAttribute("erro", mensagem);

        HttpSession session = request.getSession(false);

        if (session != null) {

            Object valor = session.getAttribute("pendingCompanyId");

            if (valor instanceof Integer companyId && companyId > 0) {

                try {
                    CompaniesDAO companiesDAO = new CompaniesDAO();

                    Companies empresa =
                            companiesDAO.searchById(companyId);

                    request.setAttribute(
                            "empresaCadastrada",
                            empresa
                    );

                } catch (RuntimeException e) {

                    LOGGER.log(
                            Level.SEVERE,
                            "Erro ao recuperar empresa durante exibição de erro.",
                            e
                    );
                }
            }
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }

    /**
     * Compara o token anti-CSRF da sessao com o que veio no formulario
     * A comparacao leva sempre o mesmo tempo, para nao dar pistas sobre o token
     * @param esperado O token guardado na sessao
     * @param recebido O token enviado pelo formulario
     * @return true se os dois existem e sao iguais
     */
    private boolean tokenValido(String esperado, String recebido) {

        if (esperado == null || recebido == null) {
            return false;
        }

        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                recebido.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Confere se o texto tem caracteres de controle ou de formatacao invisiveis
     * @param valor O texto a conferir
     * @return true se encontrou algum desses caracteres (false se o valor for null)
     */
    private boolean possuiCaracteresDeControle(String valor) {
        return valor != null
                && CARACTERES_CONTROLE.matcher(valor).find();
    }
}