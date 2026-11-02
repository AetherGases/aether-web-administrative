package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.util.service.AuthorizationService;
import com.example.servelet1ano.validation.Validators;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Servlet de login do site administrativo (/login).
 * Autentica a senha e permite acesso somente a gerentes ativos da empresa.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /** Registro de logs usado para anotar os erros inesperados. */
    private static final Logger LOGGER =
            Logger.getLogger(LoginServlet.class.getName());

    /** Pagina de login e destino depois da autenticacao. */
    private static final String LOGIN_PAGE = "/Login.jsp";
    private static final String DASHBOARD = "/dashboard";

    private static final int MAX_EMAIL = 255;
    private static final int MAX_SENHA = 1024;
    private static final Set<String> CAMPOS_PERMITIDOS =
            Set.of("campoEmail", "campoSenha");
    private static final Pattern CARACTERES_CONTROLE =
            Pattern.compile("[\\p{Cc}\\p{Cf}]");

    private final EmployeesDAO employeesDAO = new EmployeesDAO();
    private final AuthorizationService authorizationService =
            new AuthorizationService();

    /**
     * Exibe o login e redireciona somente sessoes ainda autorizadas no banco.
     * @param request A requisicao atual
     * @param response A resposta para o login ou dashboard
     * @throws ServletException se ocorrer erro no encaminhamento
     * @throws IOException se ocorrer erro de entrada ou saida
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        try {
            if (authorizationService.isCompanyAdmin(request)) {
                response.sendRedirect(request.getContextPath() + DASHBOARD);
                return;
            }

            HttpSession session = request.getSession(false);
            if (session != null
                    && session.getAttribute("employeeId") != null) {
                session.invalidate();
            }

            request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Erro ao validar sessao no login.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Confere os campos, autentica a senha e verifica no banco se o usuario
     * e um gerente ativo vinculado a uma empresa e grupo ativos.
     * @param request Requisicao com e-mail e senha
     * @param response Resposta para o login ou dashboard
     * @throws ServletException se ocorrer erro no encaminhamento
     * @throws IOException se ocorrer erro de entrada ou saida
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        try {
            if (!validarParametros(request)) {
                exibirErro(request, response, "Os dados enviados são inválidos.");
                return;
            }

            String email = request.getParameter("campoEmail");
            String senha = request.getParameter("campoSenha");

            if (email == null || email.isBlank()
                    || senha == null || senha.isBlank()) {
                exibirErro(request, response, "Todos os campos são obrigatórios.");
                return;
            }

            email = email.trim().toLowerCase(Locale.ROOT);
            request.setAttribute("email", email);

            if (email.length() > MAX_EMAIL
                    || senha.length() > MAX_SENHA
                    || !Validators.isEmail(email)
                    || contemCaracteresControle(email)
                    || contemCaracteresControle(senha)) {
                exibirErro(request, response, "Os dados informados são inválidos.");
                return;
            }

            Employees employee = employeesDAO.searchForLogin(email);

            if (employee == null
                    || employee.getId() <= 0
                    || employee.getCompanyId() <= 0) {
                credenciaisInvalidas(request, response);
                return;
            }

            String passwordHash = employee.getPasswordHash();

            if (passwordHash == null || passwordHash.isBlank()) {
                credenciaisInvalidas(request, response);
                return;
            }

            boolean senhaCorreta;

            try {
                senhaCorreta = PasswordHasher.verify(passwordHash, senha);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Erro ao verificar hash de senha.", e);
                senhaCorreta = false;
            }

            if (!senhaCorreta) {
                credenciaisInvalidas(request, response);
                return;
            }

            // Nao confia no grupo retornado pelo login: verifica a autorizacao no banco.
            if (!authorizationService.CompanyAdmin(
                    employee.getId(), employee.getCompanyId())) {
                exibirErro(
                        request,
                        response,
                        "Esta conta não possui acesso ao site administrativo. "
                                + "Utilize o aplicativo da empresa."
                );
                return;
            }

            iniciarSessao(request, employee);
            response.sendRedirect(request.getContextPath() + DASHBOARD);

        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Erro inesperado durante o login.", e);
            exibirErro(
                    request,
                    response,
                    "Não foi possível concluir o login. Tente novamente mais tarde."
            );
        }
    }

    /**
     * Inicia uma sessao nova e guarda somente identificadores necessarios.
     * @param request A requisicao atual
     * @param employee Gerente autenticado e autorizado
     */
    private void iniciarSessao(HttpServletRequest request, Employees employee) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }

        HttpSession session = request.getSession(true);
        request.changeSessionId();

        session.setAttribute("employeeId", employee.getId());
        session.setAttribute("companyId", employee.getCompanyId());
        session.setAttribute("permissionGroupId", employee.getPermissionGroupId());
        session.setAttribute("unitId", employee.getUnitId());
        session.setAttribute("sectorId", employee.getSectorId());
        session.setMaxInactiveInterval(30 * 60);

        LOGGER.info("Sessão administrativa iniciada para funcionário ID: "
                + employee.getId());
    }

    /**
     * Rejeita parametros desconhecidos ou repetidos.
     * @param request A requisicao atual
     * @return true se todos os parametros sao permitidos
     */
    private boolean validarParametros(HttpServletRequest request) {
        for (String parametro : request.getParameterMap().keySet()) {
            if (!CAMPOS_PERMITIDOS.contains(parametro)) {
                return false;
            }

            String[] valores = request.getParameterValues(parametro);
            if (valores == null || valores.length != 1) {
                return false;
            }
        }
        return true;
    }

    // Confere caracteres de controle e formatacao invisiveis.
    private boolean contemCaracteresControle(String valor) {
        return valor != null && CARACTERES_CONTROLE.matcher(valor).find();
    }

    // Mostra mensagem generica para credenciais incorretas.
    private void credenciaisInvalidas(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        exibirErro(request, response, "E-mail ou senha inválidos.");
    }

    // Encaminha a mensagem ao JSP
    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensagem
    ) throws ServletException, IOException {
        request.setAttribute("erro", mensagem);
        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
    }
}
