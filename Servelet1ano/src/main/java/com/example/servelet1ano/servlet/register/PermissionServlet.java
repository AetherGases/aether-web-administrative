package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Employees;
import com.example.servelet1ano.util.service.AuthorizationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Exibe e altera as permissoes de funcionarios da empresa do gerente autenticado. */
@WebServlet("/permissoes")
public class PermissionServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PermissionServlet.class.getName());
    private static final String PAGINA = "/WEB-INF/PermissionsOne.jsp";
    private static final String GRUPO_ADMIN = "Admin da empresa";
    private static final String PREFIXO_GRUPO = "Permissões funcionário ";
    private static final Set<Integer> IDS_PERMITIDOS = Set.of(1, 2, 3, 4, 5, 6, 7);
    private final AuthorizationService authorization = new AuthorizationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        Integer companyId = autorizar(request, response);
        if (companyId == null) return;
        mostrarPagina(request, response, companyId);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        Integer companyId = autorizar(request, response);
        if (companyId == null) return;

        if (!authorization.csrfTokenValido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // O formulario so pode enviar o funcionario, o token e as permissoes marcadas.
        if (!request.getParameterMap().keySet()
                .stream().allMatch(Set.of("funcionario", "csrfToken", "permissoes")::contains)
                || request.getParameterValues("funcionario") == null
                || request.getParameterValues("funcionario").length != 1) {
            mostrarErro(request, response, companyId, "Os dados enviados são inválidos.");
            return;
        }

        Integer funcionarioId = lerId(request.getParameter("funcionario"));
        if (funcionarioId == null) {
            mostrarErro(request, response, companyId, "Selecione um funcionário válido.");
            return;
        }

        Set<Integer> escolhidas = new HashSet<>();
        String[] valores = request.getParameterValues("permissoes");
        if (valores != null) {
            if (valores.length > IDS_PERMITIDOS.size()) {
                mostrarErro(request, response, companyId, "Quantidade de permissões inválida.");
                return;
            }
            for (String valor : valores) {
                Integer id = lerId(valor);
                if (id == null || !IDS_PERMITIDOS.contains(id) || !escolhidas.add(id)) {
                    mostrarErro(request, response, companyId, "Uma das permissões é inválida.");
                    return;
                }
            }
        }

        try {
            salvarPermissoes(companyId, funcionarioId, escolhidas);
            response.sendRedirect(request.getContextPath() + "/permissoes?funcionario="
                    + funcionarioId + "&sucesso=1");
        } catch (IllegalArgumentException e) {
            mostrarErro(request, response, companyId, e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar permissões do funcionário.", e);
            mostrarErro(request, response, companyId, "Não foi possível salvar as permissões.");
        }
    }

    /** Redireciona quem nao esta logado e nega acesso a quem nao e gerente. */
    private Integer autorizar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer)
                || !(session.getAttribute("companyId") instanceof Integer companyId)
                || companyId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (!authorization.isCompanyAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return companyId;
    }

    /** Carrega os funcionarios e as permissoes reais do funcionario selecionado. */
    private void mostrarPagina(HttpServletRequest request, HttpServletResponse response, int companyId)
            throws ServletException, IOException {
        authorization.CsrfToken(request);
        try (Connection conn = ConnectionFactory.connect()) {
            List<Employees> funcionarios = buscarFuncionarios(conn, companyId);
            Integer selecionado = lerId(request.getParameter("funcionario"));
            if (selecionado == null && !funcionarios.isEmpty()) {
                selecionado = funcionarios.getFirst().getId();
            }
            final Integer idSelecionado = selecionado;
            if (selecionado != null && funcionarios.stream().noneMatch(f -> f.getId() == idSelecionado)) {
                selecionado = funcionarios.isEmpty() ? null : funcionarios.getFirst().getId();
            }
            Set<Integer> ativas = selecionado == null
                    ? Set.of() : buscarPermissoes(conn, companyId, selecionado);

            request.setAttribute("funcionarios", funcionarios);
            request.setAttribute("funcionarioSelecionado", selecionado);
            request.setAttribute("permissoesAtivas", ativas);
            if ("1".equals(request.getParameter("sucesso"))) {
                request.setAttribute("sucesso", "Permissões salvas com sucesso.");
            }
            request.getRequestDispatcher(PAGINA).forward(request, response);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar tela de permissões.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void mostrarErro(HttpServletRequest request, HttpServletResponse response,
                             int companyId, String mensagem) throws ServletException, IOException {
        request.setAttribute("erro", mensagem);
        mostrarPagina(request, response, companyId);
    }

    /** Lista apenas funcionarios ativos da empresa, excluindo os administradores. */
    private List<Employees> buscarFuncionarios(Connection conn, int companyId) throws SQLException {
        String sql = """
                SELECT e.id, e.name, e.email
                FROM employees e
                LEFT JOIN permission_groups pg ON pg.id = e.permission_group_id
                WHERE e.company_id = ? AND e.is_active = TRUE
                  AND e.status = 'ACTIVE'
                  AND (pg.id IS NULL OR (pg.company_id = e.company_id
                       AND pg.name <> ?))
                ORDER BY e.name, e.id
                """;
        List<Employees> funcionarios = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            ps.setString(2, GRUPO_ADMIN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    funcionarios.add(new Employees(rs.getInt("id"), rs.getString("name"),
                            rs.getString("email"), null, null));
                }
            }
        }
        return funcionarios;
    }

    /** Retorna os IDs das permissoes do grupo atual do funcionario. */
    private Set<Integer> buscarPermissoes(Connection conn, int companyId, int funcionarioId)
            throws SQLException {
        String sql = """
                SELECT pgp.permission_id
                FROM employees e
                JOIN permission_groups pg ON pg.id = e.permission_group_id
                    AND pg.company_id = e.company_id AND pg.is_active = TRUE
                JOIN permission_group_permissions pgp ON pgp.permission_group_id = pg.id
                    AND pgp.is_active = TRUE
                JOIN permissions p ON p.id = pgp.permission_id AND p.is_active = TRUE
                WHERE e.id = ? AND e.company_id = ? AND e.is_active = TRUE
                """;
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, funcionarioId);
            ps.setInt(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    /** Cria (se preciso) um grupo exclusivo e atualiza os vinculos numa unica transacao. */
    private void salvarPermissoes(int companyId, int funcionarioId, Set<Integer> escolhidas)
            throws SQLException {
        try (Connection conn = ConnectionFactory.connect()) {
            conn.setAutoCommit(false);
            try {
                String atual = null;
                Integer grupoAtualId = null;
                String sqlFuncionario = """
                        SELECT e.permission_group_id, pg.name
                        FROM employees e
                        LEFT JOIN permission_groups pg ON pg.id = e.permission_group_id
                        WHERE e.id = ? AND e.company_id = ? AND e.is_active = TRUE
                          AND e.status = 'ACTIVE'
                        FOR UPDATE OF e
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sqlFuncionario)) {
                    ps.setInt(1, funcionarioId);
                    ps.setInt(2, companyId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Funcionário não encontrado.");
                        int id = rs.getInt("permission_group_id");
                        if (!rs.wasNull()) grupoAtualId = id;
                        atual = rs.getString("name");
                    }
                }
                if (GRUPO_ADMIN.equals(atual)) {
                    throw new IllegalArgumentException("Não é permitido alterar permissões administrativas aqui.");
                }
                if (!escolhidas.isEmpty()) {
                    String sqlVerifica = "SELECT id FROM permissions WHERE is_active = TRUE AND id = ANY (?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlVerifica)) {
                        ps.setArray(1, conn.createArrayOf("integer", escolhidas.toArray(new Integer[0])));
                        int encontrados = 0;
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) encontrados++;
                        }
                        if (encontrados != escolhidas.size()) {
                            throw new IllegalArgumentException("Alguma permissão não está cadastrada no banco.");
                        }
                    }
                }

                String nomeExclusivo = PREFIXO_GRUPO + funcionarioId;
                int grupoId;
                if (nomeExclusivo.equals(atual) && grupoAtualId != null) {
                    grupoId = grupoAtualId;
                } else {
                    String sqlGrupo = "INSERT INTO permission_groups (name, company_id) VALUES (?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlGrupo, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, nomeExclusivo);
                        ps.setInt(2, companyId);
                        ps.executeUpdate();
                        try (ResultSet ids = ps.getGeneratedKeys()) {
                            if (!ids.next()) throw new SQLException("Grupo sem ID gerado.");
                            grupoId = ids.getInt(1);
                        }
                    }
                    String sqlVinculo = "UPDATE employees SET permission_group_id = ?, "
                            + "updated_at = CURRENT_TIMESTAMP WHERE id = ? AND company_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlVinculo)) {
                        ps.setInt(1, grupoId);
                        ps.setInt(2, funcionarioId);
                        ps.setInt(3, companyId);
                        if (ps.executeUpdate() != 1) throw new SQLException("Funcionário não atualizado.");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM permission_group_permissions WHERE permission_group_id = ?")) {
                    ps.setInt(1, grupoId);
                    ps.executeUpdate();
                }
                String sqlInsere = "INSERT INTO permission_group_permissions "
                        + "(permission_group_id, permission_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlInsere)) {
                    for (int id : escolhidas) {
                        ps.setInt(1, grupoId);
                        ps.setInt(2, id);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                try { conn.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }

    private Integer lerId(String valor) {
        if (valor == null || !valor.matches("[1-9][0-9]{0,9}")) return null;
        try { return Integer.valueOf(valor); }
        catch (NumberFormatException e) { return null; }
    }
}
