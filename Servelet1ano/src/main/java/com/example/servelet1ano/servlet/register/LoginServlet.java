package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.util.PasswordHasher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.logging.*;


import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER =  Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String empresa = request.getParameter("empresas");
        String nome = request.getParameter("campoNome");
        String email = request.getParameter("campoEmail");
        String senha = request.getParameter("campoSenha");

        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()
                || empresa == null || empresa.isBlank()
                || nome == null || nome.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Todos os campos são obrigatórios."
            );
            return;
        }

        CompaniesFilter filterCompany = new CompaniesFilter();
        filterCompany.setName(empresa);
        CompaniesDAO companiesDAO = new CompaniesDAO();
        List<Companies> empresaSelecionada = companiesDAO.searchAll(filterCompany);
        if (empresaSelecionada.isEmpty()){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Credenciais inválidas, verifique sua empresa novamente");
            return;
        }
        int companyId = empresaSelecionada.getFirst().getId();


        EmployeesFilter filterEmployee = new EmployeesFilter();
        filterEmployee.setEmail(email);
        filterEmployee.setCompanyId(companyId);
        EmployeesDAO employeesDAO = new EmployeesDAO();
        List<Employees> employees = employeesDAO.searchAll(filterEmployee);
        if (employees.isEmpty()){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Credenciais inválidas, verifique seus dados pessoais novamente");
            return;
        }
        Employees employee = employees.getFirst();
        String passwordHash = employee.getPasswordHash();
        if (passwordHash == null || passwordHash.isBlank()) {
            LOGGER.warning("Funcionário sem hash de senha cadastrado.");

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "E-mail, senha ou empresa inválidos."
            );
            return;
        }
        boolean senhaCorreta = PasswordHasher.verify(
                employee.getPasswordHash(),
                senha
        );

        if (!senhaCorreta) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "E-mail, senha ou empresa inválidos."
            );
            return;
        }
        request.changeSessionId();

        var session = request.getSession();
        session.setAttribute("employeeId", employee.getId());
        session.setAttribute("companyId", companyId);

        response.sendRedirect(
                request.getContextPath() + ""
        );
    }
}

