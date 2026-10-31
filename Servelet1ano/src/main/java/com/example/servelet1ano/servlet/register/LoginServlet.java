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

/**
 * Servlet da tela de login (/login)
 * Mostra o formulario com a lista de empresas e confere as credenciais do funcionario
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * Registro de logs usado para anotar os erros inesperados
     */
    private static final Logger LOGGER =
            Logger.getLogger(LoginServlet.class.getName());

    /**
     * Pagina do formulario de login
     */
    private static final String CADASTRO = "/Login.jsp";

    /**
     * metodo que mostra o formulario de login
     * Carrega a lista de empresas para o usuario escolher a dele
     * Se nao for possivel carregar as empresas, responde com erro 500
     * @param request A requisicao
     * @param response A resposta que encaminha para o formulario de login
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
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

    /**
     * metodo que valida as credenciais e inicia a sessao do funcionario
     * Confere os campos, busca a empresa e o funcionario (pelo e-mail, empresa e nome) e compara a senha com o hash guardado
     * Se algo estiver errado, volta para o formulario com uma mensagem generica (sem dizer qual dado errou)
     * Se der certo, troca o id da sessao, guarda employeeId e companyId nela e redireciona para /dashboard
     * @param request A requisicao com os dados do login (empresas, campoNome, campoEmail e campoSenha)
     * @param response A resposta que redireciona para o dashboard ou volta para o formulario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
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

            // o searchAll não traz o hash da senha (por segurança)
            // o searchForLogin é o único método que traz, e só devolve
            // o funcionário se existir exatamente um com esse e-mail
            // e esse nome na empresa
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

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Carrega a lista de empresas que aparece no formulario de login
     * Guarda a lista no atributo "empresas"
     * Se a consulta falhar, a excecao e repassada para quem chamou tratar o erro
     * @param request A requisicao onde a lista sera guardada
     */
    private void carregarEmpresas(HttpServletRequest request) {
        CompaniesDAO companiesDAO = new CompaniesDAO();

        List<Companies> empresas = companiesDAO.searchAll(new CompaniesFilter());

        request.setAttribute("empresas", empresas);
    }

    /**
     * Mostra uma mensagem de erro no formulario de login
     * Guarda a mensagem no atributo "erro", recarrega a lista de empresas
     * e encaminha a mesma requisicao de volta para a pagina
     * Se nao for possivel recarregar as empresas, responde com erro 500
     * @param request A requisicao atual
     * @param response A resposta que encaminha para o formulario
     * @param mensagem O texto do erro que sera mostrado ao usuario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
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