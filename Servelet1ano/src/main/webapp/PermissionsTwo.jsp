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