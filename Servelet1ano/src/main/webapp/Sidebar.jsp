<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="ativo" value="${param.ativo}" />

<aside class="sidebar">
    <img class="sidebar-logo" src="${ctx}/Imagens/Logo.svg" alt="Logo Aether">
    <img class="sidebar-mascote" src="${ctx}/Imagens/Aeko.svg" alt="">
    <img class="sidebar-maleta" src="${ctx}/Imagens/Maleta.svg" alt="">
    <p class="sidebar-admin">ADMIN</p>

    <nav aria-label="Menu principal">
        <ul class="sidebar-menu">
            <li>
                <a href="${ctx}/Dashboard.jsp" class="${ativo eq 'dashboard' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/dashboard.svg" alt="">Dashboards
                </a>
            </li>
            <li>
                <a href="${ctx}/Employee.jsp" class="${ativo eq 'funcionarios' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/funcionario.svg" alt="">Funcionários
                </a>
            </li>
            <li>
                <a href="${ctx}/Sectors.jsp" class="${ativo eq 'setores' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/setores.svg" alt="">Setores
                </a>
            </li>
            <li>
                <a href="${ctx}/Units.jsp" class="${ativo eq 'unidades' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/unidade.svg" alt="">Unidades
                </a>
            </li>
            <li>
                <a href="${ctx}/PermissionsOne.jsp" class="${ativo eq 'permissoes' ? 'ativo' : ''}">
                    <img src="${ctx}/Imagens/permissao1.svg" alt="">Permissões
                </a>
            </li>
            <li>
                <a href="${ctx}/PermissionsTwo.jsp" class="${ativo eq 'permissoesAether' ? 'ativo' : ''}">
                    <img class="icone-largo" src="${ctx}/Imagens/permissao2.svg" alt="">Permissões
                </a>
            </li>
        </ul>
    </nav>

    <a class="sidebar-logout" href="${ctx}/login">
        Logout
        <img src="${ctx}/Imagens/logout.svg" alt="">
    </a>
</aside>