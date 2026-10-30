<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Setores | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/Crud.css">
</head>
<body>

<div class="tela">

    <jsp:include page="Sidebar.jsp">
        <jsp:param name="ativo" value="setores"/>
    </jsp:include>

    <h1 class="titulo-pagina">Área restrita</h1>
    <p class="subtitulo-pagina">Setores</p>

    <c:if test="${not empty erro}">
        <p class="mensagem mensagem-erro" role="alert"><c:out value="${erro}"/></p>
    </c:if>
    <c:if test="${not empty sucesso}">
        <p class="mensagem mensagem-sucesso" role="status"><c:out value="${sucesso}"/></p>
    </c:if>

    <jsp:include page="Acoes.jsp">
        <jsp:param name="modal" value="overlayAdicionarSetor"/>
        <jsp:param name="selecionar" value="${not empty setores}"/>
    </jsp:include>

    <%-- Tabela (Figma: TELA CRUDV2 - SETOR) --%>
    <section class="cartao-tabela">
        <table class="tabela">
            <colgroup>
                <col style="width: 64px">
                <col style="width: 102.13px">
                <col style="width: 75px">
                <col style="width: 236px">
                <col style="width: 42px">
                <col style="width: 380.87px">
                <col style="width: 151px">
            </colgroup>
            <thead>
            <tr>
                <th><span class="visually-hidden">Selecionar</span></th>
                <th>Setor</th>
                <th style="text-align: center">ID</th>
                <th style="text-align: center">Unidade</th>
                <th style="text-align: center">Status</th>
                <th style="padding-left: 46.87px">Descrição</th>
                <th><span class="visually-hidden">Ações</span></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="setor" items="${setores}">
                <tr data-id="${setor.id}"
                    data-nome="<c:out value='${setor.name}'/>"
                    data-unidade="<c:out value='${setor.unit.name}'/>"
                    data-unidade-id="${setor.unit.id}"
                    data-descricao="<c:out value='${setor.description}'/>"
                    data-status="${setor.active ? 'Ativo' : 'Inativo'}">
                    <td><input type="checkbox" class="check" aria-label="Selecionar setor ${setor.id}"></td>
                    <td title="<c:out value='${setor.name}'/>"><c:out value="${setor.name}"/></td>
                    <td style="text-align: center"><c:out value="${setor.id}"/></td>
                    <td style="text-align: center" title="<c:out value='${setor.unit.name}'/>"><c:out value="${setor.unit.name}"/></td>
                    <td>
                        <img class="status-icone" style="margin: 0 auto"
                             src="${ctx}/Imagens/${setor.active ? 'STATUSCerto' : 'STATUSErrado'}.svg"
                             alt="${setor.active ? 'Ativo' : 'Inativo'}" title="${setor.active ? 'Ativo' : 'Inativo'}">
                    </td>
                    <td style="padding-left: 46px; padding-right: 34px" title="<c:out value='${setor.description}'/>"><c:out value="${setor.description}"/></td>
                    <td>
                        <div class="acoes">
                            <button type="button" class="acao acao-ver" data-abrir="overlayVerSetor" aria-label="Visualizar">
                                <img src="${ctx}/Imagens/VER.svg" alt="">
                            </button>
                            <button type="button" class="acao acao-editar" data-abrir="overlayEditarSetor" aria-label="Editar">
                                <img src="${ctx}/Imagens/EDITAR.svg" alt="">
                            </button>
                            <button type="button" class="acao acao-excluir" data-abrir="overlayExcluirSetor" aria-label="Desativar"
                                ${setor.active ? '' : 'disabled'}>
                                <img src="${ctx}/Imagens/LIXO.svg" alt="">
                            </button>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <%-- Estado vazio (Figma: TELA CRUDV2 - SEM FUNCIONÁRIOS) --%>
        <c:if test="${empty setores}">
            <jsp:include page="Vazio.jsp">
                <jsp:param name="destaque" value="nenhum setor cadastrado."/>
            </jsp:include>
        </c:if>
    </section>

    <%-- Paginação só aparece quando há registros (no Figma do estado vazio ela não existe) --%>
    <c:if test="${not empty setores}">
        <jsp:include page="Paginacao.jsp"/>
    </c:if>
</div>

