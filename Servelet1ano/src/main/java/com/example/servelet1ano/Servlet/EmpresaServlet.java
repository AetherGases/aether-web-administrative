package com.example.servelet1ano.Servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/empresas")

public class EmpresaServlet extends HttpServlet {
        @Override
        protected void doPost(
                HttpServletRequest request,
                HttpServletResponse response
        ) throws ServletException, IOException {

            request.setCharacterEncoding("UTF-8");

            String empresa = request.getParameter("empresas");
            String tamanho = request.getParameter("tamanhos");
            String cnpj = request.getParameter("campoCNPJ");
            String email = request.getParameter("campoEmail");
            String endereco = request.getParameter("campoEndereco");
            String plano = request.getParameter("planos");

            if (empresa == null || empresa.isBlank()
                    || tamanho == null || tamanho.isBlank()
                    || cnpj == null || cnpj.isBlank()
                    || email == null || email.isBlank()
                    || endereco == null || endereco.isBlank()
                    || plano == null || plano.isBlank()) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Todos os campos são obrigatórios."
                );
                return;
            }



        }
    }


