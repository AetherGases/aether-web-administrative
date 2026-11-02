<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ativo" value="${param.ativo}" />

<aside class="sidebar">
    <img class="sidebar-logo" src="${ctx}/Imagens/Logo.svg" alt="Logo Aether">
    <button type="button" class="sidebar-toggle" aria-expanded="false" aria-controls="menuLateral" aria-label="Abrir menu">
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16" fill="none" stroke="#1E0A4A" stroke-width="2.4" stroke-linecap="round"/></svg>
    </button>
    <img class="sidebar-mascote" src="${ctx}/Imagens/Aeko.svg" alt="">
    <img class="sidebar-maleta" src="${ctx}/Imagens/Maleta.svg" alt="">
    <p class="sidebar-admin">ADMIN</p>

    <nav id="menuLateral" aria-label="Menu principal">

        <div class="navIndicador"></div>

        <ul class="sidebar-menu">
            <li>
                <a href="${ctx}/admin" class="${ativo eq 'dashboard' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/dashboard.svg" alt="">Dashboards
                </a>
            </li>
            <li>
                <a href="${ctx}/funcionarios" class="${ativo eq 'funcionarios' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/funcionario.svg" alt="">Funcionários
                </a>
            </li>
            <li>
                <a href="${ctx}/setores" class="${ativo eq 'setores' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/setores.svg" alt="">Setores
                </a>
            </li>
            <li>
                <a href="${ctx}/unidades" class="${ativo eq 'unidades' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/unidade.svg" alt="">Unidades
                </a>
            </li>
            <li>
                <a href="${ctx}/permissoes" class="${ativo eq 'permissoes' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/permissao1.svg" alt="">Permissões
                </a>
            </li>
        </ul>
    </nav>
    <form action="${ctx}/logout"
          method="POST"
          class="sidebar-logout-form">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.adminCsrfToken}' />">

        <button type="submit"
                class="sidebar-logout">

            Logout

            <img src="${ctx}/Imagens/logout.svg"
                 alt=""
                 aria-hidden="true">

        </button>
    </form>
</aside>

<script src="${ctx}/NavBar.js" defer></script>
