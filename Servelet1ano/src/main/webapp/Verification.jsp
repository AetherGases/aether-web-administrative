<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Verificação | Aether</title>

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

    <a class="voltar roxo" href="${ctx}/RecoverPassword.jsp">
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
        <img class="correio" src="${ctx}/Imagens/correio.svg" alt="">
        <h1 class="pilula-titulo roxa" style="left: 160.5px; top: 146px; width: 225px; height: 46px">Verificação</h1>

        <form action="${ctx}/verify-recovery" method="post" accept-charset="UTF-8">
            <div class="codigo" style="top: 221px">
                <input type="text" id="codigo1" name="codigo1" maxlength="1" inputmode="numeric"
                       aria-label="Dígito 1 do código" required autofocus>
                <input type="text" id="codigo2" name="codigo2" maxlength="1" inputmode="numeric"
                       aria-label="Dígito 2 do código" required>
                <input type="text" id="codigo3" name="codigo3" maxlength="1" inputmode="numeric"
                       aria-label="Dígito 3 do código" required>
                <input type="text" id="codigo4" name="codigo4" maxlength="1" inputmode="numeric"
                       aria-label="Dígito 4 do código" required>
                <input type="text" id="codigo5" name="codigo5" maxlength="1" inputmode="numeric"
                       aria-label="Dígito 5 do código" required>
                <input type="text" id="codigo6" name="codigo6" maxlength="1" inputmode="numeric"
                       aria-label="Dígito 6 do código" required>
            </div>

            <p class="link-conta roxo" style="top: 316px">Digite o código de 6 dígitos enviado para o seu e-mail</p>

            <label for="novaSenha" class="visually-hidden">Nova senha</label>
            <input class="entrada" style="top: 360px" type="password" id="novaSenha" name="novaSenha"
                   placeholder="Nova senha" maxlength="1024" autocomplete="new-password" required>

            <label for="confirmarSenha" class="visually-hidden">Confirmar nova senha</label>
            <input class="entrada" style="top: 428px" type="password" id="confirmarSenha" name="confirmarSenha"
                   placeholder="Confirmar nova senha" maxlength="1024" autocomplete="new-password" required>

            <button class="botao-enviar roxo" style="top: 512px" type="submit">Enviar</button>

            <p class="link-conta roxo" style="top: 588px">Não recebeu o código? <a href="${ctx}/RecoverPassword.jsp">Reenviar</a></p>
            <p class="link-conta roxo" style="top: 620px">Colocou o e-mail errado? <a href="${ctx}/RecoverPassword.jsp">altere aqui</a></p>
        </form>
    </main>

    <script>
        (function () {
            var campos = document.querySelectorAll('.codigo input');
            campos.forEach(function (campo, i) {
                campo.addEventListener('input', function () {
                    if (campo.value && campos[i + 1]) campos[i + 1].focus();
                });
                campo.addEventListener('keydown', function (e) {
                    if (e.key === 'Backspace' && !campo.value && campos[i - 1]) campos[i - 1].focus();
                });
                campo.addEventListener('paste', function (e) {
                    var texto = (e.clipboardData.getData('text') || '').replace(/\s/g, '').slice(0, 6);
                    if (texto.length > 1) {
                        e.preventDefault();
                        texto.split('').forEach(function (c, k) { if (campos[k]) campos[k].value = c; });
                    }
                });
            });
        })();
    </script>
</div>

</body>
</html>
