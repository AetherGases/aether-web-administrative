```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tela de Funcionários do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg" type="image/svg+xml">
    <link rel="stylesheet" href="Global2.css">
    <link rel="stylesheet" href="Employee.css">
</head>
<body>

    <aside class="aside">
        <div>
            <img class="logoAether" src="${pageContext.request.contextPath}/Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
            <p class="tipografiaLogo">aether</p>
        </div>

        <div>
            <img class="aeko" src="${pageContext.request.contextPath}/Imagens/AekoLogin.svg" alt="Imagem do Aeko">
            <h3 class="tipografiaAdmin"><c:out value="${empresa.name}" /></h3>
            <p class="tipografiaAdmin">ADMIN</p>
        </div>

        <nav>
            <ul>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/dashboard">Dashboards</a>
                        <img class="imagemDashboard" src="${pageContext.request.contextPath}/Imagens/dashboard.svg" alt="Imagem de uma Casa">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/funcionarios">Funcionários</a>
                        <img class="imagemFuncionario" src="${pageContext.request.contextPath}/Imagens/funcionario.svg" alt="Imagem de um Usuário">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="${pageContext.request.contextPath}/setores">Setores</a>
                        <img class="imagemSetores" src="${pageContext.request.contextPath}/Imagens/setores.svg" alt="Imagem de setor">
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
            <p class="tipografiaNomeDaAba">Funcionários</p>
        </div>

        <c:if test="${not empty erro}">
            <p class="mensagemErro" role="alert"><c:out value="${erro}" /></p>
        </c:if>

        <c:if test="${not empty sucesso}">
            <p class="mensagemSucesso" role="status"><c:out value="${sucesso}" /></p>
        </c:if>

        <div class="barraPesquisar">
            <label class="tipografiaPesquisar" for="campoPesquisar">Pesquisar</label>
            <input type="search" id="campoPesquisar" aria-label="Pesquisar funcionários">
            <img class="imagemPesquisar" src="${pageContext.request.contextPath}/Imagens/pesquisar2.svg" alt="Imagem de pesquisar">
        </div>

        <div class="barraFiltros">
            <label class="tipografiaFiltro" for="filtroStatus">Filtro</label>
            <select id="filtroStatus">
                <option value="">Todos os status</option>
                <option value="Ativo">Ativo</option>
                <option value="Inativo">Inativo</option>
                <option value="Em recesso">Em recesso</option>
            </select>
            <img class="imagemBarraBaixo" src="${pageContext.request.contextPath}/Imagens/setaBaixo.svg" alt="Seta para baixo">
        </div>

        <div>
            <button type="button" class="botaoAdicionar" id="btnAbrirModal">Adicionar novo</button>
            <img src="${pageContext.request.contextPath}/Imagens/Mais.svg" alt="Adicionar">
        </div>

        <div id="modalFuncionario" <c:if test="${not empty erro}">class="ativo"</c:if>>
            <div class="quadradoAdicionarFuncionarioTopo quadradoAdicionarFuncionario">
                <h2 class="tipografiaAdicionarFuncionarioTopo">Adicionar funcionário</h2>
                <img src="${pageContext.request.contextPath}/Imagens/AdicionarFuncionario.svg" alt="Imagem de adicionar funcionário">
                <button type="button" id="btnFecharModal" class="sairDaTelaAddFuncionario" aria-label="Fechar formulário"></button>

                <form action="${pageContext.request.contextPath}/funcionarios" method="post" accept-charset="UTF-8">
                    <label for="nomeFuncionario">Nome completo</label>
                    <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="text" id="nomeFuncionario" name="campoNomeFuncionario" maxlength="255" value="<c:out value='${campoNomeFuncionario}'/>" placeholder="Nome completo" required>

                    <label for="email">E-mail</label>
                    <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="email" id="email" name="campoEmail" maxlength="255" value="<c:out value='${campoEmail}'/>" placeholder="E-mail" required>

                    <label for="telefone">Telefone</label>
                    <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="tel" id="telefone" name="campoTelefone" value="<c:out value='${campoTelefone}'/>" placeholder="Telefone" required>

                    <label for="cpf">CPF</label>
                    <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="text" id="cpf" name="campoCPF" value="<c:out value='${campoCPF}'/>" placeholder="CPF" required>

                    <label for="unitId">Unidade</label>
                    <select class="campoStatus tipografiaCamposAdicionarFuncionario" id="unitId" name="unitId" required>
                        <option value="">Selecione uma unidade</option>
                        <c:forEach var="unidade" items="${unidades}">
                            <option value="${unidade.id}" <c:if test="${campoUnidade eq unidade.id.toString()}">selected</c:if>><c:out value="${unidade.name}" /></option>
                        </c:forEach>
                    </select>

                    <label for="sectorId">Setor</label>
                    <select class="campoStatus tipografiaCamposAdicionarFuncionario" id="sectorId" name="sectorId" required>
                        <option value="">Selecione um setor</option>
                        <c:forEach var="setor" items="${setores}">
                            <option value="${setor.id}" <c:if test="${campoSetor eq setor.id.toString()}">selected</c:if>><c:out value="${setor.name}" /></option>
                        </c:forEach>
                    </select>

                    <label for="senha">Senha</label>
                    <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="password" id="senha" name="campoSenha" maxlength="1024" placeholder="Senha" autocomplete="new-password" required>

                    <label for="confirmarSenha">Confirmar senha</label>
                    <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="password" id="confirmarSenha" name="campoConfirmarSenha" maxlength="1024" placeholder="Confirmar senha" autocomplete="new-password" required>

                    <label for="status">Status</label>
                    <select class="campoStatus tipografiaCamposAdicionarFuncionario" id="status" name="status" required>
                        <option value="">Selecione um status</option>
                        <option value="Ativo" <c:if test="${campoStatus eq 'Ativo'}">selected</c:if>>Ativo</option>
                        <option value="Inativo" <c:if test="${campoStatus eq 'Inativo'}">selected</c:if>>Inativo</option>
                        <option value="Em recesso" <c:if test="${campoStatus eq 'Em recesso'}">selected</c:if>>Em recesso</option>
                    </select>

                    <button class="botaoAdicionar tipografiaBotao" type="submit">Adicionar</button>
                    <div class="linha"></div>
                </form>
            </div>
        </div>

        <div class="tabela">
            <table>
                <thead class="tabelaThread">
                <tr class="tabelaLinhas">
                    <th class="tipografiaTopoTabela">Nome</th>
                    <th class="tipografiaTopoTabela">E-mail</th>
                    <th class="tipografiaTopoTabela">Status</th>
                    <th class="tipografiaTopoTabela">Setor</th>
                    <th class="tipografiaTopoTabela">Ações</th>
                </tr>
                </thead>
                <tbody>
                <tr class="tabelaLinhas">
                    <td class="tipografiaTabelaGeral">Caio Campelo Rocha</td>
                    <td class="tipografiaTabelaGeral">caio.rocha@gmail.com</td>
                    <td class="tipografiaTabelaGeral">Ativo</td>
                    <td class="tipografiaTabelaGeral">I. Têxtil</td>
                    <td>
                        <button type="button" aria-label="Visualizar funcionário"><img src="${pageContext.request.contextPath}/Imagens/VER.svg" alt="Visualizar"></button>
                        <button type="button" aria-label="Editar funcionário"><img src="${pageContext.request.contextPath}/Imagens/EDITAR.svg" alt="Editar"></button>
                        <button type="button" aria-label="Excluir funcionário"><img src="${pageContext.request.contextPath}/Imagens/LIXO.svg" alt="Excluir"></button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="quadradoAdicionarFuncionarioTopo quadradoAdicionarFuncionario">
            <h2 class="tipografiaAdicionarFuncionarioTopo">Alterar funcionário</h2>
            <img class="imagemAdicionarFuncionario" src="${pageContext.request.contextPath}/Imagens/AlterarFuncionario.svg" alt="Imagem de um lápis">
            <button type="button" class="sairDaTelaAddFuncionario" aria-label="Fechar formulário de alteração"></button>

            <form action="${pageContext.request.contextPath}/funcionarios" method="post" accept-charset="UTF-8">
                <input type="hidden" name="acao" value="alterar">

                <label for="editarNomeFuncionario">Nome completo</label>
                <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="text" id="editarNomeFuncionario" name="campoNomeFuncionario" placeholder="Nome completo">

                <label for="editarEmail">E-mail</label>
                <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="email" id="editarEmail" name="campoEmail" placeholder="E-mail">

                <label for="editarTelefone">Telefone</label>
                <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="tel" id="editarTelefone" name="campoTelefone" placeholder="Telefone">

                <label for="editarCpf">CPF</label>
                <input class="adicionarFuncionarioCampos tipografiaCamposAdicionarFuncionario" type="text" id="editarCpf" name="campoCPF" placeholder="CPF">

                <label for="editarUnitId">Unidade</label>
                <select class="campoStatus tipografiaCamposAdicionarFuncionario" id="editarUnitId" name="unitId">
                    <option value="">Selecione uma unidade</option>
                    <c:forEach var="unidade" items="${unidades}">
                        <option value="${unidade.id}"><c:out value="${unidade.name}" /></option>
                    </c:forEach>
                </select>

                <label for="editarSectorId">Setor</label>
                <select class="campoStatus tipografiaCamposAdicionarFuncionario" id="editarSectorId" name="sectorId">
                    <option value="">Selecione um setor</option>
                    <c:forEach var="setor" items="${setores}">
                        <option value="${setor.id}"><c:out value="${setor.name}" /></option>
                    </c:forEach>
                </select>

                <label for="editarStatus">Status</label>
                <select class="campoStatus tipografiaCamposAdicionarFuncionario" id="editarStatus" name="status">
                    <option value="Ativo">Ativo</option>
                    <option value="Inativo">Inativo</option>
                    <option value="Em recesso">Em recesso</option>
                </select>

                <button class="botaoAlterar tipografiaBotao" type="submit">Alterar</button>
                <div class="linha"></div>
            </form>
        </div>

        <div class="detalhesFuncionario">
            <p>Nome</p>
            <div><p id="detalheNome">Nome</p></div>

            <p>E-mail</p>
            <div><p id="detalheEmail">E-mail</p></div>

            <p>Setor</p>
            <div><p id="detalheSetor">Setor</p></div>

            <p>Data de admissão</p>
            <div><p id="detalheAdmissao">Data de admissão</p></div>

            <p>Senha atual</p>
            <div><p>********</p></div>

            <p>Status</p>
            <div><p id="detalheStatus">Ativo</p></div>
        </div>

        <div class="confirmacaoExclusao">
            <div>
                <p>Deletar</p>
                <div></div>
                <img src="${pageContext.request.contextPath}/Imagens/lixeira.svg" alt="Imagem de lixeira">
            </div>

            <img src="${pageContext.request.contextPath}/Imagens/perigo.svg" alt="Imagem indicando atenção">
            <p>Tem certeza que deseja deletar o(s) registro(s) selecionado(s)?</p>
            <p>Essa ação não poderá ser desfeita.</p>

            <button type="button">Cancelar</button>
            <button type="button">Deletar</button>
        </div>
    </main>

    <script>
        const btnAbrir = document.getElementById("btnAbrirModal");
        const btnFechar = document.getElementById("btnFecharModal");
        const modal = document.getElementById("modalFuncionario");

        btnAbrir.addEventListener("click", function () {
            modal.classList.add("ativo");
        });

        btnFechar.addEventListener("click", function () {
            modal.classList.remove("ativo");
        });

        const campoPesquisar = document.getElementById("campoPesquisar");
        const filtroStatus = document.getElementById("filtroStatus");
        const linhasTabela = document.querySelectorAll(".tabela tbody tr");

        function filtrarFuncionarios() {
            const pesquisa = campoPesquisar.value.toLowerCase();
            const statusSelecionado = filtroStatus.value.toLowerCase();

            linhasTabela.forEach(function (linha) {
                const textoLinha = linha.textContent.toLowerCase();
                const statusLinha = linha.cells[2].textContent.trim().toLowerCase();
                const correspondePesquisa = textoLinha.includes(pesquisa);
                const correspondeStatus = statusSelecionado === "" || statusLinha === statusSelecionado;

                linha.style.display = correspondePesquisa && correspondeStatus ? "" : "none";
            });
        }

        campoPesquisar.addEventListener("input", filtrarFuncionarios);
        filtroStatus.addEventListener("change", filtrarFuncionarios);
    </script>

</body>
</html>
