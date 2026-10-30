<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Cadastro de gerente | Aether</title>

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

    <a class="voltar roxo" href="${ctx}/Index.jsp">
        <span>
            <svg viewBox="0 0 22 22" aria-hidden="true"><path d="M14 4l-7 7 7 7" fill="none" stroke="#FFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </span>
        Voltar
    </a>

    <%-- Logo à esquerda (Figma: Group 427320935 em 110,325) --%>
    <div class="logo-lateral">
        <img src="${ctx}/Imagens/AekoLogoEmBaixo.svg" alt="Aether">
        <p>Pense no presente e mude seu <span>futuro</span></p>
    </div>

    <c:if test="${not empty requestScope.erro}">
        <p class="mensagem mensagem-erro" role="alert" style="top: 2px; padding: 6px 18px"><c:out value="${requestScope.erro}"/></p>
    </c:if>
    <c:if test="${not empty requestScope.sucesso}">
        <p class="mensagem mensagem-sucesso" role="status" style="top: 2px; padding: 6px 18px"><c:out value="${requestScope.sucesso}"/></p>
    </c:if>

    <main class="cartao-form roxo" style="top: 39px; height: 946px">
        <img class="mascote" style="left: 193px; top: 23px" src="${ctx}/Imagens/AekoLogin.svg" alt="">
        <h1 class="pilula-titulo roxa" style="left: 63.5px; top: 146px; width: 419px">Cadastro de gerente</h1>

        <form action="${ctx}/Dashboard.jsp" method="post" accept-charset="UTF-8">
            <label for="nome" class="visually-hidden">Nome</label>
            <input class="entrada" style="top: 221px" type="text" id="nome" name="campoNome"
                   placeholder="Nome" value="<c:out value='${requestScope.campoNome}'/>" required>

            <label for="email" class="visually-hidden">E-mail</label>
            <input class="entrada" style="top: 297px" type="email" id="email" name="campoEmail"
                   placeholder="E-mail" value="<c:out value='${requestScope.campoEmail}'/>" required>

            <label for="telefone" class="visually-hidden">Telefone</label>
            <input class="entrada" style="top: 373px" type="tel" id="telefone" name="campoTelefone"
                   placeholder="Telefone" value="<c:out value='${requestScope.campoTelefone}'/>" required>

            <label for="senha" class="visually-hidden">Senha</label>
            <input class="entrada" style="top: 449px" type="password" id="senha" name="campoSenha"
                   placeholder="Senha" autocomplete="new-password" required>

            <label for="confirmarSenha" class="visually-hidden">Confirmar senha</label>
            <input class="entrada" style="top: 525px" type="password" id="confirmarSenha" name="campoConfirmarSenha"
                   placeholder="Confirmar senha" autocomplete="new-password" required>

            <label for="empresa" class="visually-hidden">Empresa</label>
            <select class="entrada" style="top: 601px" id="empresa" name="empresas" required>
                <option value="" disabled selected>Selecionar empresa</option>
                <c:forEach var="empresa" items="${empresas}">
                    <option value="${empresa.id}"><c:out value="${empresa.name}"/></option>
                </c:forEach>
            </select>

            <label for="cpf" class="rotulo-cpf" style="top: 673px">CPF:</label>
            <input class="entrada cpf" style="top: 710px" type="text" id="cpf" name="campoCPF"
                   placeholder="Digite aqui..." value="<c:out value='${requestScope.campoCPF}'/>" required>

            <button class="botao-enviar roxo" style="top: 792px" type="submit">Entrar</button>

            <p class="link-conta roxo" style="top: 881px">
                Já possui uma conta? <a href="${ctx}/login">clique aqui</a>
            </p>
        </form>
    </main>
</div>

</body>
</html>
