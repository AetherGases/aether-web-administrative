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

/**
 * Servlet do dashboard (/dashboard)
 * Mostra a tela inicial do funcionario que fez login
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet{

    /**
     * DAO usado para buscar o funcionario logado
     */
    private final EmployeesDAO employeesDAO = new EmployeesDAO();

    /**
     * metodo que mostra o dashboard do funcionario logado
     * Recupera o funcionario pelo employeeId guardado na sessao e envia para a pagina no atributo "employee"
     * Se nao houver sessao, o id for invalido ou o funcionario nao existir mais, encerra a sessao e redireciona para o login
     * @param request A requisicao (o funcionario vem da sessao)
     * @param response A resposta que encaminha para o dashboard ou redireciona para o login
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
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