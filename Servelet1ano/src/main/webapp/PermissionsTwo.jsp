<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Permissões do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg">

</head>
<body>

    <aside class="aside">
        <div>
            <img class="logoAether" src="Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
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

    <h2 class="tipografiaAreaRestrita">Area restrita</h2>
    <p class="tipografiaNomeDaAba">Dashboard</p>

<div>

    <h2>Area restrista</h2>
    <p>Permissões Aether</p>

</div>

<section>

    <div>

        <div>

            <div>
                <div>
                    imagem
                </div>
                <h3>Criar Inventários</h3>
            </div>

            <p>Habilitar esta permissão concede ao funcionário a habilidade de criar de inventários.</p>

            <div>

                <label >
                    <input type="checkbox" checked>
                    <span>fazer animação</span>
                </label>

            </div>
        </div>

        <div>

            <div>

                <div>
                    imagem
                </div>

                <h3>Excluir Inventários</h3>

            </div>

            <p>Habilitar esta permissão concede ao funcionário a habilidade de excluir inventários.</p>

            <div>
                <label >
                    <input type="checkbox">
                    <span>animacao</span>
                </label>
            </div>
        </div>

        <div>

            <div>

                <div>
                    imagem
                </div>

                <h3>Visualizar Inventários</h3>

            </div>

            <p>Habilitar esta permissão concede ao funcionário a habilidade de visualizar inventários.</p>

            <div>
                <label>
                    <input type="checkbox" checked>
                    <span></span>
                </label>
            </div>
        </div>

        <div>

            <div>

                <div>
                    imagem
                </div>

                <h3>Visualizar Análises de ROI</h3>

            </div>

            <p>Habilitar esta permissão concede ao funcionário a habilidade de visualizar análises de ROI.</p>

            <div>

                <label>
                    <input type="checkbox" checked>
                    <span></span>
                </label>

            </div>

        </div>

    </div>

    <div>
        <span>Mostrando 4 por página</span>
    </div>

</section>
</body>