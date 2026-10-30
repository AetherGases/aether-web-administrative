<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Funcionários | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/Crud.css">
</head>
<body>

<div class="tela">

    <jsp:include page="Sidebar.jsp">
        <jsp:param name="ativo" value="funcionarios"/>
    </jsp:include>

    <h1 class="titulo-pagina">Área restrita</h1>
    <p class="subtitulo-pagina">Funcionários</p>

    <c:if test="${not empty erro}">
        <p class="mensagem mensagem-erro" role="alert"><c:out value="${erro}"/></p>
    </c:if>
    <c:if test="${not empty sucesso}">
        <p class="mensagem mensagem-sucesso" role="status"><c:out value="${sucesso}"/></p>
    </c:if>

    <jsp:include page="Acoes.jsp">
        <jsp:param name="modal" value="overlayAdicionarFuncionario"/>
        <jsp:param name="selecionar" value="${not empty funcionarios}"/>
    </jsp:include>

    <%-- Tabela (Figma: TELA CRUDV2 - FUNCIONARIOS).
         A lista vem do atributo "funcionarios" (List<Employees>). --%>
    <section class="cartao-tabela">
        <table class="tabela">
            <colgroup>
                <col style="width: 64px">
                <col style="width: 196.13px">
                <col style="width: 196px">
                <col style="width: 55px">
                <col style="width: 132px">
                <col style="width: 93px">
                <col style="width: 135px">
                <col style="width: 179.87px">
            </colgroup>
            <thead>
            <tr>
                <th><span class="visually-hidden">Selecionar</span></th>
                <th>Nome</th>
                <th>E-mail</th>
                <th style="text-align: center">Status</th>
                <th style="text-align: center">Senha</th>
                <th style="text-align: center">Setor</th>
                <th style="text-align: center">Data de admissão</th>
                <th><span class="visually-hidden">Ações</span></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="funcionario" items="${funcionarios}">
                <c:set var="ativo" value="${funcionario.status eq 'ACTIVE'}" />
                <c:set var="statusTexto"
                       value="${funcionario.status eq 'ACTIVE' ? 'Ativo' : (funcionario.status eq 'IN_VACATION' ? 'Em recesso' : 'Inativo')}" />
                <fmt:formatDate var="admissao" value="${funcionario.createdAt}" pattern="dd-MM-yyyy" />
                <tr data-id="${funcionario.id}"
                    data-nome="<c:out value='${funcionario.name}'/>"
                    data-email="<c:out value='${funcionario.email}'/>"
                    data-setor="<c:out value='${funcionario.sector.name}'/>"
                    data-admissao="${admissao}"
                    data-status="${statusTexto}">
                    <td><input type="checkbox" class="check" aria-label="Selecionar funcionário ${funcionario.id}"></td>
                    <td style="padding-right: 30px" title="<c:out value='${funcionario.name}'/>"><c:out value="${funcionario.name}"/></td>
                    <td style="padding-right: 20px" title="<c:out value='${funcionario.email}'/>"><c:out value="${funcionario.email}"/></td>
                    <td>
                        <img class="status-icone" style="margin: 0 auto"
                             src="${ctx}/Imagens/${ativo ? 'STATUSCerto' : 'STATUSErrado'}.svg"
                             alt="${statusTexto}" title="${statusTexto}">
                    </td>
                        <%-- A senha nunca é exibida: mostramos apenas a máscara. --%>
                    <td style="text-align: center" class="texto-secundario">••••••••</td>
                    <td style="text-align: center; padding: 0 4px" title="<c:out value='${funcionario.sector.name}'/>"><c:out value="${funcionario.sector.name}"/></td>
                    <td style="text-align: center">${admissao}</td>
                    <td style="padding-left: 28.87px">
                        <div class="acoes">
                            <button type="button" class="acao acao-ver" data-abrir="overlayVerFuncionario" aria-label="Visualizar">
                                <img src="${ctx}/Imagens/VER.svg" alt="">
                            </button>
                            <button type="button" class="acao acao-editar" data-abrir="overlayEditarFuncionario" aria-label="Editar">
                                <img src="${ctx}/Imagens/EDITAR.svg" alt="">
                            </button>
                            <button type="button" class="acao acao-excluir" data-abrir="overlayExcluirFuncionario" aria-label="Excluir">
                                <img src="${ctx}/Imagens/LIXO.svg" alt="">
                            </button>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <%-- Estado vazio (Figma: TELA CRUDV2 - SEM FUNCIONÁRIOS) --%>
        <c:if test="${empty funcionarios}">
            <jsp:include page="Vazio.jsp">
                <jsp:param name="destaque" value="nenhum funcionário cadastrado."/>
            </jsp:include>
        </c:if>
    </section>

    <%-- Paginação só aparece quando há registros (no Figma do estado vazio ela não existe) --%>
    <c:if test="${not empty funcionarios}">
        <jsp:include page="Paginacao.jsp"/>
    </c:if>
