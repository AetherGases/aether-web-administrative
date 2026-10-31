
package com.example.servelet1ano.util.service;

import com.example.servelet1ano.connection.ConnectionFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Serviço responsável pela autorização dos funcionários.
 * Confere o vínculo entre funcionário, empresa, grupo e permissão.
 */
public class AuthorizationService {

    /**
     * Verifica se o funcionário autenticado pertence ao grupo
     * administrativo ativo da própria empresa.
     *
     * @param request Requisição atual
     * @return true se o funcionário possui acesso administrativo
     */
    public boolean isCompanyAdmin(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer employeeId)
                || !(session.getAttribute("companyId") instanceof Integer companyId)
                || employeeId <= 0
                || companyId <= 0) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM employees e
                JOIN companies c
                  ON c.id = e.company_id
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

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao validar autorização do funcionário.", e
            );
        }
    }

    /**
     * Verifica uma permissão ativa atribuída ao grupo do funcionário.
     *
     * @param request Requisição atual
     * @param permissionName Nome exato da permissão
     * @return true se o funcionário possui a permissão
     */
    public boolean hasPermission(
            HttpServletRequest request,
            String permissionName
    ) {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer employeeId)
                || !(session.getAttribute("companyId") instanceof Integer companyId)
                || employeeId <= 0
                || companyId <= 0
                || permissionName == null
                || permissionName.isBlank()) {
            return false;
        }

        String sql = """
                SELECT 1
                FROM employees e
                JOIN companies c
                  ON c.id = e.company_id
                JOIN permission_groups pg
                  ON pg.id = e.permission_group_id
                 AND pg.company_id = e.company_id
                JOIN permission_group_permissions pgp
                  ON pgp.permission_group_id = pg.id
                JOIN permissions p
                  ON p.id = pgp.permission_id
                WHERE e.id = ?
                  AND e.company_id = ?
                  AND p.name = ?
                  AND e.is_active = TRUE
                  AND e.status = 'ACTIVE'
                  AND c.is_active = TRUE
                  AND pg.is_active = TRUE
                  AND pgp.is_active = TRUE
                  AND p.is_active = TRUE
                LIMIT 1
                """;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, companyId);
            pstmt.setString(3, permissionName);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao consultar permissão do funcionário.", e
            );
        }
    }
}
