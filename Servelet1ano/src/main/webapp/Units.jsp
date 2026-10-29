<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Unidades | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/Crud.css">
</head>
<body>

<div class="tela">

    <jsp:include page="Sidebar.jsp">
        <jsp:param name="ativo" value="unidades"/>
    </jsp:include>

    <h1 class="titulo-pagina">Área restrita</h1>
    <p class="subtitulo-pagina">Unidades</p>

    <c:if test="${not empty erro}">
        <p class="mensagem mensagem-erro" role="alert"><c:out value="${erro}"/></p>
    </c:if>
    <c:if test="${not empty sucesso}">
        <p class="mensagem mensagem-sucesso" role="status"><c:out value="${sucesso}"/></p>
    </c:if>

    <jsp:include page="Acoes.jsp">
        <jsp:param name="modal" value="overlayAdicionarUnidade"/>
        <jsp:param name="selecionar" value="${not empty unidades}"/>
    </jsp:include>

    <%-- Tabela (Figma: TELA CRUDV2 - UNIDADE) --%>
    <section class="cartao-tabela">
        <table class="tabela">
            <colgroup>
                <col style="width: 64px">
                <col style="width: 102.13px">
                <col style="width: 75px">
                <col style="width: 236px">
                <col style="width: 42px">
                <col style="width: 162px">
                <col style="width: 78px">
                <col style="width: 112px">
                <col style="width: 179.87px">
            </colgroup>
            <thead>
            <tr>
                <th><span class="visually-hidden">Selecionar</span></th>
                <th>Nome</th>
                <th style="text-align: center">ID</th>
                <th style="text-align: center">Endereço</th>
                <th style="text-align: center">Status</th>
                <th style="text-align: center">CNPJ</th>
                <th style="text-align: center">CNAE</th>
                <th style="text-align: center">Empresa</th>
                <th><span class="visually-hidden">Ações</span></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="unidade" items="${unidades}">
                <c:set var="endereco">
                    <c:if test="${not empty unidade.address}"><c:out value="${unidade.address.street}"/>, <c:out value="${unidade.address.number}"/><c:if test="${not empty unidade.address.complement}"> - <c:out value="${unidade.address.complement}"/></c:if></c:if>
                </c:set>
                <c:set var="enderecoCompleto">
                    <c:if test="${not empty unidade.address}">${endereco} — <c:out value="${unidade.address.neighborhood}"/>, <c:out value="${unidade.address.city}"/>/<c:out value="${unidade.address.state}"/> — CEP <c:out value="${unidade.address.zipCode}"/></c:if>
                </c:set>
                <tr data-id="${unidade.id}"
                    data-nome="<c:out value='${unidade.name}'/>"
                    data-endereco="${enderecoCompleto}"
                    data-cnpj="<c:out value='${unidade.cnpj}'/>"
                    data-cnae="<c:out value='${unidade.cnae}'/>"
                    data-empresa="<c:out value='${unidade.company.name}'/>"
                    data-status="${unidade.active ? 'Ativa' : 'Inativa'}">
                    <td><input type="checkbox" class="check" aria-label="Selecionar unidade ${unidade.id}"></td>
                    <td title="<c:out value='${unidade.name}'/>"><c:out value="${unidade.name}"/></td>
                    <td style="text-align: center"><c:out value="${unidade.id}"/></td>
                    <td style="text-align: center; padding: 0 8px" title="${enderecoCompleto}">${endereco}</td>
                    <td>
                        <img class="status-icone" style="margin: 0 auto"
                             src="${ctx}/Imagens/${unidade.active ? 'STATUSCerto' : 'STATUSErrado'}.svg"
                             alt="${unidade.active ? 'Ativa' : 'Inativa'}" title="${unidade.active ? 'Ativa' : 'Inativa'}">
                    </td>
                    <td style="text-align: center"><c:out value="${unidade.cnpj}"/></td>
                    <td style="text-align: center"><c:out value="${unidade.cnae}"/></td>
                    <td style="text-align: center; padding: 0 6px" title="<c:out value='${unidade.company.name}'/>"><c:out value="${unidade.company.name}"/></td>
                    <td style="padding-left: 28.87px">
                        <div class="acoes">
                            <button type="button" class="acao acao-ver" data-abrir="overlayVerUnidade" aria-label="Visualizar">
                                <img src="${ctx}/Imagens/VER.svg" alt="">
                            </button>
                            <button type="button" class="acao acao-editar" data-abrir="overlayEditarUnidade" aria-label="Editar">
                                <img src="${ctx}/Imagens/EDITAR.svg" alt="">
                            </button>
                            <button type="button" class="acao acao-excluir" disabled
                                    title="Exclusão de unidades ainda não disponível no servidor" aria-label="Excluir">
                                <img src="${ctx}/Imagens/LIXO.svg" alt="">
                            </button>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <%-- Estado vazio (Figma: TELA CRUDV2 - SEM FUNCIONÁRIOS) --%>
        <c:if test="${empty unidades}">
            <jsp:include page="Vazio.jsp">
                <jsp:param name="destaque" value="nenhuma unidade cadastrada."/>
            </jsp:include>
        </c:if>
    </section>

    <%-- Paginação só aparece quando há registros (no Figma do estado vazio ela não existe) --%>
    <c:if test="${not empty unidades}">
        <jsp:include page="Paginacao.jsp"/>
    </c:if>