</div>

<%-- ======================= OVERLAY ADICIONAR FUNCIONÁRIO =======================
     Grade do Figma: colunas x=98 / x=456, linhas a cada 80px, status centralizado.
     Telefone, CPF e Unidade foram incluídos porque o EmployeeRegisterServlet os exige. --%>
<div class="overlay" id="overlayAdicionarFuncionario" aria-hidden="true"
     data-abrir-ao-carregar="${not empty erro}">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="tituloAdicionarFuncionario">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/AdicionarFuncionario.svg" alt="">
            <h2 id="tituloAdicionarFuncionario">Adicionar funcionário</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/funcionarios" method="post" accept-charset="UTF-8">
            <div class="modal-corpo">
                <div class="campo">
                    <label for="nomeFuncionario">Nome completo</label>
                    <input type="text" id="nomeFuncionario" name="campoNomeFuncionario" placeholder="nome"
                           maxlength="255" value="<c:out value='${campoNomeFuncionario}'/>" required>
                </div>
                <div class="campo">
                    <label for="email">E-mail</label>
                    <input type="email" id="email" name="campoEmail" placeholder="e-mail"
                           maxlength="255" value="<c:out value='${campoEmail}'/>" required>
                </div>
                <div class="campo">
                    <label for="telefone">Telefone</label>
                    <input type="tel" id="telefone" name="campoTelefone" placeholder="telefone"
                           value="<c:out value='${campoTelefone}'/>" required>
                </div>
                <div class="campo">
                    <label for="cpf">CPF</label>
                    <input type="text" id="cpf" name="campoCPF" placeholder="CPF"
                           value="<c:out value='${campoCPF}'/>" required>
                </div>
                <div class="campo">
                    <label for="unitId">Unidade</label>
                    <select id="unitId" name="unitId" required>
                        <option value="" disabled ${empty campoUnidade ? 'selected' : ''}>unidade</option>
                        <c:forEach var="unidade" items="${unidades}">
                            <option value="${unidade.id}" ${campoUnidade eq unidade.id.toString() ? 'selected' : ''}>
                                <c:out value="${unidade.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="campo">
                    <label for="sectorId">Setor</label>
                    <select id="sectorId" name="sectorId" required>
                        <option value="" disabled ${empty campoSetor ? 'selected' : ''}>setor</option>
                        <c:forEach var="setor" items="${setores}">
                            <option value="${setor.id}" ${campoSetor eq setor.id.toString() ? 'selected' : ''}>
                                <c:out value="${setor.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="campo">
                    <label for="senha">Senha</label>
                    <input type="password" id="senha" name="campoSenha" placeholder="senha"
                           maxlength="1024" autocomplete="new-password" required>
                </div>
                <div class="campo">
                    <label for="confirmarSenha">Confirmar senha</label>
                    <input type="password" id="confirmarSenha" name="campoConfirmarSenha" placeholder="confirmar senha"
                           maxlength="1024" autocomplete="new-password" required>
                </div>
                <div class="campo largo">
                    <label for="status">Status</label>
                    <select id="status" name="status" required>
                        <option value="" disabled ${empty campoStatus ? 'selected' : ''}>status</option>
                        <option value="Ativo" ${campoStatus eq 'Ativo' ? 'selected' : ''}>Ativo</option>
                        <option value="Inativo" ${campoStatus eq 'Inativo' ? 'selected' : ''}>Inativo</option>
                        <option value="Em recesso" ${campoStatus eq 'Em recesso' ? 'selected' : ''}>Em recesso</option>
                    </select>
                </div>
            </div>

            <div class="modal-rodape">
                <button type="submit" class="botao-roxo adicionar">Adicionar</button>
            </div>
        </form>
    </div>
</div>

