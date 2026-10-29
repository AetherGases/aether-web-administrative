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

<!-- Elementos decorativos de fundo -->
<div class="blur1" aria-hidden="true"></div>
<div class="blur2" aria-hidden="true"></div>
<div class="blur3" aria-hidden="true"></div>
<div class="blur4" aria-hidden="true"></div>


<header class="divLogo">

    <img class="logoCRUD" src="${pageContext.request.contextPath}/Imagens/LogoAethererdeEscuro.svg" alt="Logo do Aether">

    <h1 class="logotipoAether">aether</h1>

    <p class="fraseLogo">Pense no presente e mude seu<span class="fraseFuturoLogo">futuro</span></p>

</header>

<main class="divLoginPrincipal">

    <section class="divCaixaForm" aria-labelledby="tituloLogin">

        <img class="aekoCRUD" src="${pageContext.request.contextPath}/Imagens/AekoLogin.svg" alt="Aeko, mascote do Aether">

        <div class="retanguloLogin">
            <h2 class="loginPessoalTexto" id="tituloLogin">
                Login pessoal
            </h2>
        </div>

        <!-- Mensagem de erro enviada pelo LoginServlet -->
        <c:if test="${not empty requestScope.erro}">
            <div class="mensagem-erro" role="alert">
                <p><c:out value="${requestScope.erro}" /></p>
            </div>
        </c:if>

        <!-- Formulário processado pelo LoginServlet -->
        <form action="${pageContext.request.contextPath}/login" method="post" accept-charset="UTF-8">
            <label for="empresa" class="visually-hidden">
                Empresa
            </label>

            <select
                    class="campoEmpresa campoEmpresaEmpresa"
                    name="empresas"
                    id="empresa"
                    required
            >
                <option
                        value=""
                        disabled
                        <c:if test="${empty param.empresas}">selected</c:if>
                >
                    Selecionar empresa
                </option>

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