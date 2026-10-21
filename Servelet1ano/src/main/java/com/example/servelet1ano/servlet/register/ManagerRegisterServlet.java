package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.validation.Validators;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/gerente")
public class ManagerRegisterServlet extends HttpServlet {
    private static final Logger LOGGER =
            Logger.getLogger(CompanyRegisterServlet.class.getName());

    /**
     * Carrega as empresas disponíveis para o formulário de login.
     *
     * @param request  requisição HTTP recebida
     * @param response resposta HTTP enviada ao cliente
     * @throws ServletException caso ocorra uma falha no processamento do Servlet
     * @throws IOException      caso ocorra uma falha de entrada ou saída
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            CompaniesDAO companiesDAO = new CompaniesDAO();

            List<Companies> empresas =
                    companiesDAO.searchAll(new CompaniesFilter());

            request.setAttribute("empresas", empresas);

            request.getRequestDispatcher("/Login.jsp")
                    .forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar empresas para o formulário de login.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível carregar as empresas."
            );
        }



    }
}
