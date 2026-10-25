
package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.model.StatusEmployee;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.validation.Validators;
import com.example.servelet1ano.filter.EmployeesFilter;

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

@WebServlet("/gerente")
public class ManagerRegisterServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ManagerRegisterServlet.class.getName());

    private static final String CADASTRO = "/ManagerRegister.jsp";

    /*
     * ============================================================
     * EXIBIÇÃO DO FORMULÁRIO
     * ============================================================
     *
     * O método doGet é executado quando o usuário acessa a página
     * de cadastro de gerente.
     *
     * Antes de encaminhar a requisição ao JSP, o Servlet consulta
     * as empresas disponíveis e disponibiliza a lista por meio do
     * atributo "empresas".
     *
     * Caso o cadastro tenha sido concluído e o usuário tenha sido
     * redirecionado para esta página, o parâmetro "sucesso" permite
     * exibir uma mensagem de confirmação.
     *
     * Se ocorrer uma falha ao consultar as empresas, o Servlet
     * registra a exceção e apresenta uma mensagem amigável.
     *
     * ============================================================
     */

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            carregarEmpresas(request);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar empresas para o formulário de cadastro.",
                    e
            );

            request.setAttribute(
                    "erro",
                    "Não foi possível carregar as empresas. Tente novamente mais tarde."
            );
        }

        if ("1".equals(request.getParameter("sucesso"))) {
            request.setAttribute(
                    "sucesso",
                    "Cadastro realizado com sucesso!"
            );
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }


    /*
     * PROCESSAMENTO DO CADASTRO
     *
     * O método doPost recebe os dados enviados pelo formulário.
     *
     * Quando uma validação falha, o método exibirErro encaminha
     * a requisição ao formulário com uma mensagem apropriada.
     *
     */

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String nome = request.getParameter("campoNome");
        String email = request.getParameter("campoEmail");
        String telefone = request.getParameter("campoTelefone");
        String senha = request.getParameter("campoSenha");
        String confirmarSenha = request.getParameter("campoConfirmarSenha");
        String empresaParam = request.getParameter("empresas");
        String cpf = request.getParameter("campoCPF");

        request.setAttribute("campoNome", nome);
        request.setAttribute("campoEmail", email);
        request.setAttribute("campoTelefone", telefone);
        request.setAttribute("campoSenha", senha);
        request.setAttribute("campoConfirmarSenha", confirmarSenha);
        request.setAttribute("empresas", empresaParam);
        request.setAttribute("campoCPF", cpf);


        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || telefone == null || telefone.isBlank()
                || senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()
                || empresaParam == null || empresaParam.isBlank()
                || cpf == null || cpf.isBlank()) {

            exibirErro(
                    request,
                    response,
                    "Todos os campos são obrigatórios."
            );
            return;
        }

        nome = nome.trim();
        email = email.trim();
        telefone = telefone.trim();
        empresaParam = empresaParam.trim();
        cpf = cpf.trim();


        /*
         * VALIDAÇÃO DE DADOS
         *
         * Verifica email, cpf e telefone de acordo com os validators
         * Verifica senha de acordo com uma validação básica (1024 caracteres)
         *
         */

        if (email.length() > 255 || !Validators.isEmail(email)) {
            exibirErro(
                    request,
                    response,
                    "O e-mail digitado não é válido. Verifique seus dados."
            );
            return;
        }


        if (senha.length() > 1024
                || confirmarSenha.length() > 1024) {

            exibirErro(
                    request,
                    response,
                    "As senhas devem ter no máximo 1024 caracteres."
            );
            return;
        }


        if (!Validators.isCpf(cpf)) {
            exibirErro(
                    request,
                    response,
                    "O CPF digitado não é válido. Verifique seus dados."
            );
            return;
        }


        if (!senha.equals(confirmarSenha)) {
            exibirErro(
                    request,
                    response,
                    "As senhas não conferem. Verifique-as e tente novamente."
            );
            return;
        }

        if (!Validators.isMobilePhone(telefone)) {
            exibirErro(
                    request,
                    response,
                    "O telefone digitado não é válido. Verifique seus dados."
            );
            return;
        }

        final int companyId;

        try {
            companyId = Integer.parseInt(empresaParam);

            if (companyId <= 0) {
                throw new NumberFormatException(
                        "O identificador da empresa deve ser positivo."
                );
            }

        } catch (NumberFormatException e) {
            exibirErro(
                    request,
                    response,
                    "A empresa selecionada não é válida. Selecione uma opção disponível."
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
                        "A empresa selecionada não foi encontrada ou não está disponível."
                );
                return;
            }

            StatusEmployee statusGerente = StatusEmployee.ACTIVE;

            String senhaHash = PasswordHasher.hash(senha);

            PermissionGroups permissionGroups =
                    new PermissionGroups(
                            1,
                            "Admin da empresa",
                            companyId
                    );

            Employees employee = new Employees(
                    companyId,
                    permissionGroups.getId(),
                    cpf,
                    nome,
                    email,
                    telefone,
                    senhaHash,
                    statusGerente
            );

            EmployeesDAO employeesDAO = new EmployeesDAO();

            employeesDAO.register(employee);


            EmployeesFilter filterEmployee = new EmployeesFilter();
            filterEmployee.setEmail(email);
            filterEmployee.setCompanyId(companyId);
            filterEmployee.setName(nome);

            List<Employees> employees = employeesDAO.searchAll(filterEmployee);

            if (employees == null || employees.size() != 1) {
                throw new RuntimeException(
                        "Não foi possível recuperar o funcionário cadastrado."
                );
            }

            HttpSession session = request.getSession();
            request.changeSessionId();

            session.setAttribute(
                    "employeeId",
                    employees.getFirst().getId()
            );

            session.setAttribute("companyId", companyId);

            response.sendRedirect(
                    request.getContextPath() + "/dashboard"
            );

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao processar o cadastro do gerente.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    "Não foi possível concluir seu cadastro. Tente novamente mais tarde."
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

        List<Companies> empresas =
                companiesDAO.searchAll(new CompaniesFilter());

        request.setAttribute("empresas", empresas);
    }


    /*
     * EXIBIÇÃO DE ERROS
     *
     * Recebe a mensagem que deverá ser apresentada ao usuário,
     * disponibiliza-a pelo atributo "erro" e carrega novamente
     * as empresas para que o formulário possa ser renderizado.
     *
     * Se ocorrer uma falha ao carregar as empresas, uma lista
     * vazia é disponibilizada e a exceção é registrada.
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
        request.setAttribute("empresas", List.of());

        try {
            carregarEmpresas(request);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar empresas para o formulário.",
                    e
            );
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }
}
