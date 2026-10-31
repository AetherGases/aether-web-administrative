<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Cadastro empresarial | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/Cadastro.css">
</head>
<body>

<div class="tela">
    <div class="blur blur-1"></div>
    <div class="blur blur-2"></div>
    <div class="blur blur-3"></div>
    <div class="blur blur-4"></div>

    <a class="voltar" href="${ctx}/Index.jsp">
        <span>
            <svg viewBox="0 0 22 22" aria-hidden="true"><path d="M14 4l-7 7 7 7" fill="none" stroke="#FFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </span>
        Voltar
    </a>

    <%-- Planos (Figma: PLANO PULSE em 114,128 e PLANO HORIZON em 114,536) --%>
    <section class="plano-card plano-pulse">
        <h2>Aether <span class="verde">Pulse</span></h2>
        <p class="preco"><strong>R$3.300</strong>/por mês</p>
        <hr>
        <ul>
            <li>Quantidade de cadastro limitada a 10 funcionários</li>
            <li>Quantidade de analistas e administradores limitada a 2</li>
            <li>Acompanhamento mensal</li>
        </ul>
    </section>

    <section class="plano-card plano-horizon">
        <h2>Aether <span class="roxo">Horizon</span></h2>
        <p class="preco"><strong>R$9.400</strong>/a cada 3 meses</p>
        <hr>
        <ul>
            <li>Tudo do plano Aether Pulse</li>
            <li>Quantidade de cadastro de funcionários ilimitada</li>
            <li>Acompanhamento trimestral</li>
        </ul>
    </section>

    <c:if test="${not empty requestScope.erro}">
        <p class="mensagem mensagem-erro" role="alert" style="top: 16px"><c:out value="${requestScope.erro}"/></p>
    </c:if>
    <c:if test="${not empty requestScope.sucesso}">
        <p class="mensagem mensagem-sucesso" role="status" style="top: 16px"><c:out value="${requestScope.sucesso}"/></p>
    </c:if>

    <main class="cartao-form" style="top: 72px; height: 780px">
        <img class="mascote" style="left: 192px; top: 26px" src="${ctx}/Imagens/AekoLogin.svg" alt="">
        <h1 class="pilula-titulo" style="left: 80.5px; top: 137px; width: 385px">Cadastro empresarial</h1>

        <form action="${ctx}/empresa" method="post" accept-charset="UTF-8">
            <label for="empresa" class="visually-hidden">Nome da empresa</label>
            <input class="entrada" style="top: 196px" type="text" id="empresa" name="empresas"
                   placeholder="Nome da empresa" value="<c:out value='${requestScope.name}'/>" required>

            <label for="razao" class="visually-hidden">Razão social</label>
            <input class="entrada" style="top: 268px" type="text" id="razao" name="razoes"
                   placeholder="Razão social" value="<c:out value='${requestScope.razoes}'/>" required>

            <label for="tamanho" class="visually-hidden">Tamanho da empresa</label>
            <select class="entrada" style="top: 340px" id="tamanho" name="tamanhos" required>
                <option value="" disabled ${empty requestScope.tamanhos ? 'selected' : ''}>Tamanho da empresa</option>
                <option value="pequena" ${requestScope.tamanhos eq 'pequena' ? 'selected' : ''}>Pequena empresa</option>
                <option value="media" ${requestScope.tamanhos eq 'media' ? 'selected' : ''}>Média empresa</option>
                <option value="grande" ${requestScope.tamanhos eq 'grande' ? 'selected' : ''}>Grande empresa</option>
            </select>

            <label for="cnpj" class="visually-hidden">CNPJ</label>
            <input class="entrada" style="top: 422px" type="text" id="cnpj" name="campoCNPJ"
                   placeholder="CNPJ" value="<c:out value='${requestScope.campoCNPJ}'/>" required>

            <label for="email" class="visually-hidden">E-mail</label>
            <input class="entrada" style="top: 494px" type="email" id="email" name="campoEmail"
                   placeholder="E-mail" value="<c:out value='${requestScope.campoEmail}'/>" required>

            <label for="plano" class="visually-hidden">Plano</label>
            <select class="entrada plano" style="top: 566px" id="plano" name="planos" required>
                <option value="" disabled ${empty requestScope.planos ? 'selected' : ''}>Plano...</option>
                <option value="Aether Pulse" ${requestScope.planos eq 'Aether Pulse' ? 'selected' : ''}>Aether Pulse</option>
                <option value="Aether Horizon" ${requestScope.planos eq 'Aether Horizon' ? 'selected' : ''}>Aether Horizon</option>
            </select>

            <button class="botao-enviar" style="top: 632px" type="submit">Cadastrar</button>

            <p class="link-conta" style="top: 705px">
                Já possui uma conta? <a href="${ctx}/login">clique aqui</a>
            </p>
        </form>
    </main>
</div>

</body>
</html>