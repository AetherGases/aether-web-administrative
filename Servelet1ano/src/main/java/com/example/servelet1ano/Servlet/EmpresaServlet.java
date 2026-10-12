package com.example.servelet1ano.Servlet;

import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.CompanySize;
import com.example.servelet1ano.dao.PlansDAO;
import com.example.servelet1ano.model.Plans;
import com.example.servelet1ano.model.Subscriptions;
import com.example.servelet1ano.dao.SubscriptionsDAO;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

@WebServlet("/empresas")

public class EmpresaServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            request.setCharacterEncoding("UTF-8");
            String empresa = request.getParameter("empresas");
            String tamanho = request.getParameter("tamanhos");
            String cnpj = request.getParameter("campoCNPJ");
            String email = request.getParameter("campoEmail");
            String plano = request.getParameter("planos");

            if (empresa == null || empresa.isBlank()
                    || tamanho == null || tamanho.isBlank()
                    || cnpj == null || cnpj.isBlank()
                    || email == null || email.isBlank()
                    || plano == null || plano.isBlank()) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Todos os campos são obrigatórios."
                );
                return;
            }
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
                            "Tamanho de empresa inválido"
                    );
                    return;
            }
            PlansDAO plansDAO = new PlansDAO();

            Plans planoSelecionado = plansDAO.searchByName(plano);

            if (planoSelecionado == null) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Plano inválido ou indisponível."
                );
                return;
            }

            Companies novaEmpresa = new Companies(
                    empresa.trim(),
                    tamanhoEmpresa,
                    cnpj.trim(),
                    email.trim()
            );

            novaEmpresa.setTradeName(empresa.trim());


            novaEmpresa.setRegistrationDate(
                    Date.valueOf(LocalDate.now())
            );


            CompaniesDAO companiesDAO = new CompaniesDAO();

            int companyId = companiesDAO.registerReturningId(novaEmpresa);

            if (companyId == -1) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Não foi possível cadastrar a empresa. "
                                + "Verifique o CNPJ e os dados informados."
                );
                return;
            }


            Subscriptions subscription = new Subscriptions();

            subscription.setCompanyId(companyId);
            subscription.setPlanId(planoSelecionado.getId());
            subscription.setInstallments(true);


            SubscriptionsDAO subscriptionsDAO = new SubscriptionsDAO();

            boolean cadastro =
                    subscriptionsDAO.register(subscription);

            if (!cadastro) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Empresa cadastrada, mas houve um erro "
                                + "ao cadastrar a assinatura."
                );
                return;
            }


            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h1>Cadastro realizado com sucesso!</h1>"
            );

        }
    }


