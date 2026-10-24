
package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.PlansDAO;
import com.example.servelet1ano.filter.CompaniesFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.CompanySize;
import com.example.servelet1ano.model.Plans;
import com.example.servelet1ano.model.Subscriptions;
import com.example.servelet1ano.service.CompanyRegistrationService;

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

@WebServlet("/empresas")
public class CompanyRegisterServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(CompanyRegisterServlet.class.getName());

    private static final String CADASTRO =
            "/webapp/CompanyRegister.jsp";

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

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

            // Prepara a empresa para o cadastro.
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

            // Prepara a assinatura vinculada ao plano escolhido.
            Subscriptions subscription = new Subscriptions();
            subscription.setPlanId(planoSelecionado.getId());
            subscription.setInstallments(true);

            // O serviço registra a empresa e a assinatura na mesma transação.
            CompanyRegistrationService service =
                    new CompanyRegistrationService();

            int companyId = service.register(novaEmpresa, subscription);

            request.setAttribute(
                    "sucesso",
                    "Cadastro realizado com sucesso!"
            );
            request.setAttribute("companyId", companyId);

            request.getRequestDispatcher(CADASTRO)
                    .forward(request, response);

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
