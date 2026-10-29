<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    Barra de ações (Figma: BARRA DE PESQUISA, FRILTOS, SELECIONAR TODOS, ADD NOVO).
    Parâmetros: modal = id do overlay de "Adicionar"; selecionar = "false" para esconder "Selecionar todos".
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<div class="pesquisa">
    <label for="pesquisa" class="visually-hidden">Pesquisar</label>
    <input type="search" id="pesquisa" placeholder="Pesquisar" autocomplete="off">
    <svg viewBox="0 0 26 26" aria-hidden="true">
        <circle cx="11" cy="11" r="8" fill="none" stroke="#B3B7C9" stroke-width="2"/>
        <path d="M17 17l6 6" stroke="#B3B7C9" stroke-width="2" stroke-linecap="round"/>
    </svg>
</div>

<button type="button" class="botao-filtros">
    Filtros
    <svg viewBox="0 0 28 28" aria-hidden="true">
        <path d="M8 11l6 6 6-6" fill="none" stroke="#7E8294" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
</button>

<c:if test="${param.selecionar ne 'false'}">
    <button type="button" class="botao-selecionar" id="botaoSelecionarTodos">
        <svg viewBox="0 0 20 20" aria-hidden="true">
            <rect x="1" y="1" width="18" height="18" rx="4" fill="none" stroke="#FFF" stroke-width="2" stroke-dasharray="3 2.4"/>
            <path d="M8 7l7 4.2-3.1.8-1.3 3z" fill="#FFF" stroke="#FFF" stroke-width="1.2" stroke-linejoin="round"/>
        </svg>
        Selecionar todos
    </button>
</c:if>

<button type="button" class="botao-adicionar" data-abrir="${param.modal}">
    <img src="${ctx}/Imagens/adicionarNovo.svg" alt="Adicionar Novo">
    Adicionar novo
</button>
