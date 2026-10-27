<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tela de Setores do Aether</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/Global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/Global2.css">
</head>
<body>

    <aside class="aside">
        <div>
            <img class="logoAether" src="${pageContext.request.contextPath}/Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
            <p class="tipografiaLogo">aether</p>
        </div>

        <div>
            <img class="aeko" src="${pageContext.request.contextPath}/Imagens/AekoLogin.svg" alt="Imagem do Aeko">
            <p class="tipografiaAdmin">ADMIN</p>
        </div>

        <nav>
            <ul>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/dashboard">Dashboards</a>
                        <img class="imagemDashboard" src="${pageContext.request.contextPath}/Imagens/dashboard.svg" alt="Imagem de uma casa">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/funcionarios">Funcionários</a>
                        <img class="imagemFuncionario" src="${pageContext.request.contextPath}/Imagens/funcionario.svg" alt="Imagem de um usuário">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/setores">Setores</a>
                        <img class="imagemSetores" src="${pageContext.request.contextPath}/Imagens/setores.svg" alt="Imagem de setores">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/unidades">Unidades</a>
                        <img class="imagemUnidade" src="${pageContext.request.contextPath}/Imagens/unidade.svg" alt="Imagem de prédios">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/empresas">Empresas</a>
                        <img class="imagemEmpresas" src="${pageContext.request.contextPath}/Imagens/Empresa.svg" alt="Imagem de quadrados">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/permissoes">Permissões</a>
                        <img class="imagemPermissoes1" src="${pageContext.request.contextPath}/Imagens/permissao1.svg" alt="Imagem de uma pessoa com chave">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/permissoes">Permissões</a>
                        <img class="imagemPermissoes2" src="${pageContext.request.contextPath}/Imagens/permissao2.svg" alt="Imagem de nuvem">
                    </div>
                </li>
            </ul>
        </nav>

        <div>
            <a class="tipografiaLogout" href="${pageContext.request.contextPath}/Login.jsp">Logout</a>
            <img class="imagemLogout" src="${pageContext.request.contextPath}/Imagens/logout.svg" alt="Logout">
        </div>
    </aside>

    <main>
        <div>
            <h2 class="tipografiaAreaRestrita">Área restrita</h2>
            <p class="tipografiaNomeDaAba">Setores</p>
        </div>

        <c:if test="${not empty erro}">
            <div class="mensagem-erro" role="alert">
                <c:out value="${erro}"/>
            </div>
        </c:if>

        <c:if test="${not empty sucesso}">
            <div class="mensagem-sucesso" role="status">
                <c:out value="${sucesso}"/>
            </div>
        </c:if>

        <div class="barraPesquisar">
            <p class="tipografiaPesquisar">Pesquisar</p>
            <input type="text" id="campoPesquisar" placeholder="Pesquisar setor..." aria-label="Pesquisar setores">
            <img class="imagemPesquisar" src="${pageContext.request.contextPath}/Imagens/pesquisar2.svg" alt="Pesquisar">
        </div>

        <div class="barraFiltros">
            <label class="tipografiaFiltro" for="filtroStatus">Filtro</label>
            <select id="filtroStatus" aria-label="Filtrar setores por status">
                <option value="">Todos</option>
                <option value="ativo">Ativo</option>
                <option value="inativo">Inativo</option>
            </select>
            <img class="imagemBarraBaixo" src="${pageContext.request.contextPath}/Imagens/setaBaixo.svg" alt="Seta para baixo">
        </div>

        <div>
            <button class="botaoAdicionar" type="button" id="btnAbrirModal">Adicionar novo</button>
            <img src="${pageContext.request.contextPath}/Imagens/Mais.svg" alt="Adicionar">
        </div>

        <div id="modalSetores" class="${not empty erro ? 'ativo' : ''}">
            <div>
                <h2>Adicionar Setor</h2>
                <button type="button" id="btnFecharModal" aria-label="Fechar">✕</button>

                <form action="${pageContext.request.contextPath}/setores" method="post">
                    <label for="nomeSetor">Nome do setor *</label>
                    <input type="text" id="nomeSetor" name="campoNomeSetor" placeholder="Nome do setor" maxlength="255" value="<c:out value='${campoNomeSetor}'/>" required>

                    <label for="idUnidade">Unidade *</label>
                    <select id="idUnidade" name="campoIdUnidade" required>
                        <option value="">Selecione uma unidade</option>
                        <c:forEach var="unidade" items="${unidades}">
                            <option value="${unidade.id}" <c:if test="${campoIdUnidade == unidade.id}">selected</c:if>><c:out value="${unidade.name}"/></option>
                        </c:forEach>
                    </select>

                    <label for="descricao">Descrição *</label>
                    <textarea id="descricao" name="campoDescricao" placeholder="Descrição do setor" required><c:out value="${campoDescricao}"/></textarea>

                    <button type="submit">Adicionar</button>
                </form>
            </div>
        </div>

        <div class="tabela">
            <table>
                <thead class="tabelaThread">
                <tr class="tabelaLinhas">
                    <th class="tipografiaTopoTabela">Setor</th>
                    <th class="tipografiaTopoTabela">ID</th>
                    <th class="tipografiaTopoTabela">Unidade</th>
                    <th class="tipografiaTopoTabela">Status</th>
                    <th class="tipografiaTopoTabela">Descrição</th>
                    <th class="tipografiaTopoTabela">Ações</th>
                </tr>
                </thead>

                <tbody>
                <c:choose>
                    <c:when test="${not empty setores}">
                        <c:forEach var="setor" items="${setores}">
                            <tr class="tabelaLinhas">
                                <td class="tipografiaTabelaGeral"><c:out value="${setor.name}"/></td>
                                <td class="tipografiaTabelaGeral"><c:out value="${setor.id}"/></td>
                                <td class="tipografiaTabelaGeral"><c:out value="${setor.unit.name}"/></td>
                                <td class="tipografiaTabelaGeral">
                                    <c:choose>
                                        <c:when test="${setor.active}">Ativo</c:when>
                                        <c:otherwise>Inativo</c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="tipografiaTabelaGeral"><c:out value="${setor.description}"/></td>
                                <td>
                                    <button type="button" aria-label="Visualizar setor"><img src="${pageContext.request.contextPath}/Imagens/VER.svg" alt="Visualizar"></button>
                                    <button type="button" aria-label="Editar setor"><img src="${pageContext.request.contextPath}/Imagens/EDITAR.svg" alt="Editar"></button>

                                    <c:if test="${setor.active}">
                                        <form action="${pageContext.request.contextPath}/setores" method="post">
                                            <input type="hidden" name="acao" value="excluir">
                                            <input type="hidden" name="id" value="<c:out value='${setor.id}'/>">
                                            <button type="submit" aria-label="Desativar setor" onclick="return confirm('Tem certeza de que deseja desativar este setor?')"><img src="${pageContext.request.contextPath}/Imagens/LIXO.svg" alt="Desativar"></button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <tr class="tabelaLinhas">
                            <td class="tipografiaTabelaGeral" colspan="6">Nenhum setor encontrado.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </main>

    <script>
        const btnAbrir = document.getElementById('btnAbrirModal');
        const btnFechar = document.getElementById('btnFecharModal');
        const modal = document.getElementById('modalSetores');
        const campoPesquisar = document.getElementById('campoPesquisar');
        const filtroStatus = document.getElementById('filtroStatus');

        btnAbrir.addEventListener('click', function () {
            modal.classList.add('ativo');
        });

        btnFechar.addEventListener('click', function () {
            modal.classList.remove('ativo');
        });

        function filtrarSetores() {
            const pesquisa = campoPesquisar.value.toLowerCase().trim();
            const statusSelecionado = filtroStatus.value.toLowerCase();
            const linhas = document.querySelectorAll('.tabela tbody tr');

            linhas.forEach(function (linha) {
                const colunas = linha.querySelectorAll('td');

                if (colunas.length < 5) {
                    linha.style.display = '';
                    return;
                }

                const nome = colunas[0].textContent.toLowerCase();
                const unidade = colunas[2].textContent.toLowerCase();
                const status = colunas[3].textContent.toLowerCase().trim();
                const descricao = colunas[4].textContent.toLowerCase();

                const correspondePesquisa = nome.includes(pesquisa) || unidade.includes(pesquisa) || descricao.includes(pesquisa);
                const correspondeStatus = statusSelecionado === '' || status === statusSelecionado;

                linha.style.display = correspondePesquisa && correspondeStatus ? '' : 'none';
            });
        }

        campoPesquisar.addEventListener('input', filtrarSetores);
        filtroStatus.addEventListener('change', filtrarSetores);
    </script>

</body>
</html>

