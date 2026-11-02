package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.SectorsDAO;
import com.example.servelet1ano.dao.UnitsDAO;
import com.example.servelet1ano.filter.SectorsFilter;
import com.example.servelet1ano.filter.UnitsFilter;
import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.util.service.AuthorizationService;
import com.example.servelet1ano.dao.DeactivationDAO;
import com.example.servelet1ano.util.Selection;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Servlet da tela de setores (/setores).
 * Lista, cadastra, edita e desativa setores da empresa do usuario autenticado.
 * Somente administradores ativos da propria empresa podem realizar as operacoes.
 */
@WebServlet("/setores")
public class SectorRegisterServlet extends HttpServlet {

    /** Registro de logs usado para anotar erros inesperados. */
    private static final Logger LOGGER =
            Logger.getLogger(SectorRegisterServlet.class.getName());

    /** Pagina da tela de setores. */
    private static final String CADASTRO = "/WEB-INF/Sectors.jsp";

    /** Mensagem usada quando uma operacao no banco falha. */
    private static final String ERRO_BANCO =
            "Não foi possível concluir a operação. Tente novamente mais tarde.";

    /** Limites definidos pelas colunas name e description da tabela sectors. */
    private static final int MAX_NOME = 150;
    private static final int MAX_DESCRICAO = 255;

    /** Campos aceitos para cada operacao; ids de empresa e unidade nunca sao editados. */
    private static final Set<String> CAMPOS_CADASTRO = Set.of(
            "acao", "campoNomeSetor", "campoIdUnidade", "campoDescricao", "csrfToken"
    );
    private static final Set<String> CAMPOS_EDICAO = Set.of(
            "acao", "id", "campoNomeSetor", "campoDescricao", "csrfToken"
    );
    private static final Set<String> CAMPOS_EXCLUSAO = Set.of(
            "acao", "id", "csrfToken"
    );

    /** Rejeita caracteres de controle e de formatacao invisiveis. */
    private static final Pattern CARACTERES_CONTROLE =
            Pattern.compile("[\\p{Cc}\\p{Cf}]");

