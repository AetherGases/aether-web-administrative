package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Companies;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(LoginServlet.class.getName());

    /**
     * Carrega as empresas disponíveis para o formulário de login.
     *
     * @param request requisição HTTP recebida
     * @param response resposta HTTP enviada ao cliente
     * @throws ServletException caso ocorra uma falha no processamento do Servlet
     * @throws IOException caso ocorra uma falha de entrada ou saída
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

    /**
     * Valida as credenciais do funcionário e inicia sua sessão autenticada.
     *
     * @param request requisição HTTP contendo as credenciais
     * @param response resposta HTTP enviada ao cliente
     * @throws ServletException caso ocorra uma falha no processamento do Servlet
     * @throws IOException caso ocorra uma falha de entrada ou saída
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");


        String empresaParam = request.getParameter("empresas");
        String nome = request.getParameter("campoNome");
        String email = request.getParameter("campoEmail");
        String senha = request.getParameter("campoSenha");


        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()
                || empresaParam == null || empresaParam.isBlank()
                || nome == null || nome.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Todos os campos são obrigatórios."
            );
            return;
        }


        nome = nome.trim();
        email = email.trim();


        if (email.length() > 255
                || senha.length() > 1024
                || !Validators.isEmail(email)) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Os dados informados são inválidos."
            );
            return;
        }


        final int companyId;

        try {
            companyId = Integer.parseInt(empresaParam);

            if (companyId <= 0) {
                throw new NumberFormatException(
                        "O identificador deve ser positivo."
                );
            }

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "A empresa selecionada não está disponível."
            );
            return;
        }

        try {

            CompaniesDAO companiesDAO = new CompaniesDAO();

            Companies empresaSelecionada =
                    companiesDAO.searchById(companyId);

            if (empresaSelecionada == null) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "E-mail, senha ou empresa inválidos."
                );
                return;
            }


            EmployeesFilter filterEmployee = new EmployeesFilter();
            filterEmployee.setEmail(email);
            filterEmployee.setCompanyId(companyId);
            filterEmployee.setName(nome);


            EmployeesDAO employeesDAO = new EmployeesDAO();

            List<Employees> employees =
                    employeesDAO.searchAll(filterEmployee);


            if (employees == null || employees.size() != 1) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "E-mail, senha ou empresa inválidos."
                );
                return;
            }

            Employees employee = employees.getFirst();


            String passwordHash = employee.getPasswordHash();

            if (passwordHash == null || passwordHash.isBlank()) {
                LOGGER.warning(
                        "Funcionário sem hash de senha cadastrado."
                );

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "E-mail, senha ou empresa inválidos."
                );
                return;
            }


            boolean senhaCorreta =
                    PasswordHasher.verify(passwordHash, senha);

            if (!senhaCorreta) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "E-mail, senha ou empresa inválidos."
                );
                return;
            }

            /*
             * Inicia a sessão autenticada.
             * A troca do identificador ajuda a prevenir session fixation.
             */
            HttpSession session = request.getSession();

            request.changeSessionId();


            session.setAttribute("employeeId", employee.getId());
            session.setAttribute("companyId", companyId);


            response.sendRedirect(request.getContextPath() + "/");

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro inesperado durante a autenticação do funcionário.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir o login. Tente novamente mais tarde."
            );
        }
    }
}