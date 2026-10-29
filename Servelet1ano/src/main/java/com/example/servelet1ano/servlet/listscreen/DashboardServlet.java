package com.example.servelet1ano.servlet.listscreen;

import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.dao.PermissionGroupsDAO;



import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;


@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet{

    private final EmployeesDAO employeesDAO = new EmployeesDAO();

    /*
    *Recupera a sessão do usuário, responsável pelas informações personalidades ddo servlet.
     */

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }
        Object employeeIdAttribute = session.getAttribute("employeeId");


        if (!(employeeIdAttribute instanceof Integer)) {
            session.invalidate();

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }

        int employeeId = (Integer) employeeIdAttribute;


        Employees employee = employeesDAO.searchById(employeeId);


        if (employee == null) {
            session.invalidate();

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }

        request.setAttribute("employee", employee);


        request.getRequestDispatcher("/Dashboard.jsp")
                .forward(request, response);
    }


}



