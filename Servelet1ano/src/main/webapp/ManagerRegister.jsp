<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Cadastro de Gerente do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg">

    <link rel="stylesheet" href="Global.css">
    <link rel="stylesheet" href="ManagerRegister.css">

</head>
<body>

    <div class="blur1"></div>
    <div class="blur2"></div>
    <div class="blur3"></div>
    <div class="blur4"></div>

    <div class="divLogo">
        <img class="logoCRUD" src="Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
        <h1 class="logotipoAether">aether</h1>
        <p class="fraseLogo">Pense no presente e mude seu <span class="fraseFuturoLogo">futuro</span></p>
    </div>

    <main class="divLoginPrincipal">

        <div class="caixaFormGerente">
            <!-- Mensagem de erro enviada pelo ManagerRegisterServlet -->
            <c:if test="${not empty requestScope.erro}">
                <div class="mensagem-erro" role="alert">
                    <p><c:out value="${requestScope.erro}" /></p>
                </div>
            </c:if>

            <!-- Mensagem de sucesso enviada pelo ManagerServletServlet -->
            <c:if test="${not empty requestScope.sucesso}">
                <div class="mensagem-sucesso" role="status">
                    <p><c:out value="${requestScope.sucesso}" /></p>
                </div>
            </c:if>

            <img class="aekoCRUD" src="Imagens/AekoLogin.svg" alt="Aeko">
            <div class="tipografiaRetanguloGerente retanguloGerente">
                Cadastro de gerente
            </div>

            <form
                    action="${pageContext.request.contextPath}/gerente"
                    method="post"
                    accept-charset="UTF-8"
            >
                <input class="campoNomeCG campoNomeCG2" type="text" id="nome" name="campoNome" placeholder="nome">
                <label for="nome"></label>

                <input class="campoEmailCG campoEmailCG2" type="email" id="email" name="campoEmail" placeholder="email">
                <label for="email"></label>

                <input class="campoTelefoneCG campoTelefoneCG2" type="text" id="telefone" name="campoTelefone" placeholder="telefone">
                <label for="telefone"></label>

                <input class="campoSenhaCG campoSenhaCG2" type="password" id="senha" name="campoSenha" placeholder="senha">
                <label for="senha"></label>

                <input class="campoConfSenhaCG campoConfSenhaCG2" type="password" id="confirmarSenha" name="campoConfirmarSenha" placeholder="confirmarSenha">
                <label for="confirmarSenha"></label>

                <div class="setaParaBaixo"></div>

                <label for="empresa" class="visually-hidden">
                    Empresa
                </label>

                <select class="campoEmpresa campoEmpresaEmpresa" name="empresas" id="empresa" required>
                    <option value="" disabled selected>
                        Selecionar empresa
                    </option>

                    <c:forEach var="empresa" items="${empresas}">
                        <option value="${empresa.id}">
                            <c:out value="${empresa.name}"/>
                        </option>
                    </c:forEach>

                </select>


                <p class="campoCPF">CPF:</p>

                <input class="campoCPFcg campoCPFcgTipografia" type="text" id="cpf" name="campoCPF" placeholder="CPF">
                <label for="cpf"></label>

                <button class="botaoEnvioRoxo tipografiaEnvio" type="submit" value="enviarFormulárioCadastroGerente">Entrar</button>

            </form>

            <p class="naoPossuiConta2">Já possui uma conta? <a class="naoPossuiConta2Destaque" href="Login.jsp">clique aqui</a></p>

        </div>
    </main>

</body>
</html>