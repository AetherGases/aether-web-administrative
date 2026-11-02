<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Erro 503 | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/Error.css">
</head>
<body>

<div class="telaErro">
    <h1 class="tituloErro">Erro 503</h1>
    <p class="subTituloErro">Oops! Estamos realizando uma manutenção programada para deixar a sua experiência ainda mais sustentável e rápida..</p>
    <p class="descricaoErro">Por favor, tente novamente mais tarde</p>

    <a href="${pageContext.request.contextPath}/">
        Volte ao início
    </a>

    <img class="nuvemErro" src="${pageContext.request.contextPath}/Imagens/NuvemErro.svg" alt="Aeko indicando erro">
</div>

</body>
</html>