<%-- ======================= OVERLAY ADICIONAR SETOR ======================= --%>
<div class="overlay" id="overlayAdicionarSetor" aria-hidden="true"
     data-abrir-ao-carregar="${not empty erro}">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="tituloAdicionarSetor">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/AdicionarFuncionario.svg" alt="">
            <h2 id="tituloAdicionarSetor">Adicionar setor</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/Sectors.jsp" method="post" accept-charset="UTF-8">
            <div class="modal-corpo">
                <div class="campo">
                    <label for="nomeSetor">Nome do setor</label>
                    <input type="text" id="nomeSetor" name="campoNomeSetor" placeholder="setor"
                           maxlength="255" value="<c:out value='${campoNomeSetor}'/>" required>
                </div>

                <div class="campo alto">
                    <label for="descricao">Descrição</label>
                    <textarea id="descricao" name="campoDescricao" placeholder="descrição"
                              required><c:out value="${campoDescricao}"/></textarea>
                </div>

                <div class="campo">
                    <label for="idUnidade">Unidade</label>
                    <select id="idUnidade" name="campoIdUnidade" required>
                        <option value="" disabled ${empty campoIdUnidade ? 'selected' : ''}>unidade</option>
                        <c:forEach var="unidade" items="${unidades}">
                            <option value="${unidade.id}" ${campoIdUnidade eq unidade.id.toString() ? 'selected' : ''}>
                                <c:out value="${unidade.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="modal-rodape">
                <button type="submit" class="botao-roxo adicionar">Adicionar</button>
            </div>
        </form>
    </div>
</div>

<%-- ======================= OVERLAY ALTERAR SETOR ======================= --%>
<div class="overlay" id="overlayEditarSetor" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="tituloEditarSetor">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/AlterarFuncionario.svg" alt="">
            <h2 id="tituloEditarSetor">Alterar setor</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/setores" method="post" accept-charset="UTF-8">
            <input type="hidden" name="acao" value="editar">
            <input type="hidden" name="id" data-campo="id">

            <div class="modal-corpo">
                <div class="campo">
                    <label for="editarNomeSetor">Nome do setor</label>
                    <input type="text" id="editarNomeSetor" name="campoNomeSetor" placeholder="setor"
                           maxlength="255" data-campo="nome" required>
                </div>

                <div class="campo alto">
                    <label for="editarDescricao">Descrição</label>
                    <textarea id="editarDescricao" name="campoDescricao" placeholder="descrição"
                              data-campo="descricao" required></textarea>
                </div>

                <div class="campo">
                    <label for="editarIdUnidade">Unidade</label>
                    <select id="editarIdUnidade" name="campoIdUnidade" data-campo="unidadeId" required>
                        <option value="" disabled>unidade</option>
                        <c:forEach var="unidade" items="${unidades}">
                            <option value="${unidade.id}"><c:out value="${unidade.name}"/></option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="modal-rodape">
                <%-- O SectorRegisterServlet ainda não trata acao=editar; o botão fica desativado
                     para não cadastrar um setor duplicado. Remova o "disabled" quando o servlet tratar a edição. --%>
                <button type="submit" class="botao-roxo" disabled>Alterar</button>
                <p class="aviso-modal">Edição ainda não disponível no servidor.</p>
            </div>
        </form>
    </div>
</div>

<%-- ======================= OVERLAY VER SETOR ======================= --%>
<div class="overlay" id="overlayVerSetor" aria-hidden="true">
    <div class="modal modo-ver" role="dialog" aria-modal="true" aria-labelledby="tituloVerSetor">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/VER.svg" alt="" style="width: 35px; height: 25px; top: 20px; filter: brightness(0) invert(1)">
            <h2 id="tituloVerSetor">Ver setor</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <div class="modal-corpo">
            <div class="campo">
                <label for="verNomeSetor">setor</label>
                <input type="text" id="verNomeSetor" data-campo="nome" readonly>
            </div>
            <div class="campo">
                <label for="verIdSetor">id</label>
                <input type="text" id="verIdSetor" data-campo="id" readonly>
            </div>
            <div class="campo">
                <label for="verUnidadeSetor">unidade</label>
                <input type="text" id="verUnidadeSetor" data-campo="unidade" readonly>
            </div>
            <div class="campo">
                <label for="verDescricaoSetor">descrição</label>
                <input type="text" id="verDescricaoSetor" data-campo="descricao" readonly>
            </div>
        </div>
        <div class="modal-rodape"></div>
    </div>
</div>

<%-- ======================= OVERLAY EXCLUIR SETOR ======================= --%>
<div class="overlay" id="overlayExcluirSetor" aria-hidden="true">
    <div class="modal modal-excluir" role="dialog" aria-modal="true" aria-labelledby="tituloExcluirSetor">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/lixeira.svg" alt="" style="width: 31.5px; height: 35px; left: 40px; top: 15px">
            <h2 id="tituloExcluirSetor" style="left: 85px">Deletar</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/setores" method="post">
            <input type="hidden" name="acao" value="excluir">
            <input type="hidden" name="id" data-campo="id">

            <div class="corpo-excluir">
                <img src="${ctx}/Imagens/perigo.svg" alt="">
                <p>Tem certeza que deseja deletar o(s) registro(s) selecionado(s)? Essa ação não poderá ser desfeita.</p>
            </div>

            <div class="botoes-excluir">
                <button type="button" class="botao-cancelar" data-fechar>Cancelar</button>
                <button type="submit" class="botao-deletar">Deletar</button>
            </div>
        </form>
    </div>
</div>

<script src="${ctx}/Crud.js"></script>
</body>
</html>


