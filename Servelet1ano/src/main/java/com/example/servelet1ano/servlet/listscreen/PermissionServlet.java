package com.example.servelet1ano.servlet.listscreen;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet da tela de permissoes (/permissoes)
 * Por enquanto so encaminha para a pagina de permissoes
 */
@WebServlet("/permissoes")
public class PermissionServlet extends HttpServlet {

    /**
     * metodo que mostra a tela de permissoes
     * @param request A requisicao
     * @param response A resposta que encaminha para a pagina de permissoes
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/PermissionsOne.jsp")
                .forward(request, response);
    }

}