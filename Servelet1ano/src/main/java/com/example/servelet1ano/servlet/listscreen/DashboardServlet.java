package com.example.servelet1ano.servlet.listscreen;

import com.example.servelet1ano.dao.DashboardDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.model.Employees;

import com.example.servelet1ano.util.service.AuthorizationService;
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

@WebServlet("/admin")
public class DashboardServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());

    private static final String DASHBOARD = "/WEB-INF/Dashboard.jsp";
    private static final String LOGIN = "/login";

    private final EmployeesDAO employeesDAO = new EmployeesDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        HttpSession session = request.getSession(false);


        if (session == null) {
            response.sendRedirect(
                    request.getContextPath() + LOGIN
            );
            return;
        }

        Integer employeeId = getSessionId(session, "employeeId");
        Integer companyId = getSessionId(session, "companyId");

        if (employeeId == null || companyId == null) {
            session.invalidate();

            response.sendRedirect(
                    request.getContextPath() + LOGIN
            );
            return;
        }

        try {

            Employees employee =
                    employeesDAO.searchById(employeeId);


            if (employee == null
                    || employee.getCompanyId() <= 0
                    || employee.getCompanyId() != companyId) {

                session.invalidate();

                response.sendRedirect(
                        request.getContextPath() + LOGIN
                );
                return;
            }

            if (!new AuthorizationService().isCompanyAdmin(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }


            Map<String, Integer> metrics =
                    dashboardDAO.getMetrics(companyId);


            request.setAttribute("employee", employee);
            request.setAttribute("companyId", companyId);

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

            new AuthorizationService().CsrfToken(request);

            request.getRequestDispatcher(DASHBOARD)
                    .forward(request, response);

        } catch (RuntimeException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar o dashboard do funcionário.",
                    e
            );
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


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
