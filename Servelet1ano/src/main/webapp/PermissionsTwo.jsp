<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=1440">
    <title>Permissões Aether | Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="icon" href="${ctx}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/Permissions.css">
</head>
<body>

<%-- Figma: TELA CRUDV2 - AETHER (nó 4956:6389) --%>
<div class="perm-tela">

    <jsp:include page="Sidebar.jsp">
        <jsp:param name="ativo" value="permissoesAether"/>
        <jsp:param name="semEmpresas" value="true"/>
    </jsp:include>

    <h1 class="perm-titulo medio">Área restrita</h1>
    <p class="perm-subtitulo">Permissões Aether</p>

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
    <%-- Página atual: PermissionsTwo.jsp (página 1) ou PermissionsTwo.jsp?pagina=2 --%>
    <c:set var="pagina2" value="${param.pagina eq '2'}" />

    <c:choose>
        <c:when test="${pagina2}">
            <%-- Página 2 (print enviado): 3 cartões; o texto "Mostrando" fica ao lado do 3º cartão --%>
            <ul class="perm-lista">
                <li class="perm-cartao">
                    <span class="perm-icone"><img src="${ctx}/Imagens/SimboloAnalise.svg" alt=""></span>
                    <h2>Calculadora de gases</h2>
                    <p>Acesso à calculadora de previsão de gases.</p>
                    <label class="perm-chave">
                        <input type="checkbox" name="permissao5" checked aria-label="Calculadora de gases">
                        <span class="trilho"></span>
                        <span class="botao"></span>
                    </label>
                </li>

                <li class="perm-cartao">
                    <span class="perm-icone"><img src="${ctx}/Imagens/SimboloAnalise.svg" alt=""></span>
                    <h2>Calculadora de ROI</h2>
                    <p>Acesso à calculadora de projeção de investimentos.</p>
                    <label class="perm-chave">
                        <input type="checkbox" name="permissao6" checked aria-label="Calculadora de ROI">
                        <span class="trilho"></span>
                        <span class="botao"></span>
                    </label>
                </li>

                <li class="perm-cartao">
                    <span class="perm-icone"><img src="${ctx}/Imagens/SimboloInventarios.svg" alt=""></span>
                    <h2>Chatbot</h2>
                    <p>Acesso ao Chatbot.</p>
                    <label class="perm-chave">
                        <input type="checkbox" name="permissao7" checked aria-label="Chatbot">
                        <span class="trilho"></span>
                        <span class="botao"></span>
                    </label>
                </li>
            </ul>

            <p class="perm-contagem perm-contagem-meio">Mostrando 4 por página</p>
        </c:when>
        <c:otherwise>
            <ul class="perm-lista">
                <li class="perm-cartao">
                    <span class="perm-icone"><img src="${ctx}/Imagens/SimboloAnalise.svg" alt=""></span>
                    <h2>Analisar usuários</h2>
                    <p style="width: 300px">Acesso analítico em toda a hierarquia corporativa.</p>
                    <label class="perm-chave">
                        <input type="checkbox" name="permissao1" checked aria-label="Analisar usuários">
                        <span class="trilho"></span>
                        <span class="botao"></span>
                    </label>
                </li>

                <li class="perm-cartao">
                    <span class="perm-icone"><img src="${ctx}/Imagens/SimboloAnalise.svg" alt=""></span>
                    <h2>Analisar inventários</h2>
                    <p>Acesso à análise de inventário.</p>
                    <label class="perm-chave">
                        <input type="checkbox" name="permissao2" checked aria-label="Analisar inventários">
                        <span class="trilho"></span>
                        <span class="botao"></span>
                    </label>
                </li>

                <li class="perm-cartao">
                    <span class="perm-icone"><img src="${ctx}/Imagens/SimboloInventarios.svg" alt=""></span>
                    <h2>Registrar inventários</h2>
                    <p>Acesso para criar e editar inventário.</p>
                    <label class="perm-chave">
                        <input type="checkbox" name="permissao3" checked aria-label="Registrar inventários">
                        <span class="trilho"></span>
                        <span class="botao"></span>
                    </label>
                </li>

                <li class="perm-cartao">
                    <span class="perm-icone"><img src="${ctx}/Imagens/SimboloInventarios.svg" alt=""></span>
                    <h2>Gerar relatórios</h2>
                    <p style="width: 410px">Acesso para gerar um relatório de saída.</p>
                    <label class="perm-chave">
                        <input type="checkbox" name="permissao4" checked aria-label="Gerar relatórios">
                        <span class="trilho"></span>
                        <span class="botao"></span>
                    </label>
                </li>
            </ul>


            <p class="perm-contagem">Mostrando 4 por página</p>
        </c:otherwise>
    </c:choose>

    <nav class="perm-paginacao" aria-label="Paginação">
        <a class="perm-seta" href="${ctx}/PermissionsTwo.jsp" aria-label="Página anterior">
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M15 3v14L3.5 10z" fill="none" stroke="#FFF" stroke-width="2.6" stroke-linejoin="round"/></svg>
        </a>
        <a class="perm-pagina ${pagina2 ? '' : 'atual'}" href="${ctx}/PermissionsTwo.jsp"${pagina2 ? '' : ' aria-current="page"'}>1</a>
        <a class="perm-pagina ${pagina2 ? 'atual' : ''}" href="${ctx}/PermissionsTwo.jsp?pagina=2"${pagina2 ? ' aria-current="page"' : ''}>2</a>
        <a class="perm-seta" href="${ctx}/PermissionsTwo.jsp?pagina=2" aria-label="Próxima página">
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M5 3v14l11.5-7z" fill="none" stroke="#FFF" stroke-width="2.6" stroke-linejoin="round"/></svg>
        </a>
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