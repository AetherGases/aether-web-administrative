
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

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
     * ============================================================
     * PROCESSAMENTO DO CADASTRO
     * ============================================================
     *
     * O método doPost recebe os dados enviados pelo formulário.
     *
     * O processamento é dividido em etapas:
     *
     * 1. Recuperação dos campos enviados pelo formulário.
     * 2. Verificação dos campos obrigatórios.
     * 3. Normalização e validação dos dados.
     * 4. Conversão e verificação do identificador da empresa.
     * 5. Consulta da empresa selecionada.
     * 6. Criação do hash da senha.
     * 7. Construção e persistência do funcionário.
     *
     * Quando uma validação falha, o método exibirErro encaminha
     * a requisição ao formulário com uma mensagem apropriada.
     *
     * As exceções inesperadas são registradas no log, sem expor
     * detalhes internos do sistema ao usuário.
     *
     * ============================================================
     */

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        /*
         * Recupera os valores enviados pelo formulário.
         * As strings podem ser null caso algum campo não tenha
         * sido enviado na requisição.
         */

        String nome = request.getParameter("campoNome");
        String email = request.getParameter("campoEmail");
        String telefone = request.getParameter("campoTelefone");
        String senha = request.getParameter("campoSenha");
        String confirmarSenha =
                request.getParameter("campoConfirmarSenha");
        String empresaParam = request.getParameter("empresas");
        String cpf = request.getParameter("campoCPF");


        /*
         * Verifica se todos os campos obrigatórios foram enviados
         * e possuem algum conteúdo.
         *
         * A senha não é removida de espaços nas extremidades,
         * pois isso alteraria a senha informada pelo usuário.
         */

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


        /*
         * Remove espaços desnecessários dos campos textuais que
         * não representam senhas.
         *
         * A normalização acontece antes das validações para evitar
         * que espaços acidentais prejudiquem a verificação dos dados.
         */

        nome = nome.trim();
        email = email.trim();
        telefone = telefone.trim();
        empresaParam = empresaParam.trim();
        cpf = cpf.trim();


        /*
         * VALIDAÇÃO DO E-MAIL
         *
         * O email, cpf e telefone seguem os padrões impostos pela classe validator.
         * A senha segue uma definição máxima padrão de 1024 caractéres.
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


        /*
         * CONVERSÃO DO IDENTIFICADOR DA EMPRESA
         *
         * O valor recebido do formulário é uma String.
         * Ele precisa ser convertido para int e deve ser positivo.
         */

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


        /*
         * CADASTRO DO FUNCIONÁRIO
         *
         * Nesta etapa, o Servlet consulta a empresa selecionada,
         * prepara os dados do gerente, gera o hash da senha e
         * solicita ao DAO que persista o funcionário.
         *
         * A senha em texto puro não deve ser armazenada no banco.
         *
         * O ID do grupo de permissões abaixo segue o valor usado
         * anteriormente no projeto. Confirme se o ID 1 corresponde
         * ao grupo correto no banco de dados.
         */

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


            /*
             * REDIRECIONAMENTO APÓS O SUCESSO
             *
             * Após a persistência, o redirecionamento inicia uma
             * nova requisição GET.
             *
             * Isso evita que uma atualização da página tente
             * reenviar os dados do formulário.
             */

            response.sendRedirect(
                    request.getContextPath() + "/gerente?sucesso=1"
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
     * ============================================================
     * MÉTODOS AUXILIARES
     * ============================================================
     *
     * Os métodos auxiliares centralizam operações repetidas e
     * mantêm os métodos doGet e doPost mais legíveis.
     *
     * ============================================================
     */


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
