package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.connection.DatabaseConfig;
import com.example.servelet1ano.dao.PasswordRecoveryDAO;
import com.example.servelet1ano.util.PasswordHasher;
import com.example.servelet1ano.util.service.PasswordRecoveryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet da tela de verificacao do codigo de recuperacao de senha (/verify-recovery)
 * Confere o codigo digitado e, se estiver correto, grava a nova senha
 */
@WebServlet("/verify-recovery")
public class VerificationServlet extends HttpServlet{

    /**
     * Servico usado para gerar o hash do codigo digitado
     */
    private final PasswordRecoveryService recoveryService =
            new PasswordRecoveryService();

    /**
     * DAO usado para conferir o codigo e concluir a recuperacao no banco
     */
    private final PasswordRecoveryDAO recoveryDAO =
            new PasswordRecoveryDAO();

    /**
     * metodo que valida o codigo de recuperacao e redefine a senha
     * do funcionario ligado a solicitacao guardada na sessao
     * O codigo chega em seis campos (codigo1 a codigo6) com um digito cada
     * Se algo estiver errado, volta para a tela com a mensagem de erro; se der certo, vai para o login com aviso de sucesso
     * @param request A requisicao com os digitos do codigo, a nova senha e a confirmacao
     * @param response A resposta que redireciona para o login ou volta para a tela de verificacao
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null) {
            request.setAttribute(
                    "mensagem",
                    "Solicitação inválida ou expirada. Inicie a recuperação novamente."
            );

            request.getRequestDispatcher("/RecoverPassword.jsp")
                    .forward(request, response);
            return;
        }

        Object recoveryIdAttribute =
                session.getAttribute("passwordRecoveryId");

        if (!(recoveryIdAttribute instanceof Long)) {
            request.setAttribute(
                    "mensagem",
                    "Solicitação inválida ou expirada. Inicie a recuperação novamente."
            );

            request.getRequestDispatcher("/RecoverPassword.jsp")
                    .forward(request, response);
            return;
        }

        long recoveryId = (Long) recoveryIdAttribute;

        StringBuilder codigoBuilder = new StringBuilder();

        for (int i = 1; i <= 6; i++) {
            String digito = request.getParameter("codigo" + i);

            if (digito == null || !digito.matches("\\d")) {
                request.setAttribute(
                        "mensagem",
                        "Informe os seis dígitos do código."
                );

                request.getRequestDispatcher("/Verification.jsp")
                        .forward(request, response);
                return;
            }

            codigoBuilder.append(digito);
        }

        String codigo = codigoBuilder.toString();
        String novaSenha = request.getParameter("novaSenha");
        String confirmarSenha = request.getParameter("confirmarSenha");

        if (novaSenha == null || confirmarSenha == null
                || novaSenha.isBlank()
                || novaSenha.length() > 1024) {

            request.setAttribute(
                    "mensagem",
                    "Informe uma nova senha válida."
            );

            request.getRequestDispatcher("/Verification.jsp")
                    .forward(request, response);
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            request.setAttribute(
                    "mensagem",
                    "As senhas não coincidem."
            );

            request.getRequestDispatcher("/Verification.jsp")
                    .forward(request, response);
            return;
        }

        try {
            String codeSecret =
                    DatabaseConfig.getPasswordRecoverySecret();

            String codigoHash =
                    recoveryService.hashCode(codigo, codeSecret);

            String novaSenhaHash =
                    PasswordHasher.hash(novaSenha);

            boolean atualizado = recoveryDAO.completeRecovery(
                    recoveryId,
                    codigoHash,
                    novaSenhaHash
            );

            if (!atualizado) {
                request.setAttribute(
                        "mensagem",
                        "Código inválido, expirado ou já utilizado. Confira o código ou solicite outro."
                );

                request.getRequestDispatcher("/Verification.jsp")
                        .forward(request, response);
                return;
            }

            session.removeAttribute("passwordRecoveryId");

            session.setAttribute(
                    "mensagemLogin",
                    "Senha alterada com sucesso. Entre com sua nova senha."
            );

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Erro ao concluir a recuperação de senha.",
                    e
            );

            request.setAttribute(
                    "mensagem",
                    "Não foi possível redefinir a senha agora. Tente novamente."
            );

            request.getRequestDispatcher("/Verification.jsp")
                    .forward(request, response);
        }
    }
}