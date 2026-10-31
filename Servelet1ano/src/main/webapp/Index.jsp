<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description"
          content="Aether — descarbonizando o futuro: recomendação de alternativas verdes para gases de efeito estufa.">

    <title>Landing Page Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@400;500;600;700&family=Montserrat:ital,wght@0,400;0,500;0,600;0,700;1,400;1,700&display=swap">

    <link rel="stylesheet" href="Style.css">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg+xml">
</head>
<body>

<!-- =========================================================
     CABEÇALHO
     ========================================================= -->
<header class="cabecalho">

    <a href="#inicio" class="linkLogo" aria-label="Aether, ir para o início">
        <img class="logoInicial" src="Imagens/Logo.svg" alt="Logo Aether">
    </a>

    <button id="botaoMenuMobile" class="botaoMenuMobile" type="button"
            aria-controls="menuPrincipal" aria-expanded="false" aria-label="Abrir menu">
        <span class="iconeHamburguer" aria-hidden="true"></span>
    </button>

    <nav id="menuPrincipal" class="retanguloMenuInicial montserrat" aria-label="Menu principal">
        <ul class="listaDeOpcoesMenu">
            <li><a href="#quemSomos">QUEM SOMOS</a></li>
            <li><a href="#gasesPoluentes">GEE</a></li>
            <li><a href="#nossaSolucao">NOSSA SOLUÇÃO</a></li>
            <li><a href="#planos">PLANOS</a></li>
            <li class="itemMenuMobile"><a class="linkMenuEntrar" href="Login.jsp">Entrar</a></li>
            <li class="itemMenuMobile"><a class="linkMenuCadastrar" href="CompanyRegister.jsp">Cadastrar-se</a></li>
        </ul>
    </nav>

    <div class="acoesCabecalho">
        <a class="botaoEntrar conteudoBotaoEntrar" href="Login.jsp">Entrar</a>
        <a class="botaoCadastrar conteudoBotaoCadastar" href="CompanyRegister.jsp">Cadastrar-se</a>
    </div>
</header>