</div>

<%-- ======================= OVERLAY ADICIONAR UNIDADE =======================
     O Figma traz um único campo "endereço"; o UnitsRegisterServlet exige o endereço
     separado em partes, então os campos seguem a mesma grade de 2 colunas (x=98 / x=456). --%>
<div class="overlay" id="overlayAdicionarUnidade" aria-hidden="true"
     data-abrir-ao-carregar="${not empty erro}">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="tituloAdicionarUnidade">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/AdicionarFuncionario.svg" alt="">
            <h2 id="tituloAdicionarUnidade">Adicionar unidade</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/unidades" method="post" accept-charset="UTF-8">
            <div class="modal-corpo">
                <div class="campo">
                    <label for="nomeUnidade">Nome da unidade</label>
                    <input type="text" id="nomeUnidade" name="campoNomeUnidade" placeholder="nome"
                           maxlength="255" value="<c:out value='${campoNomeUnidade}'/>" required>
                </div>
                <div class="campo">
                    <label for="cnpj">CNPJ</label>
                    <input type="text" id="cnpj" name="campoCNPJ" placeholder="CNPJ"
                           value="<c:out value='${campoCNPJ}'/>" required>
                </div>
                <div class="campo">
                    <label for="cnae">CNAE</label>
                    <input type="text" id="cnae" name="campoCNAE" placeholder="CNAE"
                           value="<c:out value='${campoCNAE}'/>" required>
                </div>
                <div class="campo">
                    <label for="cep">CEP</label>
                    <input type="text" id="cep" name="campoCEP" placeholder="CEP"
                           value="<c:out value='${campoCEP}'/>">
                </div>
                <div class="campo">
                    <label for="rua">Rua</label>
                    <input type="text" id="rua" name="campoRua" placeholder="rua"
                           value="<c:out value='${campoRua}'/>" required>
                </div>
                <div class="campo">
                    <label for="numero">Número</label>
                    <input type="text" id="numero" name="campoNumero" placeholder="número"
                           value="<c:out value='${campoNumero}'/>" required>
                </div>
                <div class="campo">
                    <label for="complemento">Complemento</label>
                    <input type="text" id="complemento" name="campoComplemento" placeholder="complemento"
                           value="<c:out value='${campoComplemento}'/>">
                </div>
                <div class="campo">
                    <label for="bairro">Bairro</label>
                    <input type="text" id="bairro" name="campoBairro" placeholder="bairro"
                           value="<c:out value='${campoBairro}'/>" required>
                </div>
                <div class="campo">
                    <label for="cidade">Cidade</label>
                    <input type="text" id="cidade" name="campoCidade" placeholder="cidade"
                           value="<c:out value='${campoCidade}'/>" required>
                </div>
                <div class="campo">
                    <label for="estado">Estado</label>
                    <input type="text" id="estado" name="campoEstado" placeholder="estado"
                           value="<c:out value='${campoEstado}'/>" required>
                </div>
                <div class="campo largo">
                    <label for="pais">País</label>
                    <input type="text" id="pais" name="campoPais" placeholder="país"
                           value="<c:out value='${campoPais}'/>" required>
                </div>
            </div>

            <div class="modal-rodape">
                <button type="submit" class="botao-roxo adicionar">Adicionar</button>
            </div>
        </form>
    </div>
