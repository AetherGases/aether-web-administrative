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

</head>
<body>

<aside>


    <div >
        <img src="Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
        <p>aether</p>
    </div>

    <div>
        <h3>Giulia Cacá</h3>
        <p>ADMIN</p>
    </div>

    <nav>
        <ul>
            <li><a href="">Dashboards</a></li>
            <li><a href="">Funcionários</a></li>
            <li><a href="">Setores</a></li>
            <li><a href="">Unidades</a></li>
            <li><a href="">Empresas</a></li>
            <li><a href="">Permissões</a></li>
            <li><a href="">Permissões</a></li>
        </ul>
    </nav>

    <div>
        <a href="Login.jsp">Logout</a>
    </div>

</aside>

<div>

    <h2>Area restrista</h2>
    <p>Unidades</p>

</div>

<div>
    <button id="btnAbrirModal">Adicionar novo</button>
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



<section>
    <table>
        <thead>
        <tr>
            <th>Nome</th>
            <th>ID</th>
            <th>Endereço</th>
            <th>Status</th>
            <th>CNPJ</th>
            <th>CNAE</th>
            <th>Empresa</th>
        </tr>
        </thead>
        <tbody>

        <tr>
            <td>Caio Campelo Rocha</td>
            <td>350</td>
            <td>sdmfdlfs</td>
            <td>Ativo</td>
            <td>3232121</td>
            <td>3213231</td>
            <td>sla</td>


            <td>
                <button></button>
                <button></button>
                <button></button>
            </td>

        </tr>
        </tbody>
    </table>
</section>


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