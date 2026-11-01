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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/Sidebar.css">
</head>
<body>

<div class="perm-tela">

    <jsp:include page="/Sidebar.jsp">
        <jsp:param name="ativo" value="permissoes"/>
        <jsp:param name="semEmpresas" value="true"/>
    </jsp:include>

    <h1 class="perm-titulo">Área restrita</h1>
    <p class="perm-subtitulo">Permissões</p>

    <c:if test="${not empty erro}">
        <p class="mensagem-erro"><c:out value="${erro}"/></p>
    </c:if>
    <c:if test="${not empty sucesso}">
        <p class="mensagem-sucesso"><c:out value="${sucesso}"/></p>
    </c:if>

    <form id="formPermissoes" action="${ctx}/permissoes" method="post">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">

        <div class="perm-seletor">
            <label for="funcionarioPermissao">Permissões de:</label>
            <select id="funcionarioPermissao" name="funcionario" required ${empty funcionarios ? 'disabled' : ''}>
                <c:choose>
                    <c:when test="${not empty funcionarios}">
                        <c:forEach var="funcionario" items="${funcionarios}">
                            <option value="${funcionario.id}" ${funcionario.id eq funcionarioSelecionado ? 'selected' : ''}>
                                <c:out value="${funcionario.name}"/>
                            </option>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <option value="">Nenhum funcionário disponível</option>
                    </c:otherwise>
                </c:choose>
            </select>
        </div>

        <div class="perm-pesquisa">
            <label for="pesquisaPermissao" class="visually-hidden">Pesquisar</label>
            <input type="search" id="pesquisaPermissao" placeholder="Pesquisar" autocomplete="off">
            <img src="${ctx}/Imagens/pesquisar.svg" alt="">
        </div>

        <%--
            Permissões da tabela "permissions" (id, name, description, pattern).
            Cada cartão carrega o id e o name do banco em data-*; o checkbox envia o id.
            Ordem = ordem dos IDs. Página 1: ids 1-4 | Página 2: ids 5-7 (4 por página).
            Se o controller enviar o atributo "permissoesAtivas" (coleção de ids que o
            funcionário já possui), os switches vêm marcados conforme essa coleção.
        --%>
        <ul class="perm-lista" id="listaPermissoes">

            <%-- ============================ PÁGINA 1 ============================ --%>
            <li class="perm-cartao" data-id="1" data-nome="user:analytics:visibility:high">
                <span class="perm-icone"><img src="${ctx}/Imagens/SimboloInventarios.svg" alt=""></span>
                <h2>Alta visibilidade</h2>
                <p>Habilitar esta permissão concede ao funcionário visibilidade da análise de dados em toda a hierarquia da empresa.</p>
                <label class="perm-chave">
                    <input type="checkbox" name="permissoes" value="1" aria-label="Alta visibilidade"
                    ${permissoesAtivas.contains(1) ? 'checked' : ''}>
                    <span class="trilho"></span>
                    <span class="botao"></span>
                </label>
            </li>

            <li class="perm-cartao" data-id="2" data-nome="inventory:analysis">
                <span class="perm-icone"><img src="${ctx}/Imagens/SimboloExcluir.svg" alt=""></span>
                <h2>Análise</h2>
                <p>Habilitar esta permissão concede ao funcionário acesso à análise de inventário.</p>
                <label class="perm-chave">
                    <input type="checkbox" name="permissoes" value="2" aria-label="Análise de inventário"
                    ${permissoesAtivas.contains(2) ? 'checked' : ''}>
                    <span class="trilho"></span>
                    <span class="botao"></span>
                </label>
            </li>

            <li class="perm-cartao" data-id="3" data-nome="inventory:register">
                <span class="perm-icone"><img src="${ctx}/Imagens/SimboloVizualizar.svg" alt=""></span>
                <h2>Registrar</h2>
                <p>Habilitar esta permissão concede ao funcionário a habilidade de criar e editar inventários.</p>
                <label class="perm-chave">
                    <input type="checkbox" name="permissoes" value="3" aria-label="Criar e editar inventários"
                    ${permissoesAtivas.contains(3) ? 'checked' : ''}>
                    <span class="trilho"></span>
                    <span class="botao"></span>
                </label>
            </li>

            <li class="perm-cartao" data-id="4" data-nome="inventory:generate">
                <span class="perm-icone"><img src="${ctx}/Imagens/SimboloAnalise.svg" alt=""></span>
                <h2>Gerar</h2>
                <p>Habilitar esta permissão concede ao funcionário a habilidade de gerar um relatório de saída.</p>
                <label class="perm-chave">
                    <input type="checkbox" name="permissoes" value="4" aria-label="Gerar relatório de saída"
                    ${permissoesAtivas.contains(4) ? 'checked' : ''}>
                    <span class="trilho"></span>
                    <span class="botao"></span>
                </label>
            </li>

            <%-- ============================ PÁGINA 2 ============================ --%>
            <li class="perm-cartao" data-id="5" data-nome="calculator:gases">
                <span class="perm-icone"><img src="${ctx}/Imagens/SimboloAnalise.svg" alt=""></span>
                <h2>Calculadora de gases</h2>
                <p>Habilitar esta permissão concede ao funcionário acesso à calculadora de previsão de gases.</p>
                <label class="perm-chave">
                    <input type="checkbox" name="permissoes" value="5" aria-label="Calculadora de gases"
                    ${permissoesAtivas.contains(5) ? 'checked' : ''}>
                    <span class="trilho"></span>
                    <span class="botao"></span>
                </label>
            </li>

            <li class="perm-cartao" data-id="6" data-nome="calculator:roi">
                <span class="perm-icone"><img src="${ctx}/Imagens/SimboloVizualizar.svg" alt=""></span>
                <h2>Calculadora de ROI</h2>
                <p>Habilitar esta permissão concede ao funcionário acesso à calculadora de previsão de investimento.</p>
                <label class="perm-chave">
                    <input type="checkbox" name="permissoes" value="6" aria-label="Calculadora de ROI"
                    ${permissoesAtivas.contains(6) ? 'checked' : ''}>
                    <span class="trilho"></span>
                    <span class="botao"></span>
                </label>
            </li>

            <li class="perm-cartao" data-id="7" data-nome="chatbot">
                <span class="perm-icone"><img src="${ctx}/Imagens/SimboloInventarios.svg" alt=""></span>
                <h2>Chatbot</h2>
                <p>Habilitar esta permissão concede ao funcionário acesso ao chatbot.</p>
                <label class="perm-chave">
                    <input type="checkbox" name="permissoes" value="7" aria-label="Chatbot"
                    ${permissoesAtivas.contains(7) ? 'checked' : ''}>
                    <span class="trilho"></span>
                    <span class="botao"></span>
                </label>
            </li>
        </ul>

        <button type="submit" class="perm-salvar" ${empty funcionarios ? 'disabled' : ''}>
            Salvar permissões
        </button>
    </form>

    <p class="perm-contagem">Mostrando 4 por página</p>

    <nav class="perm-paginacao" id="paginacaoPermissoes" aria-label="Paginação">
        <button type="button" class="perm-seta" id="paginaAnterior" aria-label="Página anterior">
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M15 3v14L3.5 10z" fill="none" stroke="#FFF" stroke-width="2.6" stroke-linejoin="round"/></svg>
        </button>
        <span id="paginasNumeros" class="perm-numeros"></span>
        <button type="button" class="perm-seta" id="paginaProxima" aria-label="Próxima página">
            <svg viewBox="0 0 20 20" aria-hidden="true"><path d="M5 3v14l11.5-7z" fill="none" stroke="#FFF" stroke-width="2.6" stroke-linejoin="round"/></svg>
        </button>
    </nav>
