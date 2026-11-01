package com.example.servelet1ano.servlet.listscreen;

import com.example.servelet1ano.util.service.AuthorizationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet responsavel pelo encerramento seguro da sessao.
 *
 * Rota: /logout
 *
 * O logout deve ocorrer somente via POST,
 * com validacao de CSRF para impedir requisicoes
 * externas de encerramento de sessao.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(LogoutServlet.class.getName());

    private static final String LOGIN = "/login";

    private static final Set<String> CAMPOS_PERMITIDOS =
            Set.of("csrfToken");

    private final AuthorizationService authorizationService =
            new AuthorizationService();

    /**
     * Impede logout por GET.
     *
     * @param request Requisicao recebida
     * @param response Resposta HTTP
     * @throws IOException Em caso de erro de resposta
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        response.setHeader("Allow", "POST");

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED
        );
    }

    /**
     * Encerra a sessao administrativa.
     *
     * Etapas:
     * 1 - Verifica a sessao
     * 2 - Valida os parametros
     * 3 - Valida CSRF
     * 4 - Invalida a sessao
     * 5 - Expira o cookie de sessao
     * 6 - Redireciona ao login
     *
     * @param request Requisicao de logout
     * @param response Resposta ao navegador
     * @throws IOException Em caso de falha na resposta
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        HttpSession session = request.getSession(false);

        // Se a sessao ja expirou, nao cria outra.
        if (session == null) {
            response.sendRedirect(
                    request.getContextPath() + LOGIN
            );
            return;
        }

        // Verifica campos desconhecidos ou duplicados.
        for (String campo : request.getParameterMap().keySet()) {

            if (!CAMPOS_PERMITIDOS.contains(campo)) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );
                return;
            }

            String[] valores = request.getParameterValues(campo);

            if (valores == null || valores.length != 1) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST
                );
                return;
            }
        }

        // Confere o token da sessao administrativa.
        if (!authorizationService.csrfTokenValido(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return;
        }

        Object employeeId = session.getAttribute("employeeId");

        try {
            // Invalida atributos e identificador da sessao.
            session.invalidate();

            // Solicita a exclusao do cookie de sessao.
            expirarCookieSessao(request, response);

            LOGGER.log(
                    Level.INFO,
                    "Logout realizado. Funcionário ID: {0}",
                    employeeId
            );

            response.sendRedirect(
                    request.getContextPath() + LOGIN
            );

        } catch (IllegalStateException e) {

            LOGGER.log(
                    Level.WARNING,
                    "Sessão já invalidada durante o logout.",
                    e
            );

            expirarCookieSessao(request, response);

            response.sendRedirect(
                    request.getContextPath() + LOGIN
            );
        }
    }

    /**
     * Expira o JSESSIONID no mesmo caminho
     * utilizado pela aplicacao.
     */
    private void expirarCookieSessao(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String contextPath = request.getContextPath();

        String cookiePath =
                contextPath == null || contextPath.isEmpty()
                        ? "/"
                        : contextPath;

        Cookie cookie = new Cookie("JSESSIONID", "");

        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());

        response.addCookie(cookie);
    }
}

