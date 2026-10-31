package com.example.servelet1ano.util.service;

import com.example.servelet1ano.connection.ConnectionFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Servico responsavel por autorizar o acesso ao site administrativo.
 * Somente gerentes ativos do grupo "Admin da empresa" podem acessar o site.
 */
public class AuthorizationService {

    /**
     * Confere os identificadores da sessao no banco, sem confiar no grupo salvo na sessao.
     * @param request A requisicao atual
     * @return true se a sessao pertence a um gerente autorizado
     */
    public boolean isCompanyAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer employeeId)
                || !(session.getAttribute("companyId") instanceof Integer companyId)) {
            return false;
        }

        return CompanyAdmin(employeeId, companyId);
    }

    /**
     * Valida o gerente diretamente no banco, inclusive durante o login.
     * Verifica funcionario, empresa e grupo ativos e o vinculo entre eles.
     * @param employeeId O id do funcionario
     * @param companyId O id da empresa
     * @return true se a conta tem acesso ao site administrativo
     */
    public boolean CompanyAdmin(int employeeId, int companyId) {
        if (employeeId <= 0 || companyId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM employees e
                JOIN companies c ON c.id = e.company_id
                JOIN permission_groups pg
                  ON pg.id = e.permission_group_id
                 AND pg.company_id = e.company_id
                WHERE e.id = ?
                  AND e.company_id = ?
                  AND e.is_active = TRUE
                  AND e.status = 'ACTIVE'
                  AND c.is_active = TRUE
                  AND pg.is_active = TRUE
                  AND pg.name = 'Admin da empresa'
                LIMIT 1
                """;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao verificar autorizacao administrativa.", e
            );
        }
    }

    /**
     * Prepara o token anti-CSRF dos formularios administrativos.
     * @param request A requisicao atual
     */
    public void CsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        if (!(session.getAttribute("adminCsrfToken") instanceof String)) {
            session.setAttribute("adminCsrfToken", UUID.randomUUID().toString());
        }

        request.setAttribute("csrfToken", session.getAttribute("adminCsrfToken"));
    }

    /**
     * Valida o token anti-CSRF recebido no POST.
     * @param request A requisicao atual
     * @return true se o token pertence a sessao
     */
    public boolean csrfTokenValido(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("adminCsrfToken") instanceof String esperado)) {
            return false;
        }

        String[] valores = request.getParameterValues("csrfToken");

        if (valores == null || valores.length != 1
                || valores[0] == null || valores[0].length() > 128) {
            return false;
        }

        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                valores[0].getBytes(StandardCharsets.UTF_8)
        );
    }
}
