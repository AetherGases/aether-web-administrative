package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.PlansDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.CompanySize;
import com.example.servelet1ano.model.Plans;
import com.example.servelet1ano.model.Subscriptions;
import com.example.servelet1ano.util.service.CompanyRegistrationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet do cadastro de uma nova empresa (/empresa)
 * Cadastra a empresa e a assinatura do plano escolhido
 */
@WebServlet("/empresa")
public class NewCompanyRegisterServlet extends HttpServlet {

    /**
     * Registro de logs usado para anotar os erros inesperados
     */
    private static final Logger LOGGER =
            Logger.getLogger(NewCompanyRegisterServlet.class.getName());

    /**
     * Pagina do formulario de cadastro da empresa
     */
    private static final String CADASTRO = "/CompanyRegister.jsp";

    /**
     * metodo que recebe os dados do formulario e cadastra a empresa com a assinatura do plano
     * Confere se todos os campos vieram preenchidos, se o tamanho e o plano existem e se a empresa ainda nao esta cadastrada
     * Se algo estiver errado, volta para o formulario com a mensagem de erro (mantendo o que foi digitado)
     * Se der certo, redireciona para /gerente com o aviso de sucesso
     * @param request A requisicao com os dados da empresa (empresas, razoes, tamanhos, campoCNPJ, campoEmail e planos)
     * @param response A resposta que redireciona ou volta para o formulario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        HttpSession session = request.getSession();

        request.setCharacterEncoding("UTF-8");

        String empresa = request.getParameter("empresas");
        String razao = request.getParameter("razoes");
        String tamanho = request.getParameter("tamanhos");
        String cnpj = request.getParameter("campoCNPJ");
        String email = request.getParameter("campoEmail");
        String plano = request.getParameter("planos");

        if (empresa == null || empresa.isBlank()
                || razao == null || razao.isBlank()
                || tamanho == null || tamanho.isBlank()
                || cnpj == null || cnpj.isBlank()
                || email == null || email.isBlank()
                || plano == null || plano.isBlank()) {

            exibirErro(
                    request,
                    response,
                    "Preencha todos os campos obrigatórios."
            );
            return;
        }

        empresa = empresa.trim();
        razao = razao.trim();
        tamanho = tamanho.trim();
        cnpj = cnpj.trim();
        email = email.trim();
        plano = plano.trim();

        CompanySize tamanhoEmpresa;

        switch (tamanho) {
            case "pequena":
                tamanhoEmpresa = CompanySize.PEQUENA;
                break;

            case "media":
                tamanhoEmpresa = CompanySize.MEDIA;
                break;

            case "grande":
                tamanhoEmpresa = CompanySize.GRANDE;
                break;

            default:
                exibirErro(
                        request,
                        response,
                        "Selecione um tamanho de empresa válido."
                );
                return;
        }

        try {

            PlansDAO plansDAO = new PlansDAO();
            Plans planoSelecionado = plansDAO.searchByName(plano);

            if (planoSelecionado == null) {
                exibirErro(
                        request,
                        response,
                        "O plano selecionado não existe ou está indisponível."
                );
                return;
            }


            CompaniesDAO companiesDAO = new CompaniesDAO();
            CompaniesFilter filter = new CompaniesFilter();

            filter.setName(empresa);
            filter.setCnpj(cnpj);

            List<Companies> empresasEncontradas =
                    companiesDAO.searchAll(filter);

            if (empresasEncontradas != null
                    && !empresasEncontradas.isEmpty()) {

                exibirErro(
                        request,
                        response,
                        "Esta empresa já está cadastrada. "
                                + "Verifique os dados ou faça login."
                );
                return;
            }


            Companies novaEmpresa = new Companies(
                    empresa,
                    razao,
                    tamanhoEmpresa,
                    cnpj,
                    email
            );

            novaEmpresa.setRegistrationDate(
                    Date.valueOf(LocalDate.now())
            );


            Subscriptions subscription = new Subscriptions();
            subscription.setPlanId(planoSelecionado.getId());
            subscription.setInstallments(true);


            CompanyRegistrationService service =
                    new CompanyRegistrationService();

            int empresaId = service.register(novaEmpresa, subscription);

            // o service devolve -1 quando o CNPJ já existe (mesmo que o nome
            // seja diferente, o que a busca acima não pega)
            if (empresaId == -1) {
                exibirErro(
                        request,
                        response,
                        "Este CNPJ já está cadastrado. "
                                + "Verifique os dados ou faça login."
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/gerente?sucesso=1"
            );
            return;
        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao realizar o cadastro da empresa.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    "Não foi possível concluir o cadastro. "
                            + "Tente novamente mais tarde."
            );
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Mostra uma mensagem de erro no formulario de cadastro
     * Guarda a mensagem no atributo "erro" e encaminha a mesma requisicao de volta para a pagina,
     * assim o que o usuario digitou continua preenchido
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
        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }
}