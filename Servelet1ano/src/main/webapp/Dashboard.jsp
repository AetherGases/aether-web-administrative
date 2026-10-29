<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Dashboard | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/Crud.css">
    <link rel="stylesheet" href="${ctx}/Dashboard.css">
</head>
<body>

<%-- Figma: TELA CRUD (INICIO) — nó 4920:5840 --%>
<div class="tela tela-dashboard">

    <jsp:include page="/WEB-INF/includes/Sidebar.jsp">
        <jsp:param name="ativo" value="dashboard"/>
    </jsp:include>

    <h1 class="titulo-pagina">Área restrita</h1>
    <p class="subtitulo-pagina">Dashboard</p>

    <%-- Cartão de boas-vindas (1051 x 348 em 330, 128) --%>
    <section class="cartao-boas-vindas">
        <span class="decoracao decoracao-grande" aria-hidden="true"></span>
        <span class="decoracao decoracao-pequena" aria-hidden="true"></span>
        <span class="decoracao decoracao-linha-1" aria-hidden="true"></span>
        <span class="decoracao decoracao-linha-2" aria-hidden="true"></span>

        <c:set var="primeiroNome" value="${empty employee.name ? 'Layous' : fn:split(fn:trim(employee.name), ' ')[0]}" />
        <h2>Olá, <c:out value="${primeiroNome}"/>!</h2>
        <p>Você tem 12 mini-layouts aguardando análise hoje.</p>

        <a class="botao-cartao botao-unidades" href="${ctx}/unidades">Ver unidades</a>
        <a class="botao-cartao botao-funcionarios" href="${ctx}/funcionarios">Ver funcionários</a>
    </section>

    <%-- Cartões de números (Analytic em 330, 513 — 343 x 206 cada).
         Os valores são de exemplo, como no Figma; troque pelos atributos do servlet quando existirem. --%>
    <a class="cartao-numero n1" href="${ctx}/setores">
        <p class="rotulo">Setores</p>
        <p class="valor">24</p>
        <img src="${ctx}/Imagens/grafico.svg" alt="">
        <span class="linha"></span>
    </a>

    <a class="cartao-numero n2" href="${ctx}/permissoes">
        <p class="rotulo">Permissões</p>
        <p class="valor">08</p>
        <img src="${ctx}/Imagens/chave.svg" alt="">
        <span class="linha"></span>
    </a>

    <a class="cartao-numero n3" href="${ctx}/funcionarios">
        <p class="rotulo">Funcionários</p>
        <p class="valor">15</p>
        <img src="${ctx}/Imagens/pessoas.svg" alt="" style="height: 60px; top: 87px">
        <span class="linha"></span>
    </a>

    <a class="cartao-numero n4" href="${ctx}/unidades">
        <p class="rotulo">Unidades</p>
        <p class="valor">07</p>
        <img src="${ctx}/Imagens/predios.svg" alt="">
        <span class="linha"></span>
    </a>
</div>

</body>
</html>
