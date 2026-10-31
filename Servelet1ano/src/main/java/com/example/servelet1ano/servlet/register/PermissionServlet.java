package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.util.service.AuthorizationService;

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

@WebServlet("/permissoes")
public class PermissionServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(PermissionServlet.class.getName());

    private static final String PAGINA = "/permissionsOne.jsp";


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
                    request.getContextPath() + "/login"
            );
            return;
        }

        // Confere se o funcionário possui acesso administrativo.
        if (!new AuthorizationService().isCompanyAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Integer companyId = (Integer) session.getAttribute("companyId");

        try {
            EmployeesFilter filter = new EmployeesFilter();
            filter.setCompanyId(companyId);

            List<Employees> funcionarios = new EmployeesDAO().searchAll(filter);

            request.setAttribute("funcionarios", funcionarios);
            request.setAttribute("funcionarioSelecionado", lerId(request.getParameter("funcionario")));

            request.getRequestDispatcher(PAGINA).forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar funcionários para a tela de permissões.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível carregar a tela de permissões."
            );
        }
    }
    private Integer lerId(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            int id = Integer.parseInt(valor.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}