    /**
     * Mostra os setores e unidades da empresa do usuario logado.
     * Redireciona usuarios sem sessao e bloqueia funcionarios sem acesso administrativo.
     * @param request Requisicao atual
     * @param response Resposta que encaminha para a pagina
     * @throws ServletException se ocorrer erro no encaminhamento
     * @throws IOException se ocorrer erro de entrada ou saida
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        HttpSession session = request.getSession(false);
        if (!sessaoValida(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            AuthorizationService authorization = new AuthorizationService();
            if (!authorization.isCompanyAdmin(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            int companyId = (Integer) session.getAttribute("companyId");
            authorization.CsrfToken(request);
            carregarDados(request, companyId);

            if ("ok".equals(request.getParameter("lote"))) {
                request.setAttribute("sucesso", "Setores selecionados desativados com sucesso.");
            } else if ("falha".equals(request.getParameter("lote"))) {
                request.setAttribute("erro", "Não foi possível desativar todos os setores. Verifique vínculos e registros ativos.");
            } else if ("true".equals(request.getParameter("sucesso"))) {
                request.setAttribute("sucesso", "Setor cadastrado com sucesso.");
            } else if ("true".equals(request.getParameter("editado"))) {
                request.setAttribute("sucesso", "Setor editado com sucesso.");
            } else if ("true".equals(request.getParameter("excluido"))) {
                request.setAttribute("sucesso", "Setor desativado com sucesso.");
            }

            request.getRequestDispatcher(CADASTRO).forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar a tela de setores.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Processa cadastro, edicao e exclusao logica de setores.
     * Primeiro confere sessao, permissao, parametros e CSRF; depois executa a acao.
     * @param request Requisicao com os campos da operacao
     * @param response Resposta que redireciona ou retorna ao formulario
     * @throws ServletException se ocorrer erro no encaminhamento
     * @throws IOException se ocorrer erro de entrada ou saida
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        HttpSession session = request.getSession(false);
        if (!sessaoValida(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            AuthorizationService authorization = new AuthorizationService();
            if (!authorization.isCompanyAdmin(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String acao = request.getParameter("acao");

            if (acao == null) {
                acao = "cadastrar";
            }

            Set<String> campos = switch (acao) {
                case "cadastrar" -> CAMPOS_CADASTRO;
                case "editar" -> CAMPOS_EDICAO;
                case "excluir" -> CAMPOS_EXCLUSAO;
                case "excluirLote" -> Set.of("acao", "csrfToken", "ids");
                default -> null;
            };

            if (campos == null || (!"excluirLote".equals(acao) && !parametrosValidos(request, campos))) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!authorization.csrfTokenValido(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            int companyId = (Integer) session.getAttribute("companyId");
            switch (acao) {
                case "editar" -> editarSetor(request, response, companyId);
                case "excluir" -> excluirSetor(request, response, companyId);
                case "cadastrar" -> cadastrarSetor(request, response, companyId);
                case "excluirLote" -> excluirLote(request, response, companyId);
                default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Erro inesperado ao processar setor.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cadastra um setor vinculado a uma unidade ativa da propria empresa.
     * Rejeita dados invalidos e nomes repetidos na mesma unidade.
     */
    private void cadastrarSetor(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId
    ) throws ServletException, IOException {

        String nome = normalizar(request.getParameter("campoNomeSetor"));
        String descricao = normalizar(request.getParameter("campoDescricao"));
        Integer unitId = lerId(request.getParameter("campoIdUnidade"));

        request.setAttribute("campoNomeSetor", nome);
        request.setAttribute("campoDescricao", descricao);
        request.setAttribute("campoIdUnidade", request.getParameter("campoIdUnidade"));

        if (!dadosValidos(nome, descricao) || unitId == null) {
            exibirErro(request, response, companyId, "Informe os dados válidos do setor e da unidade.");
            return;
        }

        Units unidade = new UnitsDAO().searchById(unitId);
        if (unidade == null || unidade.getCompanyId() != companyId) {
            exibirErro(request, response, companyId, "A unidade não está disponível para sua empresa.");
            return;
        }

        SectorsFilter filtro = new SectorsFilter();
        filtro.setCompanyId(companyId);
        filtro.setUnitId(unitId);
        filtro.setName(nome);

        // searchAll aplica busca parcial; por isso, confere o nome exato no Java.
        boolean existe = new SectorsDAO().searchAll(filtro).stream()
                .anyMatch(s -> nome.equalsIgnoreCase(s.getName()));

        if (existe) {
            exibirErro(request, response, companyId,
                    "Já existe um setor com esse nome na unidade selecionada.");
            return;
        }

        boolean cadastrado = new SectorsDAO().register(
                new Sectors(unitId, companyId, nome, descricao));

        if (!cadastrado) {
            exibirErro(request, response, companyId, ERRO_BANCO);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/setores?sucesso=true");
    }

    /**
     * Edita somente nome e descricao de um setor da empresa.
     * Nunca altera id, company_id ou unit_id, mesmo por POST manual.
     */
    private void editarSetor(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId
    ) throws ServletException, IOException {
        request.setAttribute("modalErro", "overlayEditarSetor");

        Integer id = lerId(request.getParameter("id"));
        String nome = normalizar(request.getParameter("campoNomeSetor"));
        String descricao = normalizar(request.getParameter("campoDescricao"));

        if (id == null || !dadosValidos(nome, descricao)) {
            exibirErro(request, response, companyId, "Informe dados válidos para editar o setor.");
            return;
        }

        // A verificacao de empresa tambem deve existir no WHERE do DAO.
        boolean atualizado = new SectorsDAO().updateBasicData(id, companyId, nome, descricao);
        if (!atualizado) {
            exibirErro(request, response, companyId,
                    "Setor não encontrado ou indisponível para edição.");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/setores?editado=true");
    }

    /**
     * Desativa um setor da empresa se nao houver funcionarios ativos vinculados.
     * A condicao de empresa e a ausencia de funcionarios sao revalidadas no SQL.
     */
    private void excluirSetor(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId
    ) throws ServletException, IOException {

        Integer id = lerId(request.getParameter("id"));
        if (id == null) {
            exibirErro(request, response, companyId, "O setor selecionado não é válido.");
            return;
        }

        SectorsDAO sectorsDAO = new SectorsDAO();
        Sectors setor = sectorsDAO.searchById(id);
        if (setor == null || setor.getCompanyId() != companyId) {
            exibirErro(request, response, companyId,
                    "O setor não foi encontrado ou não pertence à sua empresa.");
            return;
        }

        if (sectorsDAO.hasActiveEmployees(id)) {
            exibirErro(request, response, companyId,
                    "Não é possível excluir o setor enquanto houver funcionários ativos nele.");
            return;
        }

        if (!sectorsDAO.deleteByIdAndCompany(id, companyId)) {
            exibirErro(request, response, companyId,
                    "O setor não pôde ser desativado. Verifique se ainda possui funcionários ativos.");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/setores?excluido=true");
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Confere se a sessao contem os identificadores validos do usuario e da empresa.
     * @param session Sessao atual ou null
     * @return true quando ambos os identificadores sao validos
     */
    private boolean sessaoValida(HttpSession session) {
        return session != null
                && session.getAttribute("employeeId") instanceof Integer employeeId
                && employeeId > 0
                && session.getAttribute("companyId") instanceof Integer companyId
                && companyId > 0;
    }

    /**
     * Rejeita parametros extras, repetidos ou ausentes para cada acao.
     * O cadastro permite ausencia de acao por compatibilidade com o JSP original.
     */
    private boolean parametrosValidos(HttpServletRequest request, Set<String> permitidos) {
        for (String campo : request.getParameterMap().keySet()) {
            if (!permitidos.contains(campo)) {
                return false;
            }
        }
        for (String campo : permitidos) {
            String[] valores = request.getParameterValues(campo);
            if (valores != null && valores.length != 1) {
                return false;
            }
            if (!"acao".equals(campo) && (valores == null || valores.length == 0)) {
                return false;
            }
        }
        return true;
    }

    /** Converte um identificador positivo sem permitir outros formatos. */
    private Integer lerId(String valor) {
        if (valor == null || !valor.matches("[1-9][0-9]{0,9}")) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Normaliza os textos para evitar diferencas de representacao Unicode. */
    private String normalizar(String valor) {
        return valor == null ? null : Normalizer.normalize(valor.trim(), Normalizer.Form.NFC);
    }

    /** Confere obrigatoriedade, tamanho e caracteres permitidos no banco. */
    private boolean dadosValidos(String nome, String descricao) {
        return nome != null && !nome.isBlank()
                && nome.length() <= MAX_NOME
                && descricao != null && !descricao.isBlank()
                && descricao.length() <= MAX_DESCRICAO
                && !CARACTERES_CONTROLE.matcher(nome).find()
                && !CARACTERES_CONTROLE.matcher(descricao).find();
    }

    /**
     * Carrega os setores e unidades da empresa para a tela.
     * @param request Requisicao que recebe os atributos
     * @param companyId Empresa do usuario autenticado
     */
    private void carregarDados(HttpServletRequest request, int companyId) {
        UnitsFilter unitsFilter = new UnitsFilter();
        unitsFilter.setCompanyId(companyId);
        List<Units> unidades = new UnitsDAO().searchAll(unitsFilter);

        SectorsFilter sectorsFilter = new SectorsFilter();
        sectorsFilter.setCompanyId(companyId);
        List<Sectors> setores = new SectorsDAO().searchAll(sectorsFilter);

        request.setAttribute("setores", setores);
        request.setAttribute("unidades", unidades);
    }

    /**
     * Mostra o erro no formulario e recarrega as listas da empresa.
     * O token CSRF e mantido para que o formulario possa ser reenviado.
     */
    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId,
            String mensagem
    ) throws ServletException, IOException {
        request.setAttribute("erro", mensagem);
        new AuthorizationService().CsrfToken(request);

        try {
            carregarDados(request, companyId);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Erro ao recarregar setores e unidades.", e);
            request.setAttribute("setores", List.of());
            request.setAttribute("unidades", List.of());
        }
        request.getRequestDispatcher(CADASTRO).forward(request, response);
    }

    /** Apenas os IDs recebidos são processados
     * De forma transacional.
     * */
    private void excluirLote(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId
    ) throws IOException {

        boolean ok;

        try {
            List<Integer> ids = Selection.parse(request);

            ok = new DeactivationDAO().sectors(ids, companyId);

        } catch (IllegalArgumentException ex) {
            ok = false;
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/setores?lote="
                        + (ok ? "ok" : "falha")
        );
    }

}
