package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.filter.EmployeesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.validation.Validators;
import com.example.servelet1ano.model.StatusEmployee;
import com.example.servelet1ano.model.PermissionGroups;
import com.example.servelet1ano.dao.PermissionGroupsDAO;
import com.example.servelet1ano.filter.PermissionGroupsFilter;


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

            request.getRequestDispatcher("/ManagerRegister.jsp")
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

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nome = request.getParameter("campoNome");
        String email = request.getParameter("campoEmail");
        String telefone = request.getParameter("campoTelefone");
        String senha = request.getParameter("campoSenha");
        String confirmarSenha = request.getParameter("campoConfirmarSenha");
        String empresaParam = request.getParameter("empresas");
        String cpf = request.getParameter("campoCPF");

        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || telefone == null || telefone.isBlank()
                || senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()
                || empresaParam == null || empresaParam.isBlank()
                || cpf == null || cpf.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Todos os campos são obrigatórios"
            );
            return;
        }
        nome = nome.trim();
        email = email.trim();
        telefone = telefone.trim();
        empresaParam = empresaParam.trim();
        cpf = cpf.trim();

        if (email.length() > 255
                || !Validators.isEmail(email)) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Email inválido, verifique seus dados ou tente novamente.");
            return;
        }
        if (senha.length() > 1024
                || confirmarSenha.length() > 1024) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Senha inválida. As senhas devem seguir o limite de 1024 caractéres."
            );
            return;
        }
        if (!Validators.isCpf(cpf)) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "CPF inválido. Verifique os números informados e tente novamente."
            );
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "As senhas não conferem. Verifique-as e tente novamente."
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
            StatusEmployee statusGerente;
            statusGerente = StatusEmployee.ACTIVE;
            String senhaHash = PasswordHasher.hash(senha);

            //Aguardando atualizações sobre permission group para continuar :)))))


        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao conectar com o banco e realizar o cadastro do usuário " + nome + " da empresa de ID" + companyId,
                    e
            );
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir o seu cadastro devido a um erro inesperado. Tente novamente mais tarde."
            );
        }

    }
}
