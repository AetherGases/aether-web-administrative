package com.example.servelet1ano.dao;


import com.example.servelet1ano.connection.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAO {

    private static final String DASHBOARD_METRICS = """
            SELECT

                (
                    SELECT COUNT(*)
                    FROM sectors
                    WHERE company_id = ?
                      AND is_active = TRUE
                ) AS total_sectors,

                (
                    SELECT COUNT(DISTINCT pgp.permission_id)
                    FROM permission_group_permissions pgp
                    INNER JOIN permission_groups pg
                        ON pg.id = pgp.permission_group_id
                    INNER JOIN permissions p
                        ON p.id = pgp.permission_id
                    WHERE pg.company_id = ?
                      AND pg.is_active = TRUE
                      AND pgp.is_active = TRUE
                      AND p.is_active = TRUE
                ) AS total_permissions,

                (
                    SELECT COUNT(*)
                    FROM employees
                    WHERE company_id = ?
                      AND is_active = TRUE
                ) AS total_employees,

                (
                    SELECT COUNT(*)
                    FROM units
                    WHERE company_id = ?
                      AND is_active = TRUE
                ) AS total_units
            """;

    /**
     * Busca os indicadores do dashboard de uma empresa.
     *
     * Todas as consultas consideram somente registros ativos.
     * As permissões são contadas uma única vez, mesmo que
     * estejam vinculadas a mais de um grupo da empresa.
     *
     * @param companyId ID da empresa
     * @return Mapa com os indicadores do dashboard
     */
    public Map<String, Integer> getMetrics(int companyId) {

        if (companyId <= 0) {
            throw new IllegalArgumentException(
                    "O ID da empresa deve ser positivo."
            );
        }

        Map<String, Integer> metrics = new LinkedHashMap<>();

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt =
                        conn.prepareStatement(DASHBOARD_METRICS)
        ) {

            for (int i = 1; i <= 4; i++) {
                pstmt.setInt(i, companyId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    metrics.put(
                            "totalSectors",
                            rs.getInt("total_sectors")
                    );

                    metrics.put(
                            "totalPermissions",
                            rs.getInt("total_permissions")
                    );

                    metrics.put(
                            "totalEmployees",
                            rs.getInt("total_employees")
                    );

                    metrics.put(
                            "totalUnits",
                            rs.getInt("total_units")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar os indicadores do dashboard.",
                    e
            );
        }

        return metrics;
    }
}
