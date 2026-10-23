<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Empresas do Aether</title>

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


        <div>
            <img src="Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
            <p>aether</p>
        </div>

        <div>
            <img src="Imagens/AekoLogin.svg">
            <p>ADMIN</p>
        </div>

        <nav>
            <ul>
                <li>
                    <div>
                        <a href="Dashboard.jsp">Dashboards</a>
                        <img src="Imagens/dashboard.svg" alt="Imagem de uma Casa">
                    </div>
                </li>
                <li>
                    <div>
                        <a href="Employee.jsp">Funcionários</a>
                        <img src="Imagens/funcionario.svg" alt="Imagem de um Usuário cujo rosto não é identificado">
                    </div>
                </li>
                <li>
                    <div>
                        <a href="Sectors.jsp">Setores</a>
                        <img src="Imagens/setores.svg" alt="Imagem de setor">
                    </div>
                </li>
                <li>
                    <div>
                        <a href="Units.jsp">Unidades</a>
                        <img src="Imagens/unidade.svg" alt="Imagem de prédios">
                    </div>
                </li>
                <li>
                    <div>
                        <a href="Company.jsp">Empresas</a>
                        <img src="Imagens/Empresa.svg" alt="Imagem de Quadrados">
                    </div>
                </li>
                <li>
                    <div>
                        <a href="PermissionsOne.jsp">Permissões</a>
                        <img src="Imagens/permissao1.svg" alt="Imagem de uma pessoa com chave">
                    </div>
                </li>
                <li>
                    <div>
                        <a href="PermissionsOne.jsp">Permissões</a>
                        <img src="Imagens/permissao2.svg" alt="Imagem de nuvem">
                    </div>
                </li>
            </ul>
        </nav>

        <div>
            <a href="Login.jsp">Logout</a>
            <img src="Imagens/logout.svg" alt="Logout">
        </div>

    </aside>

    <div>

        <h2>Area restrista</h2>
        <p>Empresas</p>

    </div>

        <div>
            <p>Pesquisar</p>
            <img src="Imagens/pesquisar2.svg" alt="Imagem de Pesquisar">
        </div>

        <div>
            <p>Filtro</p>
        </div>

    <div>
        <button id="btnAbrirModal">Adicionar novo</button>
        <img src="Imagens/Mais.svg" alt="Adicionar">
    </div>

    <div id="modalEmpresa">

        <div>
            <h2>Adicionar empresa</h2>
            <button id="btnFecharModal">✕</button>

            <form action="AdicionarEmpresa" method="post">

                <input type="text" id="nomeEmpresa" name="campoNomeEmpresa" placeholder="nome">
                <label for="nomeEmpresa"></label>

                <input type="text" id="taxID" name="campoTaxID" placeholder="tax ID">
                <label for="taxID"></label>

                <input type="text" id="setor" name="campoSetor" placeholder="setor">
                <label for="setor"></label>

                <input type="date" id="dtRegistro" name="campoDataRegistro" placeholder="data registro">
                <label for="dtRegistro"></label>

                <label for="status"></label>
                <select name="status" id="status" >
                    <option value="" disabled selected hidden>ativo</option>
                    <option value="inativo">inativo</option>
                    <option value="ativo">ativo</option>
                </select>

                <label for="tamanho"></label>
                <select name="tamanhos" id="tamanho" >
                    <option value="" disabled selected hidden>grande</option>
                    <option value="inativo">média</option>
                    <option value="recessa">pequena</option>
                    <option value="licenca">grande</option>
                </select>

                <button type="submit" value="enviarFormularioAdicionarEmpresa">Adicionar</button>

            </form>

        </div>

    </div>



    <section>
        <table>
            <thead>
            <tr>
                <th>Nome</th>
                <th>Tamanho</th>
                <th>Data de Registro</th>
                <th>Tax ID</th>
                <th>E-mail</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>

            <tr>
                <td>Caio Campelo Rocha</td>
                <td>350</td>
                <td>05/02/2029</td>
                <td>19090938208</td>
                <td>jbs@gmail.com</td>
                <td>Ativo</td>

                <td>
                    <button><img src="Imagens/VER.svg" alt="Imagem de um Olho"></button>
                    <button><img src="Imagens/EDITAR.svg" alt="Imagem de um Lápis"></button>
                    <button><img src="Imagens/LIXO.svg" alt="Imagem de uma Lixeira"></button>
                </td>

            </tr>
            </tbody>
        </table>
    </section>

    <button></button>
    <div>1</div>
    <button></button>


    <script>
        const btnAbrir = document.getElementById('btnAbrirModal');
        const btnFechar = document.getElementById('btnFecharModal');
        const modal = document.getElementById('modalEmpresa');

        btnAbrir.addEventListener('click', function() {
            modal.classList.add('ativo');
        });

        btnFechar.addEventListener('click', function() {
            modal.classList.remove('ativo');
        });
    </script>

</body>
</html>