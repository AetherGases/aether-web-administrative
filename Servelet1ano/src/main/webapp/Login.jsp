<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Login | Aether</title>


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>

    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg" type="image/svg+xml">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/global.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/login.css">
</head>

<body>

<div class="tela">
    <div class="blur blur-1"></div>
    <div class="blur blur-2"></div>
    <div class="blur blur-3"></div>
    <div class="blur blur-4"></div>

    <a class="voltar roxo" href="${ctx}/Index.jsp">
        <span>
            <svg viewBox="0 0 22 22" aria-hidden="true"><path d="M14 4l-7 7 7 7" fill="none" stroke="#FFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </span>
        Voltar
    </a>

    <div class="logo-lateral">
        <img src="${ctx}/Imagens/AekoLogoEmBaixo.svg" alt="Aether">
        <p>Pense no presente e mude seu <span>futuro</span></p>
    </div>

    <c:if test="${not empty requestScope.erro or not empty requestScope.mensagem}">
        <p class="mensagem mensagem-erro" role="alert" style="top: 2px; padding: 6px 18px"><c:out value="${not empty requestScope.erro ? requestScope.erro : requestScope.mensagem}"/></p>
    </c:if>

    <main class="cartao-form" style="top: 167px; height: 690px">
        <img class="mascote" style="left: 193px; top: 23px" src="${ctx}/Imagens/AekoLogin.svg" alt="">
        <h1 class="pilula-titulo" style="left: 109.5px; top: 146px; width: 327px">Login pessoal</h1>

        <form action="${ctx}/Dashboard.jsp" method="post" accept-charset="UTF-8">
            <label for="empresa" class="visually-hidden">Empresa</label>
            <select class="entrada" style="top: 221px" id="empresa" name="empresas" required>
                <option value="" disabled ${empty param.empresas ? 'selected' : ''}>Selecionar empresa</option>
                <c:forEach var="empresa" items="${requestScope.empresas}">
                    <option
                            value="${empresa.id}"
                            <c:if test="${param.empresas == empresa.id}">selected</c:if>
                    >
                        <c:out value="${empresa.name}" />
                    </option>
                </c:forEach>

            </select>

            <!-- Identificação do funcionário -->
            <label for="nome" class="visually-hidden">
                Nome
            </label>

            <input
                    class="campoNome campoNomeNome"
                    type="text"
                    id="nome"
                    name="campoNome"
                    placeholder="Nome"
                    autocomplete="username"
                    maxlength="255"
                    required
            >

            <!-- E-mail do funcionário -->
            <label for="email" class="visually-hidden">E-mail</label>

            <input class="campoEmail campoEmailEmail" type="email" id="email" name="campoEmail" placeholder="E-mail" autocomplete="email" maxlength="255" required>

            <!-- Senha da conta -->
            <label for="senha" class="visually-hidden">
                Senha
            </label>

            <input class="campoSenha campoSenhaSenha" type="password" id="senha" name="campoSenha" placeholder="Senha" autocomplete="current-password" maxlength="1024" required>


            <button class="botaoEnvioRoxo tipografiaEnvio" type="submit">Entrar</button>

        </form>

    </section>

</main>

</body>
</html>