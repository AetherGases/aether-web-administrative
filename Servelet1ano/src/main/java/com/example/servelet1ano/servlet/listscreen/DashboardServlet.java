package com.example.servelet1ano.servlet.listscreen;

import com.example.servelet1ano.dao.DashboardDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.model.Employees;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(DashboardServlet.class.getName());

    private static final String DASHBOARD_PAGE = "/Dashboard.jsp";
    private static final String LOGIN_PAGE = "/login";

    private final EmployeesDAO employeesDAO = new EmployeesDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    /**
     * Carrega os dados do dashboard do funcionário autenticado.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        HttpSession session = request.getSession(false);

        // Verifica se existe uma sessão autenticada.
        if (session == null) {
            response.sendRedirect(
                    request.getContextPath() + LOGIN_PAGE
            );
            return;
        }

        Integer employeeId = getSessionId(session, "employeeId");
        Integer companyId = getSessionId(session, "companyId");

        // Impede o acesso sem identificadores válidos.
        if (employeeId == null || companyId == null) {
            session.invalidate();

            response.sendRedirect(
                    request.getContextPath() + LOGIN_PAGE
            );
            return;
        }

        try {

            // Recupera o funcionário autenticado.
            Employees employee =
                    employeesDAO.searchById(employeeId);

            // Confere se o funcionário pertence à empresa da sessão.
            if (employee == null
                    || employee.getCompanyId() != companyId) {

                session.invalidate();

                response.sendRedirect(
                        request.getContextPath() + LOGIN_PAGE
                );
                return;
            }

            // Busca os indicadores da empresa.
            Map<String, Integer> metrics =
                    dashboardDAO.getMetrics(companyId);

            // Dados utilizados pela Dashboard.jsp.
            request.setAttribute("employee", employee);

            request.setAttribute(
                    "totalSectors",
                    metrics.get("totalSectors")
            );

            request.setAttribute(
                    "totalPermissions",
                    metrics.get("totalPermissions")
            );

            request.setAttribute(
                    "totalEmployees",
                    metrics.get("totalEmployees")
            );

            request.setAttribute(
                    "totalUnits",
                    metrics.get("totalUnits")
            );

            request.getRequestDispatcher(DASHBOARD_PAGE)
                    .forward(request, response);

        } catch (RuntimeException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar o dashboard do funcionário.",
                    e
            );

            request.setAttribute(
                    "erroDashboard",
                    "Não foi possível carregar os dados do dashboard."
            );

            request.getRequestDispatcher(DASHBOARD_PAGE)
                    .forward(request, response);
        }
    }

    /**
     * Recupera um identificador positivo armazenado na sessão.
     */
    private Integer getSessionId(
            HttpSession session,
            String attribute
    ) {

        Object value = session.getAttribute(attribute);

        if (!(value instanceof Number number)) {
            return null;
        }

        long id = number.longValue();

        if (id <= 0 || id > Integer.MAX_VALUE) {
            return null;
        }

        return (int) id;
    }
}
