package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.model.Sectors;
import com.example.servelet1ano.dao.SectorsDAO;
import com.example.servelet1ano.filter.SectorsFilter;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.dao.UnitsDAO;
import com.example.servelet1ano.filter.UnitsFilter;
import com.example.servelet1ano.util.service.AuthorizationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet da tela de setores (/setores)
 * Lista os setores e as unidades da empresa do usuario logado, cadastra novos setores e exclui (desativa) setores
 */
@WebServlet("/setores")
public class SectorRegisterServlet extends HttpServlet {

    /**
     * Registro de logs usado para anotar os erros inesperados
     */
    private static final Logger LOGGER =
            Logger.getLogger(SectorRegisterServlet.class.getName());

    /**
     * Pagina da tela de setores
     */
    private static final String CADASTRO = "/Sectors.jsp";

    /**
     * Mensagem mostrada ao usuario quando der erro no banco ao cadastrar um setor
     */
    private static final String ERRO_BANCO =
            "Não foi possível concluir a operação. Tente novamente mais tarde.";

    /**
     * metodo que mostra a tela de setores da empresa do usuario logado
     * Se nao houver sessao valida, redireciona para o login
     * Se vier o parametro sucesso=true ou excluido=true, mostra a mensagem de setor cadastrado ou excluido
     * @param request A requisicao (a empresa vem da sessao)
     * @param response A resposta que encaminha para a tela de setores ou redireciona para o login
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer)
                || !(session.getAttribute("companyId") instanceof Integer)) {

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }

        // Confere se o funcionário possui acesso administrativo.
        if (!new AuthorizationService().isCompanyAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int companyId = (Integer) session.getAttribute("companyId");

        try {
            carregarDados(request, companyId);

            if ("true".equals(request.getParameter("sucesso"))) {
                request.setAttribute("sucesso", "Setor cadastrado com sucesso.");
            }

            if ("true".equals(request.getParameter("excluido"))) {
                request.setAttribute("sucesso", "Setor excluído com sucesso.");
            }

            request.getRequestDispatcher(CADASTRO)
                    .forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao consultar os dados dos setores e unidades.",
                    e
            );

            request.setAttribute(
                    "erro",
                    "Não foi possível carregar os dados dos setores. "
                            + "Tente novamente mais tarde."
            );

            request.setAttribute("setores", List.of());
            request.setAttribute("unidades", List.of());

            request.getRequestDispatcher(CADASTRO)
                    .forward(request, response);
        }
    }

    /**
     * metodo que cadastra um novo setor ou exclui (desativa) um setor da empresa do usuario logado
     * Se nao houver sessao valida, redireciona para o login
     * Se vier acao=excluir, desativa o setor do id informado (so se ele for da empresa e nao tiver funcionarios ativos)
     * Caso contrario, cadastra um setor novo: confere os campos obrigatorios, o tamanho do nome, se a unidade e da empresa e se o setor ja nao existe na unidade
     * Se algo estiver errado, volta para a tela com a mensagem de erro (mantendo o que foi digitado)
     * Se der certo, redireciona para /setores com o aviso de sucesso
     * @param request A requisicao com a acao e o id (exclusao) ou com os dados do setor (campoNomeSetor, campoIdUnidade e campoDescricao)
     * @param response A resposta que redireciona ou volta para a tela de setores
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer)
                || !(session.getAttribute("companyId") instanceof Integer)) {

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }

        if (!new AuthorizationService().isCompanyAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        int companyId = (Integer) session.getAttribute("companyId");

        if ("excluir".equals(request.getParameter("acao"))) {
            String idParam = request.getParameter("id");

            final int id;

            try {
                if (idParam == null || idParam.isBlank()) {
                    exibirErro(
                            request,
                            response,
                            companyId,
                            "O setor selecionado não é válido."
                    );
                    return;
                }

                id = Integer.parseInt(idParam);

                if (id <= 0) {
                    exibirErro(
                            request,
                            response,
                            companyId,
                            "O setor selecionado não é válido."
                    );
                    return;
                }
            } catch (NumberFormatException e) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "O setor selecionado não é válido."
                );
                return;
            }

            try {
                SectorsDAO sectorsDAO = new SectorsDAO();

                Sectors setor = sectorsDAO.searchById(id);

                if (setor == null || setor.getCompanyId() != companyId) {
                    exibirErro(
                            request,
                            response,
                            companyId,
                            "O setor não foi encontrado ou não pertence à sua empresa."
                    );
                    return;
                }

                // um setor com funcionários ativos não pode ser excluído:
                // antes é preciso mover ou desativar esses funcionários
                if (sectorsDAO.hasActiveEmployees(id)) {
                    exibirErro(
                            request,
                            response,
                            companyId,
                            "Não é possível excluir o setor enquanto houver "
                                    + "funcionários ativos nele. Mova ou desative "
                                    + "os funcionários antes de excluir o setor."
                    );
                    return;
                }

                boolean excluido = sectorsDAO.delete(id);

                if (!excluido) {
                    exibirErro(
                            request,
                            response,
                            companyId,
                            "Não foi possível excluir o setor. Tente novamente."
                    );
                    return;
                }

                response.sendRedirect(
                        request.getContextPath() + "/setores?excluido=true"
                );
                return;

            } catch (RuntimeException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Erro de banco de dados ao excluir um setor.",
                        e
                );

                exibirErro(
                        request,
                        response,
                        companyId,
                        "Não foi possível excluir o setor. Tente novamente mais tarde."
                );
                return;
            }
        }

        String nome = request.getParameter("campoNomeSetor");
        String unidadeIdParam = request.getParameter("campoIdUnidade");
        String descricao = request.getParameter("campoDescricao");


        request.setAttribute("campoNomeSetor", nome);
        request.setAttribute("campoIdUnidade", unidadeIdParam);
        request.setAttribute("campoDescricao", descricao);

        // Valida os campos obrigatórios.
        if (nome == null || nome.isBlank()
                || unidadeIdParam == null || unidadeIdParam.isBlank()
                || descricao == null || descricao.isBlank()) {

            exibirErro(
                    request,
                    response,
                    companyId,
                    "Todos os campos são obrigatórios."
            );
            return;
        }

        nome = nome.trim();
        unidadeIdParam = unidadeIdParam.trim();
        descricao = descricao.trim();

        // Valida o tamanho do nome.
        if (nome.length() > 255) {
            exibirErro(
                    request,
                    response,
                    companyId,
                    "O nome do setor não pode ultrapassar 255 caracteres."
            );
            return;
        }

        final int unidadeId;


        try {
            unidadeId = Integer.parseInt(unidadeIdParam);

            if (unidadeId <= 0) {
                throw new NumberFormatException(
                        "O ID da unidade deve ser positivo."
                );
            }

        } catch (NumberFormatException e) {
            exibirErro(
                    request,
                    response,
                    companyId,
                    "A unidade selecionada não é válida. "
                            + "Selecione uma unidade disponível."
            );
            return;
        }

        try {

            Units unidade = new UnitsDAO().searchById(unidadeId);

            if (unidade == null
                    || unidade.getCompanyId() != companyId) {

                exibirErro(
                        request,
                        response,
                        companyId,
                        "A unidade selecionada não pertence à sua empresa."
                );
                return;
            }


            SectorsFilter filter = new SectorsFilter();
            filter.setCompanyId(companyId);
            filter.setUnitId(unidadeId);
            filter.setName(nome);

            List<Sectors> existentes =
                    new SectorsDAO().searchAll(filter);

            if (!existentes.isEmpty()) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "Já existe um setor com esse nome na unidade selecionada."
                );
                return;
            }


            Sectors setor = new Sectors(
                    unidadeId,
                    companyId,
                    nome,
                    descricao
            );

            boolean cadastrado = new SectorsDAO().register(setor);

            if (!cadastrado) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        ERRO_BANCO
                );
                return;
            }


            response.sendRedirect(
                    request.getContextPath() + "/setores?sucesso=true"
            );
            return;

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro de banco de dados ao validar ou cadastrar um setor.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    companyId,
                    ERRO_BANCO
            );
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Carrega os dados que a tela de setores precisa
     * Busca as unidades da empresa e guarda no atributo "unidades"
     * Busca os setores da empresa e guarda no atributo "setores"
     * Se a consulta falhar, a excecao e repassada para quem chamou tratar o erro
     * @param request A requisicao onde os atributos serao guardados
     * @param companyId O id da empresa do usuario logado
     */
    private void carregarDados(
            HttpServletRequest request,
            int companyId
    ) {
        UnitsFilter unitsFilter = new UnitsFilter();
        unitsFilter.setCompanyId(companyId);

        List<Units> unidades =
                new UnitsDAO().searchAll(unitsFilter);

        SectorsFilter sectorsFilter = new SectorsFilter();
        sectorsFilter.setCompanyId(companyId);

        List<Sectors> setores =
                new SectorsDAO().searchAll(sectorsFilter);

        request.setAttribute("setores", setores);
        request.setAttribute("unidades", unidades);
    }

    /**
     * Mostra uma mensagem de erro na tela de setores
     * Guarda a mensagem no atributo "erro", recarrega os dados da tela
     * e encaminha a mesma requisicao de volta para a pagina, assim o que o usuario digitou continua preenchido
     * Se nao for possivel recarregar os dados, as listas de setores e unidades ficam vazias
     * @param request A requisicao atual
     * @param response A resposta que encaminha para a tela de setores
     * @param companyId O id da empresa do usuario logado
     * @param mensagem O texto do erro que sera mostrado ao usuario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId,
            String mensagem
    ) throws ServletException, IOException {

        request.setAttribute("erro", mensagem);

        try {
            carregarDados(request, companyId);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao recarregar os setores e unidades após uma falha.",
                    e
            );

            request.setAttribute("setores", List.of());
            request.setAttribute("unidades", List.of());
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }
}