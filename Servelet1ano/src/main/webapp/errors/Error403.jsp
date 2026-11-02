<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Erro 403 | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/Error.css">
</head>
<body>

<div class="telaErro">
    <div class="textoErro">
        <h1 class="tituloErro">Erro 403</h1>
        <p class="subTituloErro longo">Oops! A nossa nuvem detectou que você não tem permissão para acessar esta área restrita.</p>
        <p class="descricaoErro">Parece que você pegou um caminho errado... Por favor, volte ao início</p>

        <a class="botaoVoltarInicio" href="${pageContext.request.contextPath}/">
            <span class="tipografiaVoltarInicio">Volte ao início</span>
        </a>
    </div>

    <img class="nuvemErro" src="${pageContext.request.contextPath}/Imagens/NuvemErro.svg" alt="Aeko indicando erro">
</div>

<script src="${pageContext.request.contextPath}/Escala.js"></script>

</body>
</html>