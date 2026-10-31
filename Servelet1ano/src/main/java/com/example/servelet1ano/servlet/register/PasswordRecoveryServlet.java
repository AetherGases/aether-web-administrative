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

/**
 * Servlet da tela de recuperacao de senha (/recover-password)
 * Recebe o e-mail, pede o envio do codigo de verificacao e segue para a tela de verificacao
 */
@WebServlet("/recover-password")
public class PasswordRecoveryServlet extends HttpServlet {

    /**
     * Servico que gera o codigo e registra a solicitacao de recuperacao
     */
    private final PasswordRecoveryService recoveryService =
            new PasswordRecoveryService();

    /**
     * DAO usado para buscar o funcionario pelo e-mail
     */
    private final EmployeesDAO employeesDAO = new EmployeesDAO();

    /**
     * DAO usado para registrar a solicitacao de recuperacao no banco
     */
    private final PasswordRecoveryDAO recoveryDAO =
            new PasswordRecoveryDAO();

    /**
     * Servico usado para enviar o codigo por e-mail
     */
    private final EmailService emailService = new EmailService();

    /**
     * metodo que recebe o e-mail informado na tela de recuperacao
     * Solicita o envio do codigo e encaminha para a tela de verificacao
     * Sempre mostra a mesma mensagem, para nao revelar se o e-mail esta cadastrado
     * Se der certo, guarda o id da solicitacao na sessao (passwordRecoveryId) para a verificacao
     * @param request A requisicao com o e-mail digitado (campoEmail)
     * @param response A resposta que encaminha para a tela de verificacao
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
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