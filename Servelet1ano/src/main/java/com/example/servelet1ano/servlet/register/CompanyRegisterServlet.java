
package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.PlansDAO;
import com.example.servelet1ano.dao.SubscriptionsDAO;
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

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {


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

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Preencha todos os campos obrigatórios."
            );
            return;
        }

        empresa = empresa.trim();
        razao = razao.trim();
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
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Selecione um tamanho de empresa válido."
                );
                return;
        }


        Plans planoSelecionado;
        /*
        * Abre método para identificar o plano selecionado pelo usuário.
        */
        try {
            PlansDAO plansDAO = new PlansDAO();
            planoSelecionado = plansDAO.searchByName(plano);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao consultar o plano no banco de dados.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível consultar os planos. Tente novamente mais tarde."
            );
            return;
        }

        if (planoSelecionado == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "O plano selecionado não existe ou está indisponível."
            );
            return;
        }


        CompaniesDAO companiesDAO = new CompaniesDAO();
        /*
        * Verifica se já há uma empresa cadastrada com os mesmos dados
        */
        try {
            CompaniesFilter filter = new CompaniesFilter();
            filter.setName(empresa);
            filter.setCnpj(cnpj);

            List<Companies> empresasEncontradas =
                    companiesDAO.searchAll(filter);

            if (empresasEncontradas != null
                    && !empresasEncontradas.isEmpty()) {

                response.sendError(
                        HttpServletResponse.SC_CONFLICT,
                        "Esta empresa já está cadastrada. Verifique os dados ou faça login."
                );
                return;
            }

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao verificar a existência da empresa.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Não foi possível verificar o cadastro da empresa. Tente novamente."
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
        /*
        * Realiza o cadastro de empresa e cria uma inscrição, formando uma dependência entre os dois registros utilizando a mesma conexão ao banco
         */

        try {
            Subscriptions subscription = new Subscriptions();

            subscription.setPlanId(planoSelecionado.getId());
            subscription.setInstallments(true);

            CompanyRegistrationService service =
                    new CompanyRegistrationService();

            try {
                int companyId = service.register(novaEmpresa, subscription);

                response.setContentType("text/html;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_CREATED);

                response.getWriter().println(
                        "<h1>Cadastro realizado com sucesso!</h1>"
                );

            } catch (RuntimeException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Falha ao cadastrar empresa e assinatura.",
                        e
                );

                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Não foi possível concluir seu cadastro. "
                                + "Nenhum registro desta operação foi confirmado."
                );
            }
        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro o sistema de cadastro ao banco não respondeu",
                    e
            );
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Nosso serviço de cadastro não esta´disponível no momento. Tente mais tarde."
            );
            }
        }
    }