<main id="conteudo">

    <!-- =========================================================
         SETOR 1 — TOPO / HERO
         ========================================================= -->
    <section class="setor setorInicial" id="inicio">

        <div class="heroTexto">
            <h1 class="montserrat descarbonizando">DESCARBONIZANDO</h1>
            <p class="palavraFuturo">O FUTURO</p>

            <p class="descricaoInicial">Conheça nosso projeto Aether, que tem como seu
                foco reduzir a emissão de gases de efeito estufa através de
                sugestões para troca de seus respectivos gases verdes, ou seja,
                menor potencial de aquecimento global.</p>

            <a class="alinharPosicaoComecarAgora" href="#cadastro" aria-label="Começar agora">
                <span class="botaoComecarAgora">COMEÇAR AGORA</span>
                <span class="botaoRedondoComecarAgora" aria-hidden="true">
                <span class="setaComecarAgora"></span>
            </span>
            </a>

            <p class="tipografiaFraseInicial">Pense no presente e mude seu futuro, <br>
                ajudando empresas a construir<span class="tipografiaFraseInicialDestaque"> um mundo mais sustentável!</span></p>
        </div>

        <div class="heroVisual">
            <div class="divCarroselCelular">
                <img class="fundoVerde" src="Imagens/FundoVerdeCarrossel.svg" alt="">

                <img id="imagemCelular" class="celularInterativo"
                     src="Imagens/CelularInterativo1.svg" alt="Tela do aplicativo Aether" fetchpriority="high">

                <div class="divBotoes">
                    <button id="botaoAnterior" class="botaoEsquerdoCarrossel" type="button" aria-label="Tela anterior">
                        <img class="setaEsquerdaCarroselCelulares" src="Imagens/setaEsquerda.svg" alt="">
                    </button>
                    <button id="botaoProximo" class="botaoDireitoCarrossel" type="button" aria-label="Próxima tela">
                        <img class="setaDireitaCarroselCelulares" src="Imagens/setaDireita.svg" alt="">
                    </button>
                </div>
            </div>
        </div>

        <div class="Bola1" aria-hidden="true"></div>
        <div class="Bola2" aria-hidden="true"></div>
    </section>


    <!-- =========================================================
         SETOR 2 — QUEM SOMOS
         ========================================================= -->
    <section class="setor setorQuemSomos" id="quemSomos">

        <div class="Bola3" aria-hidden="true"></div>

        <div class="linhas">
            <span class="basePraTexto">grupo aether</span>
        </div>

        <h2 class="quemSomos">Quem <span class="somos">somos</span></h2>

        <div class="quemSomosConteudo">
            <div class="retanguloDescricao">
                <p>Integrantes do programa de Tecnologia do Ensino Médio do Instituto J&amp;F, apresentamos
                    <span class="aetherDestacado">aether</span>: um projeto voltado ao desenvolvimento e à
                    recomendação de alternativas e práticas mais sustentáveis para as operações de toda a JBS,
                    com o objetivo de promover a sustentabilidade ao futuro.</p>
            </div>

            <div class="carrosselAlunos" id="carrosselAlunos" role="group" aria-label="Fotos das turmas">

                <button type="button" class="retangulosRoxos" data-turma="0" aria-pressed="true">1º Médio</button>
                <button type="button" class="retangulosRoxos2" data-turma="1" aria-pressed="false">2º Médio</button>

                <div class="carrosselAlunosQuadro" aria-hidden="true"></div>

                <div class="carrosselAlunosFotos">
                    <div class="carrosselAlunosFoto visivel" role="img" aria-label="Foto do 1º Médio"></div>
                    <div class="carrosselAlunosFoto" role="img" aria-label="Foto do 2º Médio"></div>
                </div>

                <p class="carrosselAlunosLegenda" aria-live="polite">
                    <span class="carrosselAlunosLegendaTexto visivel">1º Médio</span>
                    <span class="carrosselAlunosLegendaTexto">2º Médio</span>
                </p>
            </div>
        </div>
    </section>


    <!-- =========================================================
         SETOR 3 — O QUE SÃO GASES DE EFEITO ESTUFA
         ========================================================= -->
    <section class="setor setorGee" id="gasesPoluentes">

        <div class="rotuloExplicacao">
            <span class="linhasExplicacao" aria-hidden="true"></span>
            <span class="explicacao">Explicação</span>
        </div>

        <h2 class="tituloOqueSao">O que são <span class="tituloGasesEf">Gases de Efeito Estufa?</span></h2>

        <p class="descricaoGasesPoluentes">Gases poluentes são substâncias químicas ou partículas suspensas que, quando
            presentes na atmosfera em concentrações anormais, tornam o ar tóxico ou nocivo. Eles degradam a qualidade do
            ar, causam impactos severos à saúde humana (como problemas respiratórios) e afetam o meio ambiente. Os gases
            são separados em 3 escopos de emissão, que são:</p>

        <div class="gradeEscopos">
            <article class="cardEscopo1">
                <img class="imagemCaminhao" src="Imagens/caminhaoCard.svg" alt="" loading="lazy">
                <h3 class="escopo1Texto">ESCOPO 1</h3>
                <p class="emissoesDiretastexto">EMISSÕES DIRETAS</p>
                <ul>
                    <li class="listaCard1Linha1">Queima de diesel em geradores;</li>
                    <li class="listaCard1Linha2">Frotas de veículos próprios;</li>
                    <li class="listaCard1Linha3">Processos químicos industriais.</li>
                </ul>
            </article>

            <article class="cardEscopo2">
                <img class="imagemRelampago" src="Imagens/relampagoCard.svg" alt="" loading="lazy">
                <h3 class="escopo2Texto">ESCOPO 2</h3>
                <p class="energiaAdequadatexto">ENERGIA ADQUIRIDA</p>
                <ul>
                    <li class="listaCard2Linha1">Consumo de energia elétrica;</li>
                    <li class="listaCard2Linha2">Compra de vapor industrial;</li>
                    <li class="listaCard2Linha3">Sistemas de climatização.</li>
                </ul>
            </article>

            <article class="cardEscopo3">
                <img class="imagemFogo" src="Imagens/fogoCard.svg" alt="" loading="lazy">
                <h3 class="escopo3Texto">ESCOPO 3</h3>
                <p class="cadeiaDeValortexto">CADEIA DE VALOR</p>
                <ul id="LISTACADEIADEVALOR">
                    <li class="listaCard3Linha1">Transporte e distribuição;</li>
                    <li class="listaCard3Linha2">Viagens corporativas;</li>
                    <li class="listaCard3Linha3">Uso dos produtos vendidos.</li>
                </ul>
            </article>
        </div>

        <div class="Bola4" aria-hidden="true"></div>
    </section>


    <!-- =========================================================
         SETOR 4 — CARROSSEL DE GASES A SUBSTITUIR
         ========================================================= -->
    <section class="setor setorGases">

        <h2 class="tituloSobreSubstituicaoDeGases">Mas... quais <span class="gasesDestacado">gases</span>
            substituímos?</h2>

        <p class="descricaoSubstituicao">Então, o Aether sugere à sua empresa um substituto verde para gases
            poluentes de Escopo 1, 2 e 3, utilizado em cada setor de sua empresa. Essas categorias incluem os gases:</p>

        <!-- ===== CARROSSEL DE GASES ===== -->
        <div class="carrosselGases" id="carrosselGases" role="region" aria-roledescription="carrossel" aria-label="Gases que procuramos substituir">

            <button id="gasesAnterior" class="carrosselGasesSeta carrosselGasesSetaEsq" type="button" data-cor="verde" aria-label="Gás anterior">
                <svg viewBox="-20 -20 40 40" fill="none" stroke-width="10.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M5.55 -14.78L-7.64 -2.17C-8.92 -0.94 -8.92 1.12 -7.62 2.34L5.55 14.78"/>
                </svg>
            </button>

            <article class="cardGas" data-cor="verde" data-pos="ativo" aria-label="Monóxido de Carbono">
                <h3 class="cardGasRisco">RISCO GEE: ALTO</h3>
                <p class="cardGasEscopo">ESCOPO 3</p>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>Gás / substância</dt>
                        <dd>Monóxido de Carbono<br>CO</dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Fator de emissão</dt>
                        <dd class="cardGasNumero">≈ 2</dd>
                        <dd class="cardGasUnidade">tCO<sub>2</sub>e / t CO</dd>
                    </div>
                </dl>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>GHGs emitidos</dt>
                        <dd class="cardGasNumero">CO CO<sub>2</sub></dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Potencial climático</dt>
                        <dd class="cardGasPotencial">Moderado (GWP indireto)</dd>
                        <dd class="cardGasAplicacao">Embalagem MAP e combustão incompleta</dd>
                    </div>
                </dl>
            </article>

            <article class="cardGas" data-cor="roxo" aria-label="Óxido Nitroso">
                <h3 class="cardGasRisco">RISCO GEE: CRÍTICO</h3>
                <p class="cardGasEscopo">ESCOPO 3</p>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>Gás / substância</dt>
                        <dd>Óxido Nitroso<br>N<sub>2</sub>O</dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Fator de emissão</dt>
                        <dd class="cardGasNumero">273</dd>
                        <dd class="cardGasUnidade">tCO<sub>2</sub>e / t N<sub>2</sub>O</dd>
                    </div>
                </dl>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>GHGs emitidos</dt>
                        <dd class="cardGasNumero">N<sub>2</sub>O</dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Potencial climático</dt>
                        <dd class="cardGasPotencial">Alto (GWP100 = 273)</dd>
                        <dd class="cardGasAplicacao">Fertilizantes sintéticos nitrogenados</dd>
                    </div>
                </dl>
            </article>

            <article class="cardGas" data-cor="roxo" aria-label="Hexafluoreto de Enxofre">
                <h3 class="cardGasRisco">RISCO GEE: CRÍTICO</h3>
                <p class="cardGasEscopo">ESCOPO 3</p>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>Gás / substância</dt>
                        <dd>Hexafluoreto de Enxofre<br>SF<sub>6</sub></dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Fator de emissão</dt>
                        <dd class="cardGasNumero">23.500</dd>
                        <dd class="cardGasUnidade">tCO<sub>2</sub>e / t SF<sub>6</sub></dd>
                    </div>
                </dl>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>GHGs emitidos</dt>
                        <dd class="cardGasNumero">SF<sub>6</sub></dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Potencial climático</dt>
                        <dd class="cardGasPotencial">Extremo (GWP100 = 23.500)</dd>
                        <dd class="cardGasAplicacao">Disjuntores e subestações elétricas</dd>
                    </div>
                </dl>
            </article>

            <article class="cardGas" data-cor="roxo" aria-label="HFC-23, Trifluorometano">
                <h3 class="cardGasRisco">RISCO GEE: CRÍTICO</h3>
                <p class="cardGasEscopo">ESCOPO 3</p>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>Gás / substância</dt>
                        <dd>HFC-23 (Trifluorometano)<br>CHF<sub>3</sub></dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Fator de emissão</dt>
                        <dd class="cardGasNumero">14.800</dd>
                        <dd class="cardGasUnidade">tCO<sub>2</sub>e / t HFC-23</dd>
                    </div>
                </dl>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>GHGs emitidos</dt>
                        <dd class="cardGasNumero">CHF<sub>3</sub></dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Potencial climático</dt>
                        <dd class="cardGasPotencial">Extremo (GWP100 = 14.800)</dd>
                        <dd class="cardGasAplicacao">Supressão de incêndio e ultracongelamento</dd>
                    </div>
                </dl>
            </article>

            <article class="cardGas" data-cor="verde" aria-label="HFC-134a, Tetrafluoroetano">
                <h3 class="cardGasRisco">RISCO GEE: ALTO</h3>
                <p class="cardGasEscopo">ESCOPO 3</p>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>Gás / substância</dt>
                        <dd>HFC-134a<br>(Tetrafluoroetano)</dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Fator de emissão</dt>
                        <dd class="cardGasNumero">1.430</dd>
                        <dd class="cardGasUnidade">tCO<sub>2</sub>e / t HFC-134a</dd>
                    </div>
                </dl>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>GHGs emitidos</dt>
                        <dd class="cardGasNumero">HFC</dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Potencial climático</dt>
                        <dd class="cardGasPotencial">Alto (GWP100 = 1.430)</dd>
                        <dd class="cardGasAplicacao">Refrigeração comercial e ar-condicionado veicular</dd>
                    </div>
                </dl>
            </article>

            <article class="cardGas" data-cor="verde" aria-label="Óxidos de Nitrogênio">
                <h3 class="cardGasRisco">RISCO GEE: ALTO</h3>
                <p class="cardGasEscopo">ESCOPO 3</p>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>Gás / substância</dt>
                        <dd>Óxidos de Nitrogênio<br>NO<sub>x</sub> / NO<sub>2</sub></dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Fator de emissão</dt>
                        <dd class="cardGasNumero">298</dd>
                        <dd class="cardGasUnidade">tCO<sub>2</sub>e / t NO<sub>2</sub> equiv. (GWP indireto)</dd>
                    </div>
                </dl>
                <dl class="cardGasColuna">
                    <div class="cardGasItem">
                        <dt>GHGs emitidos</dt>
                        <dd class="cardGasNumero">NO<sub>x</sub> NO<sub>2</sub></dd>
                    </div>
                    <div class="cardGasItem">
                        <dt>Potencial climático</dt>
                        <dd class="cardGasPotencial">Alto — precursor de ozônio</dd>
                        <dd class="cardGasAplicacao">Frota diesel e caldeiras industriais</dd>
                    </div>
                </dl>
            </article>

            <button id="gasesProximo" class="carrosselGasesSeta carrosselGasesSetaDir" type="button" data-cor="roxo" aria-label="Próximo gás">
                <svg viewBox="-20 -20 40 40" fill="none" stroke-width="10.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M-5.55 -14.78L7.64 -2.17C8.92 -0.94 8.92 1.12 7.62 2.34L-5.55 14.78"/>
                </svg>
            </button>

        </div>
        <!-- ===== FIM DO CARROSSEL DE GASES ===== -->
    </section>


    <!-- =========================================================
         SETOR 5 — NOSSA SOLUÇÃO + ANÁLISE DE DADOS
         ========================================================= -->
    <section class="setor setorSolucao" id="nossaSolucao">

        <div class="Bola5" aria-hidden="true"></div>
        <div class="Bola6" aria-hidden="true"></div>

        <div class="retanguloSetor5">

            <h2 class="tituloConhecaASolucao">Conheça nossa Solução</h2>
            <p class="tituloFuncionalidadeDoAPP">Funcionalidades do APP</p>

            <div class="solucaoConteudo">
                <img class="imagemCelularAeko" src="Imagens/CelularesAeko.svg"
                     alt="Telas do aplicativo Aether com o chatbot Aeko" loading="lazy">

                <div class="agrupandoGrupo">
                    <details name="menuFuncionalidades">
                        <summary class="tipografiaMenuAbreEFechaTitulo">Leitor de PDF</summary>
                        <p class="tipografiaMenuAbreEFechaConteudo">Um leitor de PDF em que será possível carregar
                            relatórios de emissões de gases de efeito estufa (por unidade), que recomendará
                            possíveis alternativas.</p>
                    </details>

                    <details name="menuFuncionalidades">
                        <summary class="tipografiaMenuAbreEFechaTitulo">Consultor Manual</summary>
                        <p class="tipografiaMenuAbreEFechaConteudo">No plano Pulse, o consultor manual será uma
                            forma de adicionar informações especiais. Já no plano Horizon, será o principal método
                            de entrada de informações.</p>
                    </details>

                    <details name="menuFuncionalidades">
                        <summary class="tipografiaMenuAbreEFechaTitulo"><span>Calculadora de CO<sub>2</sub></span></summary>
                        <p class="tipografiaMenuAbreEFechaConteudo">Essa funcionalidade terá como principal função
                            calcular quantas toneladas de CO<sub>2</sub> será possível reduzir a partir da troca sugerida.</p>
                    </details>

                    <details name="menuFuncionalidades">
                        <summary class="tipografiaMenuAbreEFechaTitulo">Calculadora de ROI</summary>
                        <p class="tipografiaMenuAbreEFechaConteudo">Calculará o ROI (retorno sobre o investimento) a
                            partir da troca do gás poluente pelo gás sugerido, utilizando como base o desgaste de
                            máquina, a taxa WACC etc.</p>
                    </details>

                    <details name="menuFuncionalidades">
                        <summary class="tipografiaMenuAbreEFechaTitulo">Cadastro</summary>
                        <p class="tipografiaMenuAbreEFechaConteudo">Será possível o cadastro do usuário
                            pelo nosso aplicativo.</p>
                    </details>

                    <details name="menuFuncionalidades">
                        <summary class="tipografiaMenuAbreEFechaTitulo">ChatBot Aeko</summary>
                        <p class="tipografiaMenuAbreEFechaConteudo">O chatbot Aeko estará sempre disponível para
                            tirar suas dúvidas sobre gases (verdes e poluentes), sustentabilidade, fórmulas,
                            abreviações etc.</p>
                    </details>
                </div>
            </div>

            <div class="divTituloAnaliseDeDados">
                <h2 class="tituloAnalise">Análise de <span class="tituloDadosDestacado">Dados</span></h2>
                <p class="textoAnaliseDados">
                    Nossa empresa traz a melhor solução para a sua em 3 etapas
                    principais. Analisamos dados e relatórios a partir de diversos critérios
                    para sugerir as melhores alternativas renováveis aos gases poluentes.
                </p>
            </div>

            <div class="secaoCards">
                <article class="cardDados card1Dados">
                    <span class="marcadorEtapa marcadorEtapa1" aria-hidden="true"></span>
                    <span class="tipografiaCardsEtapa">1ª Etapa</span>
                    <h3 class="tipografiaCardsTitulos">Entrada de Dados</h3>
                    <p class="tipografiaCardsConteudo" id="textoEtapa1">A primeira etapa é onde armazenamos os dados, seja por
                        meio da leitura de relatórios, seja de modo manual pelo usuário.</p>
                    <button class="saibaMais" type="button" aria-expanded="false" aria-controls="textoEtapa1" aria-label="Saiba mais: Entrada de Dados">
                        <svg class="seta-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                            <path d="M12 5V19M12 19L5 12M12 19L19 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </button>
                </article>

                <article class="cardDados card2Dados">
                    <span class="marcadorEtapa marcadorEtapa2" aria-hidden="true"></span>
                    <span class="tipografiaCardsEtapa">2ª Etapa</span>
                    <h3 class="tipografiaCardsTitulos">Leitura de Dados</h3>
                    <p class="tipografiaCardsConteudo" id="textoEtapa2">Nessa etapa será feito o processamento e a leitura dos
                        dados, para que possam ser transformados em relatórios com dados-chave para sua empresa.</p>
                    <button class="saibaMais" type="button" aria-expanded="false" aria-controls="textoEtapa2" aria-label="Saiba mais: Leitura de Dados">
                        <svg class="seta-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                            <path d="M12 5V19M12 19L5 12M12 19L19 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </button>
                </article>

                <article class="cardDados card3Dados">
                    <span class="marcadorEtapa marcadorEtapa3" aria-hidden="true"></span>
                    <span class="tipografiaCardsEtapa">3ª Etapa</span>
                    <h3 class="tipografiaCardsTitulos">Saída de Dados</h3>
                    <p class="tipografiaCardsConteudo" id="textoEtapa3">Os dados lidos viram relatórios-chave, que indicam sua
                        categoria no Selo Clima Paraná, o retorno do investimento em 5 anos e quantas toneladas
                        de CO<sub>2</sub> a troca verde evita.</p>
                    <button class="saibaMais" type="button" aria-expanded="false" aria-controls="textoEtapa3" aria-label="Saiba mais: Saída de Dados">
                        <svg class="seta-svg" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                            <path d="M12 5V19M12 19L5 12M12 19L19 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </button>
                </article>
            </div>
        </div>
    </section>


    <!-- =========================================================
         SETOR 6 — PLANOS
         ========================================================= -->
    <section class="setor setorPlanos" id="planos">

        <div class="Bola7" aria-hidden="true"></div>
        <div class="Bola8" aria-hidden="true"></div>

        <div class="linhasPlanos">
            <span class="basePraTexto">Planos</span>
        </div>

        <h2 class="tituloPlanos">Planos</h2>

        <div class="divPlanos">

            <!-- Alternador mensal / trimestral (começa no trimestral) -->
            <div class="alternadorPeriodo">
                <span class="rotuloPeriodo" data-periodo="mensal">Plano mensal</span>

                <button id="botaoAlternarPeriodo" class="interruptorPeriodo" type="button"
                        role="switch" aria-checked="true" aria-label="Alternar entre plano mensal e trimestral">
                    <span class="bolinhaInterruptor"></span>
                </button>

                <span class="rotuloPeriodo rotuloPeriodoAtivo" data-periodo="trimestral">Plano trimestral</span>
            </div>

            <div class="gradePlanos">

                <!-- Plano Pulse (destacado no plano mensal) -->
                <article class="cardPlano cardPlanoPulse" data-periodo="mensal">
                    <h3 class="nomePlano">Aether Pulse</h3>
                    <p class="subtituloPlano">For Industries</p>
                    <p class="precoPlano">
                        <span class="valorPlano">R$3.300</span><span class="periodoPlano">/por mês</span>
                    </p>
                    <a class="botaoAssinar" href="#cadastro">Assine agora <span class="setaAssinar" aria-hidden="true">&rarr;</span></a>
                    <ul class="listaBeneficios">
                        <li>Quantidade de cadastro limitada a 10 funcionários</li>
                        <li>Quantidade de analistas e administradores limitada a 2</li>
                        <li>Acompanhamento mensal</li>
                    </ul>
                </article>

                <!-- Plano Horizon (destacado no plano trimestral) -->
                <article class="cardPlano cardPlanoHorizon cardPlanoAtivo" data-periodo="trimestral">
                    <h3 class="nomePlano">Aether Horizon</h3>
                    <p class="subtituloPlano">For Bigger Industries</p>
                    <p class="precoPlano">
                        <span class="valorPlano">R$9.400</span><span class="periodoPlano">/a cada 3 meses</span>
                    </p>
                    <a class="botaoAssinar botaoAssinarRoxo" href="#cadastro">Assine agora <span class="setaAssinar" aria-hidden="true">&rarr;</span></a>
                    <ul class="listaBeneficios">
                        <li>Tudo do plano Aether Pulse</li>
                        <li>Quantidade de cadastro de funcionários ilimitada</li>
                        <li>Acompanhamento Trimestral</li>
                    </ul>
                </article>

                <div class="tabelaPlanos" data-periodo="trimestral">

                    <h3 class="tabelaPlanosTitulo" id="tituloTabelaPlanos">Compare os planos</h3>
                    <p class="tabelaPlanosSubtitulo">O Horizon inclui tudo do Pulse e mais</p>

                    <div class="tabelaPlanosCorpo">
                        <div class="tabelaPlanosDestaque" aria-hidden="true"></div>

                        <table class="tabelaPlanosGrade" aria-labelledby="tituloTabelaPlanos">
                            <colgroup>
                                <col class="colLabelTabela">
                                <col class="colValorTabela">
                                <col class="colValorTabela">
                            </colgroup>
                            <thead>
                            <tr>
                                <th scope="col"><span class="tabelaPlanosSomenteLeitor">Recurso</span></th>
                                <th scope="col"><span class="pilulaTabela pilulaPulse">Pulse</span></th>
                                <th scope="col"><span class="pilulaTabela pilulaHorizon">Horizon</span></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">Custo por mês</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">R$3.300</span><span class="valorNegrito" aria-hidden="true">R$3.300</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">R$3.133</span><span class="valorNegrito" aria-hidden="true">R$3.133</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Funcionários</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">Até 10</span><span class="valorNegrito" aria-hidden="true">Até 10</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">Ilimitado</span><span class="valorNegrito" aria-hidden="true">Ilimitado</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Gerentes</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">Até 2</span><span class="valorNegrito" aria-hidden="true">Até 2</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">Ilimitados</span><span class="valorNegrito" aria-hidden="true">Ilimitados</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Acompanhamento</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">Mensal</span><span class="valorNegrito" aria-hidden="true">Mensal</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">Trimestral</span><span class="valorNegrito" aria-hidden="true">Trimestral</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Troca simulada</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">Até 3</span><span class="valorNegrito" aria-hidden="true">Até 3</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">Ilimitada</span><span class="valorNegrito" aria-hidden="true">Ilimitada</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Plano de melhoria (IA)</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">Básico</span><span class="valorNegrito" aria-hidden="true">Básico</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">Completo</span><span class="valorNegrito" aria-hidden="true">Completo</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Alertas no app</th>
                                <td class="colunaPulse"><svg class="iconeTabela" viewBox="0 0 17 17" role="img" aria-label="Não incluído"><circle cx="8.5" cy="8.5" r="8.5" fill="#E5586F"/><path d="M5.3 5.3L11.7 11.7M11.7 5.3L5.3 11.7" fill="none" stroke="#fff" stroke-width="1.7" stroke-linecap="round"/></svg></td>
                                <td class="colunaHorizon"><svg class="iconeTabela" viewBox="0 0 17 17" role="img" aria-label="Incluído"><circle cx="8.5" cy="8.5" r="8.5" fill="#2A9170"/><path d="M4.5 8.9L7.2 11.6L13 5.6" fill="none" stroke="#fff" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg></td>
                            </tr>
                            <tr>
                                <th scope="row">Metas de redução</th>
                                <td class="colunaPulse"><svg class="iconeTabela" viewBox="0 0 17 17" role="img" aria-label="Não incluído"><circle cx="8.5" cy="8.5" r="8.5" fill="#E5586F"/><path d="M5.3 5.3L11.7 11.7M11.7 5.3L5.3 11.7" fill="none" stroke="#fff" stroke-width="1.7" stroke-linecap="round"/></svg></td>
                                <td class="colunaHorizon"><svg class="iconeTabela" viewBox="0 0 17 17" role="img" aria-label="Incluído"><circle cx="8.5" cy="8.5" r="8.5" fill="#2A9170"/><path d="M4.5 8.9L7.2 11.6L13 5.6" fill="none" stroke="#fff" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg></td>
                            </tr>
                            <tr>
                                <th scope="row">Permissões</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">Padrão</span><span class="valorNegrito" aria-hidden="true">Padrão</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">Por usuário</span><span class="valorNegrito" aria-hidden="true">Por usuário</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Relatórios</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">PDF</span><span class="valorNegrito" aria-hidden="true">PDF</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">PDF e Excel</span><span class="valorNegrito" aria-hidden="true">PDF e Excel</span></span></td>
                            </tr>
                            <tr>
                                <th scope="row">Suporte no app</th>
                                <td class="colunaPulse"><span class="valorTabela"><span class="valorNormal">Até 48 h</span><span class="valorNegrito" aria-hidden="true">Até 48 h</span></span></td>
                                <td class="colunaHorizon"><span class="valorTabela"><span class="valorNormal">Até 8 h</span><span class="valorNegrito" aria-hidden="true">Até 8 h</span></span></td>
                            </tr>
                            </tbody>
                        </table>
                    </div>

                    <p class="tabelaPlanosNota">Custo mensal: Horizon R$9.400 por trimestre ÷ 3.</p>
                </div>

            </div>
        </div>
    </section>


    <!-- =========================================================
         SETOR 7 — CADASTRO / LOGIN
         ========================================================= -->
    <section class="setor setorCadastro" id="cadastro">

        <div class="Bola9" aria-hidden="true"></div>
        <div class="Bola10" aria-hidden="true"></div>

        <div class="linhasCadastro">
            <span class="basePraTexto">Cadastro</span>
        </div>

        <div class="cadastroConteudo">

            <div class="marcaDaguaCadastro" id="Aethercadastro">
                <img src="Imagens/AekoLogoEmBaixo.svg" id="LogoCadastro" alt="" loading="lazy">
            </div>

            <div class="cardCadastroLogin">
                <form id="formularioEmpresarial" class="formularioEmpresarial" action="${pageContext.request.contextPath}/empresa" method="post" accept-charset="UTF-8">

                    <img class="aekoCadastro" src="Imagens/Aeko.svg" alt="" loading="lazy">

                    <h2 class="tituloCadastro">Cadastro empresarial</h2>

                    <!-- Empresa -->
                    <div class="campoFormulario">
                        <label class="rotuloCampoOculto" for="empresaNome">Nome da empresa</label>
                        <input class="entradaEmpresarial" type="text" id="empresaNome" name="empresas"
                               placeholder="Nome da empresa" autocomplete="organization"
                               value="<c:out value='${requestScope.empresas}' />" required>
                    </div>

                    <!-- Tamanho -->
                    <div class="dropdownPersonalizado">
                        <span class="dropdownRotulo" id="rotuloTamanho">tamanho</span>
                        <div class="dropdownCaixa">
                            <button class="dropdownBotao" type="button" aria-haspopup="listbox" aria-expanded="false"
                                    aria-labelledby="rotuloTamanho textoTamanho">
                                <span class="dropdownTexto" id="textoTamanho">Selecionar</span>
                                <span class="dropdownSeta" aria-hidden="true"></span>
                            </button>
                            <ul class="dropdownLista" role="listbox" aria-labelledby="rotuloTamanho">
                                <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="grande" data-selecionado>Grande</li>
                                <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="media">Média</li>
                                <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="pequena">Pequena</li>
                            </ul>
                            <input type="hidden" id="empresaTamanho" name="tamanhos" value="">
                        </div>
                    </div>

                    <!-- CNPJ -->
                    <div class="campoFormulario">
                        <label class="rotuloCampoOculto" for="empresaCnpj">CNPJ</label>
                        <input class="entradaEmpresarial" type="text" id="empresaCnpj" name="cnpj"
                               placeholder="00.000.000/0001-91" inputmode="numeric" autocomplete="off" required>
                    </div>

                    <!-- E-mail -->
                    <div class="campoFormulario">
                        <label class="rotuloCampoOculto" for="empresaEmail">E-mail</label>
                        <input class="entradaEmpresarial" type="email" id="empresaEmail" name="email"
                               placeholder="e-mail" autocomplete="email" required>
                    </div>

                    <!-- Plano -->
                    <div class="dropdownPersonalizado">
                        <span class="dropdownRotulo" id="rotuloPlano">Plano</span>
                        <div class="dropdownCaixa">
                            <button class="dropdownBotao" type="button" aria-haspopup="listbox" aria-expanded="false"
                                    aria-labelledby="rotuloPlano textoPlano">
                                <span class="dropdownTexto" id="textoPlano">Selecionar</span>
                                <span class="dropdownSeta" aria-hidden="true"></span>
                            </button>
                            <ul class="dropdownLista" role="listbox" aria-labelledby="rotuloPlano">
                                <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="pulse">Aether Pulse</li>
                                <li class="dropdownOpcao" role="option" tabindex="-1" data-valor="horizon">Aether Horizon</li>
                            </ul>
                            <input type="hidden" id="empresaPlano" name="plano" value="">
                        </div>
                    </div>

                    <c:if test="${not empty requestScope.erro}">
                        <p class="mensagemFormulario mensagemErro"
                           role="alert">
                            <c:out value="${requestScope.erro}"/>
                        </p>
                    </c:if>

                    <c:if test="${not empty requestScope.sucesso}">
                        <div class="mensagemFormulario mensagemSucesso"
                             role="status">
                            <p><c:out value="${requestScope.sucesso}"/></p>

                            <c:if test="${not empty requestScope.companyId}">
                                <p>
                                    Código da empresa:
                                    <c:out value="${requestScope.companyId}"/>
                                </p>
                            </c:if>
                        </div>
                    </c:if>

                    <button class="botaoEnviarFormulario" type="submit">Cadastrar</button>

                    <p class="rodapeFormulario">Já possui uma conta?
                        <a href="Login.jsp" class="linkTrocaAba" data-destino="l">clique aqui</a>
                    </p>
                </form>
            </div>
        </div>
    </section>

