<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Verificação do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg">

    <link rel="stylesheet" href="Global.css">
    <link rel="stylesheet" href="Verification.css">

</head>
<body>

    <c:if test="${not empty mensagem}">
        <p role="status">
            <c:out value="${mensagem}" />
        </p>
    </c:if>

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

        <div class="caixaFormVerificacao">

            <div>
                <img class="aekoCRUD" src="Imagens/AekoLogin.svg" alt="Aeko">
                <img class="correio" src="Imagens/correio.svg" alt="Imagem de uma Carta">
            </div>

            <div class="retanguloVerificacao">
                <p class="tipografiaVerificacao">Verificação</p>
            </div>

            <form action="${pageContext.request.contextPath}/verify-recovery" method="post">

                <div class="divQuadradinhos">
                    <input type="text" id="codigo1" aria-label="Primeiro dígito do código" name="codigo1" maxlength="1" required class="quadradinhoCod" autofocus oninput="moverProximo(this, 'codigo2')">
                    <input type="text" id="codigo2" aria-label="Segundo dígito do código" name="codigo2" maxlength="1" required class="quadradinhoCod" oninput="moverProximo(this, 'codigo3')">
                    <input type="text" id="codigo3" aria-label="Terceiro dígito do código" name="codigo3" maxlength="1" required class="quadradinhoCod" oninput="moverProximo(this, 'codigo4')">
                    <input type="text" id="codigo4" aria-label="Quarto dígito do código" name="codigo4" maxlength="1" required class="quadradinhoCod" oninput="moverProximo(this, 'codigo5')">
                    <input type="text" id="codigo5" aria-label="Quinto dígito do código" name="codigo5" maxlength="1" required class="quadradinhoCod" oninput="moverProximo(this, 'codigo6')">
                    <input type="text" id="codigo6" aria-label="Sexto dígito do código" name="codigo6" maxlength="1" required class="quadradinhoCod">
                </div>

                <div>
                    <label for="novaSenha">Nova senha</label>
                    <input type="password" id="novaSenha" name="novaSenha"
                           maxlength="1024" autocomplete="new-password" required>
                </div>

                <div>
                    <label for="confirmarSenha">Confirmar nova senha</label>
                    <input type="password" id="confirmarSenha" name="confirmarSenha"
                           maxlength="1024" autocomplete="new-password" required>
                </div>

                <c:if test="${not empty mensagem}">
                    <p role="alert"><c:out value="${mensagem}" /></p>
                </c:if>

                <button class="botaoEnvioRoxo tipografiaEnvio" type="submit" value="enviarFormulárioSenha">Enviar</button>


            </form>

            <p class="divSubtexto1">Digite o código de 6 dígitos enviado para o seu email</p>

            <p class="divSubtexto2"> Não recebeu o código? <a href="RecoverPassword.css" class="divSubtexto2Reenviar">Reenviar </a> </p>

            <p class="divSubTexto3">Colocou o E-mail errado? <a class="divSubTexto3Destaque"  href="RecoverPassword.jsp">altere aqui</a></p>


            <script>
                function moverProximo(campoAtual, proximoId) {
                    if (campoAtual.value.length >= campoAtual.maxLength) {
                        document.getElementById(proximoId).focus();
                    }
                }
            </script>

        </div>
    </main>

</body>
</html>