</div>

<%-- ======================= OVERLAY ALTERAR UNIDADE ======================= --%>
<div class="overlay" id="overlayEditarUnidade" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="tituloEditarUnidade">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/AlterarFuncionario.svg" alt="">
            <h2 id="tituloEditarUnidade">Alterar unidade</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <form action="${ctx}/unidades" method="post" accept-charset="UTF-8">
            <input type="hidden" name="acao" value="editar">
            <div class="modal-corpo">
                <div class="campo">
                    <label for="editarNomeUnidade">Nome</label>
                    <input type="text" id="editarNomeUnidade" name="campoNomeUnidade" placeholder="nome" data-campo="nome">
                </div>
                <div class="campo">
                    <label for="editarIdUnidade">ID</label>
                    <input type="text" id="editarIdUnidade" name="id" placeholder="id" data-campo="id" readonly>
                </div>
                <div class="campo">
                    <label for="editarEnderecoUnidade">Endereço</label>
                    <input type="text" id="editarEnderecoUnidade" placeholder="endereço" data-campo="endereco">
                </div>
                <div class="campo">
                    <label for="editarCnpjUnidade">CNPJ</label>
                    <input type="text" id="editarCnpjUnidade" name="campoCNPJ" placeholder="CNPJ" data-campo="cnpj">
                </div>
                <div class="campo">
                    <label for="editarCnaeUnidade">CNAE</label>
                    <input type="text" id="editarCnaeUnidade" name="campoCNAE" placeholder="CNAE" data-campo="cnae">
                </div>
                <div class="campo">
                    <label for="editarEmpresaUnidade">Empresa</label>
                    <input type="text" id="editarEmpresaUnidade" placeholder="empresa" data-campo="empresa" readonly>
                </div>
            </div>

            <div class="modal-rodape">
                <%-- O UnitsRegisterServlet ainda não trata edição; habilite quando houver suporte. --%>
                <button type="submit" class="botao-roxo" disabled>Alterar</button>
                <p class="aviso-modal">Edição ainda não disponível no servidor.</p>
            </div>
        </form>
    </div>
</div>

<%-- ======================= OVERLAY VER UNIDADE ======================= --%>
<div class="overlay" id="overlayVerUnidade" aria-hidden="true">
    <div class="modal modo-ver" role="dialog" aria-modal="true" aria-labelledby="tituloVerUnidade">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/VER.svg" alt="" style="width: 35px; height: 25px; top: 20px; filter: brightness(0) invert(1)">
            <h2 id="tituloVerUnidade">Ver unidade</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <div class="modal-corpo">
            <div class="campo">
                <label for="verNomeUnidade">nome</label>
                <input type="text" id="verNomeUnidade" data-campo="nome" readonly>
            </div>
            <div class="campo">
                <label for="verIdUnidade">id</label>
                <input type="text" id="verIdUnidade" data-campo="id" readonly>
            </div>
            <div class="campo">
                <label for="verEnderecoUnidade">endereço</label>
                <input type="text" id="verEnderecoUnidade" data-campo="endereco" readonly>
            </div>
            <div class="campo">
                <label for="verCnpjUnidade">CNPJ</label>
                <input type="text" id="verCnpjUnidade" data-campo="cnpj" readonly>
            </div>
            <div class="campo">
                <label for="verCnaeUnidade">CNAE</label>
                <input type="text" id="verCnaeUnidade" data-campo="cnae" readonly>
            </div>
            <div class="campo">
                <label for="verEmpresaUnidade">empresa</label>
                <input type="text" id="verEmpresaUnidade" data-campo="empresa" readonly>
            </div>
        </div>
        <div class="modal-rodape"></div>
    </div>
</div>

<script src="${ctx}/Crud.js"></script>
</body>
</html>