</main>


<!-- =========================================================
     SETOR 8 — RODAPÉ
     ========================================================= -->
<footer class="rodapeAether">
    <div class="rodapeAetherConteudo">

        <div class="rodapeAetherMarca">
            <a href="#inicio" aria-label="Aether, voltar ao início">
                <img class="rodapeAetherLogo" src="Imagens/LogoFooter.svg" alt="Aether" loading="lazy">
            </a>
            <p class="rodapeAetherDescricao">
                A Aether é uma empresa dedicada a acelerar a transição energética global.
                Indicamos a transformação do legado de emissões em energia limpa por meio de
                inovação, tecnologia de ponta e soluções de alto impacto, movendo o amanhã de
                forma sustentável para as próximas gerações.
            </p>
        </div>

        <nav class="rodapeAetherNav" aria-label="Navegação do rodapé">
            <ul>
                <li><a href="#inicio">Início</a></li>
                <li><a href="#quemSomos">Quem Somos</a></li>
                <li><a href="#nossaSolucao">Nossa Solução</a></li>
                <li><a href="#planos">Planos</a></li>
                <li><a href="Contact.jsp">Contatos</a></li>
            </ul>
        </nav>

        <section class="rodapeAetherColuna" aria-labelledby="rodapeAetherRecursos">
            <h2 id="rodapeAetherRecursos">Recursos</h2>
            <ul>
                <li><a href="https://www.instagram.com/aethergases/" target="_blank" rel="noopener noreferrer">Instagram</a></li>
            </ul>
        </section>

        <section class="rodapeAetherColuna" aria-labelledby="rodapeAetherEmpresa">
            <h2 id="rodapeAetherEmpresa">Empresa</h2>
            <ul>
                <li><a href="#quemSomos">Nossa equipe</a></li>
                <li><a href="Contact.jsp">Contato</a></li>
            </ul>
        </section>

    </div>
</footer>

<script src="Script.js"></script>
<script src="MenuMobile.js" defer></script>

</body>
</html>