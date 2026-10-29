<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Recuperação de Senha do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg">

    <link rel="stylesheet" href="Global.css">
    <link rel="stylesheet" href="RecoverPassword.css">

</head>
<body>

    <div class="blur1"></div>
    <div class="blur2"></div>
    <div class="blur3"></div>
    <div class="blur4"></div>

    <a href="Login.jsp"  class="botaoVoltar">
        <p class="tipografiaBotaoVoltar">Voltar</p>
        <div class="botaoRendondoVoltar"> <img class="setaVoltar" src="Imagens/setaVoltar.svg" alt="Seta Voltar"> </div>
    </a>

    <div class="divLogo">
        <img class="logoCRUD" src="Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
        <h1 class="logotipoAether">aether</h1>
        <p class="fraseLogo">Pense no presente e mude seu <span class="fraseFuturoLogo">futuro</span></p>
    </div>


    <main class="divLoginPrincipal">

        <div class="caixaFormRS">

            <img class="aekoCRUD" src="Imagens/AekoLogin.svg" alt="Aeko">

            <div class="retanguloRecSenha">
                <p class="tipografiaRecSenha">Recuperar senha</p>
            </div>

            <form action="Verification.jsp" method="post">

                <input class="campoEmail campoEmailtipografia" type="email" id="email" name="campoEmail" placeholder="e-mail">
                <label for="email"></label>

                <button class="botaoEnvioRoxo tipografiaEnvio" type="submit" value="enviarFormulárioSenha">Confirmar</button>

            </form>

            <p class="naoPossuirConta">Não possui uma conta? <a class="naoPossuirContaClicavel" href="CompanyRegister.jsp">clique aqui</a></p>
        </div>

    </main>

</body>
</html>