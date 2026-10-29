<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Cadastro Empresarial do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg" type="image/svg+xml">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/Global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CompanyRegister.css">
</head>

<body>

<!-- Mensagem de erro enviada pelo ManagerRegisterServlet -->
<c:if test="${not empty requestScope.erro}">
    <div class="mensagem-erro" role="alert">
        <p><c:out value="${requestScope.erro}" /></p>
    </div>
</c:if>


<div class="blur1"></div>
    <div class="blur2"></div>
    <div class="blur3"></div>
    <div class="blur4"></div>

    <div>
        <div class="quadradoAetherPulse">
            <p class="tituloPlanos">Aether <span class="tituloPlanoVerde">Pulse</span></p>
            <p class="tipografiaPorMes"><span class="tipografiaValores">R$3.300</span>/por mês</p>

            <div class="linhaPlanoPulse"></div>

            <ul class="tipografiaPulse">
                <li>Quantidade de cadastro limitada a 10 funcionários</li>
                <li>Quantidade de analistas e administradores limitada a 2</li>
                <li>Acompanhamento mensal</li>
            </ul>
        </div>

        <div class="quadradoAetherHorizon">
            <p class="tipografiaHorizon">Aether <span class="tituloPlanoRoxo">Horizon</span></p>
            <p class="tipografiaPorMes"><span class="tipografiaValores">R$9.400</span>/a cada 3 meses</p>

            <div class="linhaPlanoHorizon"></div>

            <ul class="tipografiaHorizon">
                <li>Tudo do plano Aether Pulse</li>
                <li>Quantidade de cadastro de funcionários ilimitada</li>
                <li>Acompanhamento trimestral</li>
            </ul>
        </div>

    <fieldset>
        <form action="${pageContext.request.contextPath}/empresa" method="post">

            <%-- Mantém os valores preenchidos quando o Servlet retorna um erro. --%>
            <label for="empresa"></label>
            <input type="text" name="empresas" id="empresa" placeholder="Nome da empresa" value="<c:out value='${requestScope.empresas}' />" required>

            <label for="razao"></label>
            <input
                    type="text"
                    name="razoes"
                    id="razao"
                    placeholder="Razão social"
                    value="<c:out value='${requestScope.razoes}' />"
                    required>

            <label for="tamanho"></label>
            <select name="tamanhos" id="tamanho" required>
                <option value="" disabled hidden
                        <c:if test="${empty requestScope.tamanhos}">selected</c:if>>
                    Selecionar
                </option>

                <option value="pequena"
                        <c:if test="${requestScope.tamanhos eq 'pequena'}">selected</c:if>>
                    Pequena empresa
                </option>

                <option value="media"
                        <c:if test="${requestScope.tamanhos eq 'media'}">selected</c:if>>
                    Média empresa
                </option>

                <option value="grande"
                        <c:if test="${requestScope.tamanhos eq 'grande'}">selected</c:if>>
                    Grande empresa
                </option>
            </select>

            <label for="cnpj"></label>
            <input
                    type="text"
                    id="cnpj"
                    name="campoCNPJ"
                    placeholder="CNPJ"
                    value="<c:out value='${requestScope.campoCNPJ}' />"
                    required>

            <label for="email"></label>
            <input
                    type="email"
                    id="email"
                    name="campoEmail"
                    placeholder="E-mail"
                    value="<c:out value='${requestScope.campoEmail}' />"
                    required>

            <label for="plano"></label>
            <select name="planos" id="plano" required>
                <option value="" disabled hidden
                        <c:if test="${empty requestScope.planos}">selected</c:if>>
                    Plano...
                </option>

                <option value="Aether Pulse"
                        <c:if test="${requestScope.planos eq 'Aether Pulse'}">selected</c:if>>
                    Aether Pulse
                </option>

                <option value="Aether Horizon"
                        <c:if test="${requestScope.planos eq 'Aether Horizon'}">selected</c:if>>
                    Aether Horizon
                </option>
            </select>

            <button type="submit">Cadastrar</button>
        </form>
    </fieldset>

    <p>
        Já possui uma conta?
        <a href="${pageContext.request.contextPath}/Login.jsp">Clique aqui</a>
    </p>
</div>

</body>
</html>
