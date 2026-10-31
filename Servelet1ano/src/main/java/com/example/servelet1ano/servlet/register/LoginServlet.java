
package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.EmployeesDAO;
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
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(LoginServlet.class.getName());

    private static final String LOGIN_PAGE = "/Login.jsp";
    private static final String DASHBOARD = "/dashboard";

    private final EmployeesDAO employeesDAO = new EmployeesDAO();

    /**
     * Exibe a tela de login.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("employeeId") instanceof Integer
                && session.getAttribute("companyId") instanceof Integer) {

            response.sendRedirect(
                    request.getContextPath() + DASHBOARD
            );
            return;
        }

        request.getRequestDispatcher(LOGIN_PAGE)
                .forward(request, response);
    }

    /**
     * Autentica o funcionário e inicia sua sessão.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-store");

        try {

            if (!validarParametros(request)) {
                exibirErro(
                        request,
                        response,
                        "Os dados enviados são inválidos."
                );
                return;
            }

            String email = request.getParameter("campoEmail");
            String senha = request.getParameter("campoSenha");

            if (email == null || email.isBlank()
                    || senha == null || senha.isBlank()) {

                exibirErro(
                        request,
                        response,
                        "Todos os campos são obrigatórios."
                );
                return;
            }

            email = email.trim().toLowerCase(Locale.ROOT);

            request.setAttribute("email", email);

            if (email.length() > 255
                    || senha.length() > 1024
                    || !Validators.isEmail(email)
                    || contemCaracteresControle(email)
                    || contemCaracteresControle(senha)) {

                exibirErro(
                        request,
                        response,
                        "Os dados informados são inválidos."
                );
                return;
            }

            Employees employee =
                    employeesDAO.searchForLogin(email);

            if (employee == null) {
                credenciaisInvalidas(request, response);
                return;
            }

            if (employee.getId() <= 0
                    || employee.getCompanyId() <= 0) {

                LOGGER.warning(
                        "Funcionário com identificadores inválidos."
                );

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
                senhaCorreta =
                        PasswordHasher.verify(passwordHash, senha);

            } catch (RuntimeException e) {

                LOGGER.log(
                        Level.WARNING,
                        "Erro ao verificar hash de senha.",
                        e
                );

                senhaCorreta = false;
            }

            if (!senhaCorreta) {
                credenciaisInvalidas(request, response);
                return;
            }

            iniciarSessao(request, employee);

            response.sendRedirect(
                    request.getContextPath() + DASHBOARD
            );

        } catch (RuntimeException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Erro inesperado durante o login.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    "Não foi possível concluir o login. Tente novamente mais tarde."
            );
        }
    }

    /**
     * Cria a sessão autenticada com os identificadores
     * necessários para as demais telas.
     */
    private void iniciarSessao(
            HttpServletRequest request,
            Employees employee
    ) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        session = request.getSession(true);
        request.changeSessionId();

        session.setAttribute("employeeId", employee.getId());
        session.setAttribute("companyId", employee.getCompanyId());

        session.setAttribute(
                "permissionGroupId",
                employee.getPermissionGroupId()
        );

        session.setAttribute("unitId", employee.getUnitId());
        session.setAttribute("sectorId", employee.getSectorId());

        session.setMaxInactiveInterval(30 * 60);

        LOGGER.info(
                "Sessão autenticada iniciada para funcionário ID: "
                        + employee.getId()
        );
    }

    /**
     * Impede parâmetros desconhecidos ou duplicados.
     */
    private boolean validarParametros(HttpServletRequest request) {

        for (String parametro : request.getParameterMap().keySet()) {

            if (!parametro.equals("campoEmail")
                    && !parametro.equals("campoSenha")) {
                return false;
            }

            String[] valores = request.getParameterValues(parametro);

            if (valores == null || valores.length != 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Rejeita caracteres de controle nos campos.
     */
    private boolean contemCaracteresControle(String valor) {

        return valor.codePoints()
                .anyMatch(Character::isISOControl);
    }

    /**
     * Exibe mensagem genérica para credenciais incorretas.
     */
    private void credenciaisInvalidas(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        exibirErro(
                request,
                response,
                "E-mail ou senha inválidos."
        );
    }

    /**
     * Encaminha o erro para o JSP sem expor a senha.
     */
    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensagem
    ) throws ServletException, IOException {

        request.setAttribute("erro", mensagem);

        request.getRequestDispatcher(LOGIN_PAGE)
                .forward(request, response);
    }
}