<%-- ======================= OVERLAY ALTERAR FUNCIONÁRIO ======================= --%>
<div class="overlay" id="overlayEditarFuncionario" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="tituloEditarFuncionario">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/AlterarFuncionario.svg" alt="">
            <h2 id="tituloEditarFuncionario">Alterar funcionário</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/funcionarios" method="post" accept-charset="UTF-8">
            <input type="hidden" name="acao" value="editar">
            <input type="hidden" name="id" data-campo="id">
            <div class="modal-corpo">
                <div class="campo">
                    <label for="editarNome">Nome</label>
                    <input type="text" id="editarNome" name="campoNomeFuncionario" placeholder="nome" data-campo="nome">
                </div>
                <div class="campo">
                    <label for="editarEmail">E-mail</label>
                    <input type="email" id="editarEmail" name="campoEmail" placeholder="e-mail" data-campo="email">
                </div>
                <div class="campo">
                    <label for="editarSetor">Setor</label>
                    <input type="text" id="editarSetor" placeholder="setor" data-campo="setor">
                </div>
                <div class="campo">
                    <label for="editarAdmissao">Data de admissão</label>
                    <input type="text" id="editarAdmissao" placeholder="data admissão" data-campo="admissao" readonly>
                </div>
                <div class="campo">
                    <label for="editarSenha">Senha</label>
                    <input type="password" id="editarSenha" name="campoSenha" placeholder="senha" autocomplete="new-password">
                </div>
                <div class="campo">
                    <label for="editarConfirmar">Confirmar senha</label>
                    <input type="password" id="editarConfirmar" name="campoConfirmarSenha" placeholder="confirmar senha" autocomplete="new-password">
                </div>
                <div class="campo largo">
                    <label for="editarStatus">Status</label>
                    <select id="editarStatus" name="status" data-campo="status">
                        <option value="Ativo">Ativo</option>
                        <option value="Inativo">Inativo</option>
                        <option value="Em recesso">Em recesso</option>
                    </select>
                </div>
            </div>

            <div class="modal-rodape">
                <%-- O EmployeeRegisterServlet ainda não trata edição; habilite quando houver suporte. --%>
                <button type="submit" class="botao-roxo" disabled>Alterar</button>
                <p class="aviso-modal">Edição ainda não disponível no servidor.</p>
            </div>
        </form>
    </div>
</div>

<%-- ======================= OVERLAY VER FUNCIONÁRIO ======================= --%>
<div class="overlay" id="overlayVerFuncionario" aria-hidden="true">
    <div class="modal modo-ver" role="dialog" aria-modal="true" aria-labelledby="tituloVerFuncionario">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/VER.svg" alt="" style="width: 35px; height: 25px; top: 20px; filter: brightness(0) invert(1)">
            <h2 id="tituloVerFuncionario">Ver funcionário</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <div class="modal-corpo">
            <div class="campo">
                <label for="verNome">nome</label>
                <input type="text" id="verNome" data-campo="nome" readonly>
            </div>
            <div class="campo">
                <label for="verEmail">e-mail</label>
                <input type="text" id="verEmail" data-campo="email" readonly>
            </div>
            <div class="campo">
                <label for="verSetor">setor</label>
                <input type="text" id="verSetor" data-campo="setor" readonly>
            </div>
            <div class="campo">
                <label for="verAdmissao">data admissão</label>
                <input type="text" id="verAdmissao" data-campo="admissao" readonly>
            </div>
            <div class="campo">
                <label for="verSenha">senha atual</label>
                <input type="password" id="verSenha" value="********" readonly>
            </div>
            <div class="campo">
                <label for="verStatus">status</label>
                <input type="text" id="verStatus" data-campo="status" readonly>
            </div>
        </div>
        <div class="modal-rodape"></div>
    </div>
</div>

<%-- ======================= OVERLAY EXCLUIR FUNCIONÁRIO ======================= --%>
<div class="overlay" id="overlayExcluirFuncionario" aria-hidden="true">
    <div class="modal modal-excluir" role="dialog" aria-modal="true" aria-labelledby="tituloExcluirFuncionario">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/lixeira.svg" alt="" style="width: 31.5px; height: 35px; left: 40px; top: 15px">
            <h2 id="tituloExcluirFuncionario" style="left: 85px">Deletar</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/funcionarios" method="post">
            <input type="hidden" name="acao" value="excluir">
            <input type="hidden" name="id" data-campo="id">

            <div class="corpo-excluir">
                <img src="${ctx}/Imagens/perigo.svg" alt="">
                <p>Tem certeza que deseja deletar o(s) registro(s) selecionado(s)? Essa ação não poderá ser desfeita.</p>
            </div>

            <div class="botoes-excluir">
                <button type="button" class="botao-cancelar" data-fechar>Cancelar</button>
                <button type="submit" class="botao-deletar" disabled title="Exclusão ainda não disponível no servidor">Deletar</button>
            </div>
        </form>
    </div>
</div>

<script src="${ctx}/Crud.js"></script>
</body>
</html>


