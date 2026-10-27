```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tela de Unidades do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg" type="image/svg+xml">
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
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
                        <img class="imagemDashboard" src="${pageContext.request.contextPath}/Imagens/dashboard.svg" alt="Dashboard">
                    </div>
                </li>

                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/funcionarios">Funcionários</a>
                        <img class="imagemFuncionario" src="${pageContext.request.contextPath}/Imagens/funcionario.svg" alt="Funcionários">
                    </div>
                </li>

                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/setores">Setores</a>
                        <img class="imagemSetores" src="${pageContext.request.contextPath}/Imagens/setores.svg" alt="Setores">
                    </div>
                </li>

                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/unidades">Unidades</a>
                        <img class="imagemUnidade" src="${pageContext.request.contextPath}/Imagens/unidade.svg" alt="Unidades">
                    </div>
                </li>

                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/empresas">Empresas</a>
                        <img class="imagemEmpresas" src="${pageContext.request.contextPath}/Imagens/Empresa.svg" alt="Empresas">
                    </div>
                </li>

                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/permissoes">Permissões</a>
                        <img class="imagemPermissoes1" src="${pageContext.request.contextPath}/Imagens/permissao1.svg" alt="Permissões de usuários">
                    </div>
                </li>

                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/permissoes">Permissões</a>
                        <img class="imagemPermissoes2" src="${pageContext.request.contextPath}/Imagens/permissao2.svg" alt="Permissões de nuvem">
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
            <p class="tipografiaNomeDaAba">Unidades</p>
        </div>

        <c:if test="${not empty erro}">
            <div role="alert">
                <c:out value="${erro}"/>
            </div>
        </c:if>

        <c:if test="${not empty sucesso}">
            <div role="status">
                <c:out value="${sucesso}"/>
            </div>
        </c:if>

        <div class="barraPesquisar">
            <p class="tipografiaPesquisar">Pesquisar</p>
            <img class="imagemPesquisar" src="${pageContext.request.contextPath}/Imagens/pesquisar2.svg" alt="Pesquisar">
        </div>

        <div class="barraFiltros">
            <p class="tipografiaFiltro">Filtro</p>
            <img class="imagemBarraBaixo" src="${pageContext.request.contextPath}/Imagens/setaBaixo.svg" alt="Seta para baixo">
        </div>

        <div>
            <button type="button" class="botaoAdicionar" id="btnAbrirModal">Adicionar novo</button>
            <img src="${pageContext.request.contextPath}/Imagens/Mais.svg" alt="Adicionar">
        </div>

        <div id="modalUnidades" class="${not empty erro ? 'ativo' : ''}">
            <div>
                <h2>Adicionar Unidade</h2>
                <button type="button" id="btnFecharModal" aria-label="Fechar">✕</button>

                <form action="${pageContext.request.contextPath}/unidades" method="post">

                    <label for="nomeUnidade">Nome da unidade *</label>
                    <input type="text" id="nomeUnidade" name="campoNomeUnidade" placeholder="Nome da unidade" maxlength="255" value="<c:out value='${campoNomeUnidade}'/>" required>

                    <label for="cnpj">CNPJ *</label>
                    <input type="text" id="cnpj" name="campoCNPJ" placeholder="CNPJ" value="<c:out value='${campoCNPJ}'/>" required>

                    <label for="cnae">CNAE *</label>
                    <input type="text" id="cnae" name="campoCNAE" placeholder="CNAE" value="<c:out value='${campoCNAE}'/>" required>

                    <h3>Endereço</h3>

                    <label for="cep">CEP</label>
                    <input type="text" id="cep" name="campoCEP" placeholder="CEP" value="<c:out value='${campoCEP}'/>">

                    <label for="rua">Rua *</label>
                    <input type="text" id="rua" name="campoRua" placeholder="Rua" value="<c:out value='${campoRua}'/>" required>

                    <label for="numero">Número *</label>
                    <input type="text" id="numero" name="campoNumero" placeholder="Número" value="<c:out value='${campoNumero}'/>" required>

                    <label for="complemento">Complemento</label>
                    <input type="text" id="complemento" name="campoComplemento" placeholder="Complemento" value="<c:out value='${campoComplemento}'/>">

                    <label for="bairro">Bairro *</label>
                    <input type="text" id="bairro" name="campoBairro" placeholder="Bairro" value="<c:out value='${campoBairro}'/>" required>

                    <label for="cidade">Cidade *</label>
                    <input type="text" id="cidade" name="campoCidade" placeholder="Cidade" value="<c:out value='${campoCidade}'/>" required>

                    <label for="estado">Estado *</label>
                    <input type="text" id="estado" name="campoEstado" placeholder="Estado" value="<c:out value='${campoEstado}'/>" required>

                    <label for="pais">País *</label>
                    <input type="text" id="pais" name="campoPais" placeholder="País" value="<c:out value='${campoPais}'/>" required>

                    <button type="submit">Adicionar</button>
                </form>
            </div>
        </div>

        <section class="tabela">
            <table>
                <thead class="tabelaThread">
                <tr class="tabelaLinhas">
                    <th class="tipografiaTopoTabela">Nome</th>
                    <th class="tipografiaTopoTabela">ID</th>
                    <th class="tipografiaTopoTabela">Endereço</th>
                    <th class="tipografiaTopoTabela">Status</th>
                    <th class="tipografiaTopoTabela">CNPJ</th>
                    <th class="tipografiaTopoTabela">CNAE</th>
                    <th class="tipografiaTopoTabela">Empresa</th>
                    <th class="tipografiaTopoTabela">Ações</th>
                </tr>
                </thead>

                <tbody>
                <c:choose>
                    <c:when test="${not empty unidades}">
                        <c:forEach var="unidade" items="${unidades}">
                            <tr class="tabelaLinhas">
                                <td class="tipografiaTabelaGeral"><c:out value="${unidade.name}"/></td>
                                <td class="tipografiaTabelaGeral"><c:out value="${unidade.id}"/></td>

                                <td class="tipografiaTabelaGeral">
                                    <c:if test="${not empty unidade.address}">
                                        <c:out value="${unidade.address.street}"/>,
                                        <c:out value="${unidade.address.number}"/>

                                        <c:if test="${not empty unidade.address.complement}">
                                            - <c:out value="${unidade.address.complement}"/>
                                        </c:if>

                                        <br>
                                        <c:out value="${unidade.address.neighborhood}"/> -
                                        <c:out value="${unidade.address.city}"/>/
                                        <c:out value="${unidade.address.state}"/>
                                        <br>
                                        CEP: <c:out value="${unidade.address.zipCode}"/>
                                    </c:if>
                                </td>

                                <td class="tipografiaTabelaGeral">
                                    <c:choose>
                                        <c:when test="${unidade.active}">Ativa</c:when>
                                        <c:otherwise>Inativa</c:otherwise>
                                    </c:choose>
                                </td>

                                <td class="tipografiaTabelaGeral"><c:out value="${unidade.cnpj}"/></td>
                                <td class="tipografiaTabelaGeral"><c:out value="${unidade.cnae}"/></td>
                                <td class="tipografiaTabelaGeral"><c:out value="${unidade.company.name}"/></td>

                                <td>
                                    <button type="button" aria-label="Visualizar unidade"><img src="${pageContext.request.contextPath}/Imagens/VER.svg" alt="Visualizar"></button>
                                    <button type="button" aria-label="Editar unidade"><img src="${pageContext.request.contextPath}/Imagens/EDITAR.svg" alt="Editar"></button>
                                    <button type="button" aria-label="Excluir unidade"><img src="${pageContext.request.contextPath}/Imagens/LIXO.svg" alt="Excluir"></button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <tr class="tabelaLinhas">
                            <td class="tipografiaTabelaGeral" colspan="8">Nenhuma unidade encontrada.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </section>

    </main>

    <script>
        const btnAbrir = document.getElementById('btnAbrirModal');
        const btnFechar = document.getElementById('btnFecharModal');
        const modal = document.getElementById('modalUnidades');

        btnAbrir.addEventListener('click', function () {
            modal.classList.add('ativo');
        });

        btnFechar.addEventListener('click', function () {
            modal.classList.remove('ativo');
        });
    </script>

</body>
</html>


