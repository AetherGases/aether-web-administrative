<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Tela de Dashboard do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg">

    <link rel="stylesheet" href="Dashboard.css">
    <link rel="stylesheet" href="Global2.css">


</head>
<body>

    <aside class="aside">


        <div>
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

        <h2 class="tipografiaAreaRestrita">Area restrita </h2>
        <p class="tipografiaNomeDaAba">Dashboard</p>

        <div class="quadradoInformativo">
            <h3 class="tipografiaNomeQuadrado"> Olá, Layous!</h3>
            <p class="tipografiaDescricao">Voce tem 12 mini-layouts aguardando análise hoje </p>

            <a class="botaoRegistro tipografiaRegistro">Ver registros </a>
            <a class="botaoFuncionario tipografiaFuncionario">Ver funcionários</a>
        </div>


        <div class="quadradoInsercoes">
            <h3 class="tituloInsercoes">Inserções recentes</h3>
            <a class="tipografiaVerMais">Ver mais</a>

            <p class="numeracaoIncersoes">1.</p>
            <p class="tipografiaFuncionarioIncersao">Caio Sagaz</p>
            <p class="tipografiaDescricaoIncersao">Mandou um relatório</p>

            <a class="botaoReprovar tipografiaReprovar">Reprovar</a>
            <a class="botaoAprovar tipografiaAprovar">Aprovar</a>

        </div>

        <div>

            <div class="quadradoAtividadeRecente">
                <p class="tipografiaQuadradinhos">Atividades recentes </p>
                <p class="tipografiaNumerosQuadradinhos">24</p>
                <img class="imagemQuadradinhos" src="Imagens/grafico.svg" alt="Imagem de um gráfico">
                <div class="linhaQuadradinho"></div>
            </div>

            <div class="quadradoPermissoes">
                <p class="tipografiaQuadradinhos">Permissões</p>
                <p class="tipografiaNumerosQuadradinhos">8</p>
                <img class="imagemQuadradinhos" src="Imagens/chave.svg" alt="Imagem de uma chave">
                <div class="linhaQuadradinho"></div>
            </div>

            <div class="quadradoFuncionario">
                <p class="tipografiaQuadradinhos">Funcionários</p>
                <p class="tipografiaNumerosQuadradinhos">15</p>
                <img class="imagemQuadradinhosEspecial" src="Imagens/pessoas.svg" alt="Imagem de Pessoas">
                <div class="linhaQuadradinho"></div>
            </div>

            <div class="quadradoUnidade">
                <p class="tipografiaQuadradinhos">Unidades</p>
                <p class="tipografiaNumerosQuadradinhos">07</p>
                <img class="imagemQuadradinhos" src="Imagens/predios.svg" alt="Imagem de Prédios">
                <div class="linhaQuadradinho"></div>
            </div>
        </div>

        <div class="quadradoRelatorios">
            <p class="tipografiaTituloRelatorios">Relatórios revisados</p>
            <p class="tipografiaNumerosQuadradinhos">450</p>
            <p class="tipografiaSubTitulo">Este mês</p>
            <img class="imagemRelatorio" src="Imagens/Relatorio.svg" alt="Imagem de um Relatório">
            <a class="botaoHistorico tipografiaBotaoHistorico">Ver histórico</a>
            <p class="tipografiaDescricaoRelatorio">Último enviado em 11/09/2001</p>
        </div>

        <div class="quadradoEmissao">
            <p class="tipografiaTituloEmissao">Emissões da unidade</p>
            <p class="tipografiaNumerosQuadradinhos">2.403</p>
            <p class="tipografiaSubTitulo">tCO<sub>2</sub>e/mês</p>

            <div class="notificacaoEmissao">
                <p class="tipografiaNotificacao">12% em relação ao período anterior</p>
                <img class="imagemIndustria" src="Imagens/fabrica.svg" alt="Imagem de uma Fábrica">
            </div>

        </div>

        <div class="quadradoSCP">
            <p class="tipopgrafiaTituloSCP">Selo Clima Paraná</p>
            <p class="tipografiaSubtituloSCP">Nível</p>
            <p class="tipografiaNumerosQuadradinhos">50%</p>
            <img class="imagemSelo" src="Imagens/medalha.svg" alt="Imagem de uma Medalha">
            <img class="imagemAviso" src="Imagens/atencao.svg" alt="Imagem de uma Sinal de Atenção">
            <p class="tipografiaCriterios">2 de 4 critérios atendidos</p>
        </div>

</body>
</html>