</div>

<script>
    (function () {
        const POR_PAGINA = 4;

        const campo = document.getElementById("pesquisaPermissao");
        const cartoes = Array.from(document.querySelectorAll(".perm-cartao"));
        const numeros = document.getElementById("paginasNumeros");
        const anterior = document.getElementById("paginaAnterior");
        const proxima = document.getElementById("paginaProxima");
        const contagem = document.querySelector(".perm-contagem");

        let paginaAtual = 1;

        function filtrados() {
            const termo = campo.value.trim().toLowerCase();
            return cartoes.filter(function (cartao) {
                if (termo === "") return true;
                const texto = (cartao.textContent + " " + cartao.dataset.nome).toLowerCase();
                return texto.indexOf(termo) !== -1;
            });
        }

        function renderizar() {
            const visiveis = filtrados();
            const totalPaginas = Math.max(1, Math.ceil(visiveis.length / POR_PAGINA));
            if (paginaAtual > totalPaginas) paginaAtual = totalPaginas;

            const inicio = (paginaAtual - 1) * POR_PAGINA;
            const daPagina = visiveis.slice(inicio, inicio + POR_PAGINA);

            cartoes.forEach(function (cartao) {
                cartao.hidden = daPagina.indexOf(cartao) === -1;
            });

            // botões numerados
            numeros.innerHTML = "";
            for (let i = 1; i <= totalPaginas; i++) {
                const botao = document.createElement("button");
                botao.type = "button";
                botao.className = "perm-pagina" + (i === paginaAtual ? " atual" : "");
                botao.textContent = i;
                botao.setAttribute("aria-label", "Página " + i);
                if (i === paginaAtual) botao.setAttribute("aria-current", "page");
                botao.addEventListener("click", function () {
                    paginaAtual = i;
                    renderizar();
                });
                numeros.appendChild(botao);
            }

            anterior.disabled = paginaAtual === 1;
            proxima.disabled = paginaAtual === totalPaginas;


            contagem.classList.toggle("perm-contagem-meio", daPagina.length <= 2);
        }

        anterior.addEventListener("click", function () {
            if (paginaAtual > 1) { paginaAtual--; renderizar(); }
        });
        proxima.addEventListener("click", function () {
            paginaAtual++; renderizar();
        });
        campo.addEventListener("input", function () {
            paginaAtual = 1;
            renderizar();
        });

        const seletorFuncionario = document.getElementById("funcionarioPermissao");
        seletorFuncionario.addEventListener("change", function () {
            if (seletorFuncionario.value) {
                window.location.href = "${ctx}/permissoes?funcionario=" +
                    encodeURIComponent(seletorFuncionario.value);
            }
        });

        renderizar();
    })();
</script>

</body>
</html>
