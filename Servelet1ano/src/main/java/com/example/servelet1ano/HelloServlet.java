package com.example.servelet1ano;

import java.io.*;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "Aether", value = "")
public class HelloServlet extends HttpServlet {
    private String message;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String nome = request.getParameter("nome");

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        out.println("<h1>Olá, " + nome + "!</h1>");
    }

    public void destroy() {
    }
}