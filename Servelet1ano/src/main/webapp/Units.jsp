<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Unidades do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg">

    <link rel="stylesheet" href="Global2.css">


</head>
<body>

    <aside class="aside">


        <div >
            <img  class="logoAether" src="Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
            <p class="tipografiaLogo">aether</p>
        </div>

        <div>
            <img class="aeko" src="Imagens/AekoLogin.svg" alt="Imagem do Aeko">
            <p class="tipografiaAdmin">ADMIN</p>
        </div>

        <nav>
            <ul>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="Dashboard.jsp">Dashboards</a>
                        <img class="imagemDashboard" src="Imagens/dashboard.svg" alt="Imagem de uma Casa">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="Employee.jsp">Funcionários</a>
                        <img class="imagemFuncionario" src="Imagens/funcionario.svg" alt="Imagem de um Usuário cujo rosto não é identificado">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="Sectors.jsp">Setores</a>
                        <img class="imagemSetores" src="Imagens/setores.svg" alt="Imagem de setor">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="Units.jsp">Unidades</a>
                        <img class="imagemUnidade" src="Imagens/unidade.svg" alt="Imagem de prédios">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="Company.jsp">Empresas</a>
                        <img class="imagemEmpresas" src="Imagens/Empresa.svg" alt="Imagem de Quadrados">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="PermissionsOne.jsp">Permissões</a>
                        <img class="imagemPermissoes1" src="Imagens/permissao1.svg" alt="Imagem de uma pessoa com chave">
                    </div>
                </li>
                <li>
                    <div>
                        <a class="tipografiaOpcoesTela" href="PermissionsOne.jsp">Permissões</a>
                        <img class="imagemPermissoes2" src="Imagens/permissao2.svg" alt="Imagem de nuvem">
                    </div>
                </li>
            </ul>
        </nav>

        <div>
            <a class="tipografiaLogout" href="Login.jsp">Logout</a>
            <img class="imagemLogout" src="Imagens/logout.svg" alt="Logout">
        </div>

    </aside>

    <div>

        <h2 class="tipografiaAreaRestrita">Area restrista</h2>
        <p class="tipografiaNomeDaAba">Unidades</p>

    </div>

    <div class="barraPesquisar">
        <p class="tipografiaPesquisar">Pesquisar</p>
        <img class="imagemPesquisar" src="Imagens/pesquisar2.svg" alt="Imagem de Pesquisar">
    </div>

    <div class="barraFiltros">
        <p class="tipografiaFiltro">Filtro</p>
        <img class="imagemBarraBaixo" src="Imagens/setaBaixo.svg" alt="Seta para Baixo">
    </div>

    <div>
        <button class="botaoAdicionar" id="btnAbrirModal">Adicionar novo</button>
        <img src="Imagens/Mais.svg" alt="Adicionar">
    </div>

    <div id="modalUnidades">

        <div>
            <h2>Adicionar Unidade</h2>
            <button id="btnFecharModal">✕</button>

            <form action="AdicionarUnidade" method="post">

                <input type="text" id="nomeUnidade" name="campoNomeUnidade" placeholder="nome">
                <label for="nomeUnidade"></label>

                <input type="text" id="id" name="campoId" placeholder="id">
                <label for="id"></label>

                <input type="text" id="endereco" name="campoEndereco" placeholder="endereço">
                <label for="endereco"></label>

                <input type="text" id="CNPJ" name="campoCNPJ" placeholder="CNPJ">
                <label for="CNPJ"></label>

                <input type="text" id="CNAE" name="campoCNAE" placeholder="CNAE">
                <label for="CNAE"></label>

                <input type="text" id="empresaMatriz" name="campoEmpresaMatriz" placeholder="empresa matriz">
                <label for="empresaMatriz"></label>

                <button type="submit" value="enviarFormularioAdicionarUnidade">Adicionar</button>

            </form>

        </div>

    </div>



    <div class="tabela">
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
                </tr>
            </thead>
            <tbody>

                <tr class="tabelaLinhas">
                    <td class="tipografiaTabelaGeral">Caio Campelo Rocha</td>
                    <td class="tipografiaTabelaGeral">350</td>
                    <td class="tipografiaTabelaGeral">Rua Caio</td>
                    <td class="tipografiaTabelaGeral">Ativo</td>
                    <td class="tipografiaTabelaGeral">3232121</td>
                    <td class="tipografiaTabelaGeral">3213231</td>
                    <td class="tipografiaTabelaGeral">Caio</td>


                    <td>
                        <button><img src="Imagens/VER.svg" alt="Imagem de um Olho"></button>
                        <button><img src="Imagens/EDITAR.svg" alt="Imagem de um Lápis"></button>
                        <button><img src="Imagens/LIXO.svg" alt="Imagem de uma Lixeira"></button>
                    </td>

                </tr>
            </tbody>
        </table>
    </div>


    <script>
        const btnAbrir = document.getElementById('btnAbrirModal');
        const btnFechar = document.getElementById('btnFecharModal');
        const modal = document.getElementById('modalUnidades');

        btnAbrir.addEventListener('click', function() {
            modal.classList.add('ativo');
        });

        btnFechar.addEventListener('click', function() {
            modal.classList.remove('ativo');
        });
    </script>

</body>
</html>