<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Permissões | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/Permissions.css">
</head>
<body>

<%-- Figma: TELA CRUDV2 - PERMISSOES (nó 4956:5965) --%>
<div class="perm-tela">

    <jsp:include page="Sidebar.jsp">
        <jsp:param name="ativo" value="permissoes"/>
        <jsp:param name="semEmpresas" value="true"/>
    </jsp:include>

    <h1 class="perm-titulo">Área restrita</h1>
    <p class="perm-subtitulo">Permissões</p>

    <%-- Seletor do funcionário (Frame 2049760436). Usa o atributo "funcionarios" quando o servlet enviar. --%>
    <div class="perm-seletor">
        <label for="funcionarioPermissao">Permissões de:</label>
        <select id="funcionarioPermissao" name="funcionario">
            <c:choose>
                <c:when test="${not empty funcionarios}">
                    <c:forEach var="funcionario" items="${funcionarios}">
                        <option value="${funcionario.id}"><c:out value="${funcionario.name}"/></option>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <option>Caio Campelo Roxo</option>
                </c:otherwise>
            </c:choose>
        </select>
    </div>

    <div class="perm-pesquisa">
        <label for="pesquisaPermissao" class="visually-hidden">Pesquisar</label>
        <input type="search" id="pesquisaPermissao" placeholder="Pesquisar" autocomplete="off">
        <img src="${ctx}/Imagens/pesquisar.svg" alt="">
    </div>

    <%-- Cartões de permissão (instância PERMISSÕES: 1058 x 662 em 335, 259) --%>
    <ul class="perm-lista">
        <li class="perm-cartao">
            <span class="perm-icone"><img src="${ctx}/Imagens/SimboloInventarios.svg" alt=""></span>
            <h2>Criar Inventários</h2>
            <p>Habilitar esta permissão concede ao funcionário a habilidade de criar de inventários.</p>
            <label class="perm-chave">
                <input type="checkbox" name="permissao1" checked aria-label="Criar Inventários">
                <span class="trilho"></span>
                <span class="botao"></span>
            </label>
        </li>

        <li class="perm-cartao">
            <span class="perm-icone"><img src="${ctx}/Imagens/SimboloExcluir.svg" alt=""></span>
            <h2>Excluir Inventários</h2>
            <p>Habilitar esta permissão concede ao funcionário a habilidade de excluir inventários.</p>
            <label class="perm-chave">
                <input type="checkbox" name="permissao2" checked aria-label="Excluir Inventários">
                <span class="trilho"></span>
                <span class="botao"></span>
            </label>
        </li>

        <li class="perm-cartao">
            <span class="perm-icone"><img src="${ctx}/Imagens/SimboloVizualizar.svg" alt=""></span>
            <h2>Visualizar Inventários</h2>
            <p>Habilitar esta permissão concede ao funcionário a habilidade de visualizar inventários.</p>
            <label class="perm-chave">
                <input type="checkbox" name="permissao3" checked aria-label="Visualizar Inventários">
                <span class="trilho"></span>
                <span class="botao"></span>
            </label>
        </li>

        <li class="perm-cartao">
            <span class="perm-icone"><img src="${ctx}/Imagens/SimboloAnalise.svg" alt=""></span>
            <h2>Visualizar Análises de ROI</h2>
            <p>Habilitar esta permissão concede ao funcionário a habilidade de visualizar análises de ROI.</p>
            <label class="perm-chave">
                <input type="checkbox" name="permissao4" checked aria-label="Visualizar Análises de ROI">
                <span class="trilho"></span>
                <span class="botao"></span>
            </label>
        </li>
    </ul>

    <p class="perm-contagem">Mostrando 4 por página</p>

    <nav class="perm-paginacao" aria-label="Paginação">
        <button type="button" class="perm-seta" aria-label="Página anterior">
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M15 3v14L3.5 10z" fill="none" stroke="#FFF" stroke-width="2.6" stroke-linejoin="round"/></svg>
        </button>
        <span class="perm-pagina atual" aria-current="page">1</span>
        <a class="perm-pagina" href="#">2</a>
        <button type="button" class="perm-seta" aria-label="Próxima página">
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M5 3v14l11.5-7z" fill="none" stroke="#FFF" stroke-width="2.6" stroke-linejoin="round"/></svg>
        </button>
    </nav>
</div>

<script>
    // Pesquisa local: filtra os cartões pelo título e pela descrição.
    (function () {
        const campo = document.getElementById("pesquisaPermissao");
        const cartoes = document.querySelectorAll(".perm-cartao");
        campo.addEventListener("input", function () {
            const termo = campo.value.trim().toLowerCase();
            cartoes.forEach(function (cartao) {
                cartao.hidden = termo !== "" && cartao.textContent.toLowerCase().indexOf(termo) === -1;
            });
        });
    })();
</script>

</body>
</html>
