package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.EmployeesDAO;
import com.example.servelet1ano.dao.PasswordRecoveryDAO;
import com.example.servelet1ano.util.service.EmailService;
import com.example.servelet1ano.util.service.PasswordRecoveryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/recover-password")
public class PasswordRecoveryServlet extends HttpServlet {
    private final PasswordRecoveryService recoveryService =
            new PasswordRecoveryService();

    private final EmployeesDAO employeesDAO = new EmployeesDAO();

    private final PasswordRecoveryDAO recoveryDAO =
            new PasswordRecoveryDAO();

    private final EmailService emailService = new EmailService();

    /*
     * Recebe o e-mail informado na tela de recuperação.
     * Solicita o envio do código e encaminha para a verificação
     * sem revelar se o endereço está cadastrado.
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        request.getSession().removeAttribute("passwordRecoveryId");

        String email = request.getParameter("campoEmail");

        if (email == null
                || email.isBlank()
                || email.length() > 255) {

            request.setAttribute(
                    "mensagem",
                    "Informe um endereço de e-mail válido."
            );

            request.getRequestDispatcher("/RecoverPassword.jsp")
                    .forward(request, response);

            return;
        }

        try {
            Long recoveryId = recoveryService.requestRecovery(
                    email.trim(),
                    employeesDAO,
                    recoveryDAO,
                    emailService
            );

            if (recoveryId != null) {
                request.getSession().setAttribute(
                        "passwordRecoveryId",
                        recoveryId
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Falha na solicitação de recuperação de senha.", e
            );

        } catch (IOException e) {
            getServletContext().log(
                    "Falha no envio do e-mail de recuperação.", e
            );
        }

        request.setAttribute(
                "mensagem",
                "Se o e-mail estiver cadastrado, você receberá um código de verificação. Confira sua caixa de entrada."
        );

        request.getRequestDispatcher("/Verification.jsp")
                .forward(request, response);
    }
}

