package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.validation.Validators;

import jakarta.servlet.RequestDispatcher;
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

    private static final String CADASTRO = "/Login.jsp";

    /*
     *CARREGAMENTO DE EMPRESA.
     *
     *Recupera a sessão do gerente para relacionar a empresa do employee
     *
     * Se não conseguiR, trata o erro RunTimeException pelo método exibirErros()
     *
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            carregarEmpresas(request);

            request.getRequestDispatcher(CADASTRO)
                    .forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar empresas para o formulário de login.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível carregar o formulário de login."
            );
        }
    }

    /** LOGIN DOPOST
     *
     * Valida as credenciais e inicia a sessão do funcionário.
     *
     * Se houver algum erro por runtime, é tratado por um try/catch.
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

        request.setAttribute("empresaParam", empresaParam);
        request.setAttribute("nome", nome);
        request.setAttribute("email", email);
        request.setAttribute("senha", senha);


        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()
                || empresaParam == null || empresaParam.isBlank()
                || nome == null || nome.isBlank()) {

            exibirErro(
                    request,
                    response,
                    "Todos os campos são obrigatórios."
            );
            return;
        }

        nome = nome.trim();
        email = email.trim();


        if (email.length() > 255
                || senha.length() > 1024
                || !Validators.isEmail(email)) {

            exibirErro(
                    request,
                    response,
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
            exibirErro(
                    request,
                    response,
                    "A empresa selecionada não está disponível."
            );
            return;
        }

        try {
            CompaniesDAO companiesDAO = new CompaniesDAO();

            Companies empresaSelecionada =
                    companiesDAO.searchById(companyId);

            if (empresaSelecionada == null) {
                exibirErro(
                        request,
                        response,
                        "Nome, e-mail, senha ou empresa inválidos."
                );
                return;
            }

            /*
             * O searchAll não traz o hash da senha (por segurança).
             * O searchForLogin é o único método que traz, e só devolve
             * o funcionário se existir exatamente um com esse e-mail
             * e esse nome na empresa.
             */
            EmployeesDAO employeesDAO = new EmployeesDAO();

            Employees employee =
                    employeesDAO.searchForLogin(email, companyId, nome);

            if (employee == null) {
                exibirErro(
                        request,
                        response,
                        "Nome, e-mail, senha ou empresa inválidos."
                );
                return;
            }

            String passwordHash = employee.getPasswordHash();

            if (passwordHash == null || passwordHash.isBlank()) {
                LOGGER.warning(
                        "Funcionário sem hash de senha cadastrado."
                );

                exibirErro(
                        request,
                        response,
                        "Nome, e-mail, senha ou empresa inválidos."
                );
                return;
            }

            boolean senhaCorreta;

            try {
                senhaCorreta = PasswordHasher.verify(passwordHash, senha);

            } catch (RuntimeException e) {
                // hash fora do formato esperado no banco: trata como senha errada
                LOGGER.log(
                        Level.WARNING,
                        "Não foi possível conferir o hash de senha do funcionário.",
                        e
                );
                senhaCorreta = false;
            }

            if (!senhaCorreta) {
                exibirErro(
                        request,
                        response,
                        "Nome, e-mail, senha ou empresa inválidos."
                );
                return;
            }


            HttpSession session = request.getSession();
            request.changeSessionId();

            session.setAttribute("employeeId", employee.getId());
            session.setAttribute("companyId", companyId);

            response.sendRedirect(
                    request.getContextPath() + "/dashboard"
            );

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro inesperado durante a autenticação do funcionário.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    "Não foi possível concluir o login. Tente novamente mais tarde."
            );
        }
    }

    /*
     * CARREGAMENTO DAS EMPRESAS
     *
     * Consulta as empresas disponíveis e disponibiliza a lista
     * ao JSP pelo atributo "empresas".
     *
     * Caso a consulta falhe, a exceção é propagada para o método
     * responsável por tratar a falha.
     */

    private void carregarEmpresas(HttpServletRequest request) {
        CompaniesDAO companiesDAO = new CompaniesDAO();

        List<Companies> empresas = companiesDAO.searchAll(new CompaniesFilter());

        request.setAttribute("empresas", empresas);
    }

    /*
     * EXIBIÇÃO DE ERROS
     *
     * Recebe a mensagem que deverá ser apresentada ao usuário,
     * disponibiliza-a pelo atributo "erro".
     *
     * O forward encaminha a requisição atual ao JSP, permitindo
     * que a mensagem seja exibida na mesma página do formulário.
     */

    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            String mensagem
    ) throws ServletException, IOException {

        request.setAttribute("erro", mensagem);

        try {
            carregarEmpresas(request);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao recarregar empresas após falha no login.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível carregar o formulário de login."
            );
            return;
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }
}