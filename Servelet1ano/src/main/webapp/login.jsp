<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tela de Login do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">


    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

    <link rel="icon" href="Imagens/LogoAether.svg" type="image/svg">

    <link rel="stylesheet" href="global.css">
    <link rel="stylesheet" href="login.css">

</head>
<body>

    <div class="blur1"></div>
    <div class="blur2"></div>
    <div class="blur3"></div>
    <div class="blur4"></div>


    <div class="divLogo">
        <img class="logoCRUD" src="Imagens/LogoAethererdeEscuro.svg" alt="Logo Aether">
        <h1 class="logotipoAether">aether</h1>
        <p class="fraseLogo">Pense no presente e mude seu <span class="fraseFuturoLogo">futuro</span></p>
    </div>


    <main class="divLoginPrincipal">

        <div class="divCaixaForm">

            <img class="aekoCRUD" src="Imagens/AekoLogin.svg" alt="Aeko">

            <div class="retanguloLogin">
                <p class="loginPessoalTexto">Login pessoal</p>
            </div>

            <form action="${pageContext.request.contextPath}/login"
                  method="post">

                <label for="empresa"></label>


                <select class="campoEmpresa campoEmpresaEmpresa" name="empresas" id="empresa" required>
                    <option class="campoEmpresaEmpresa campoEmpresaEmpresa" value="" disabled selected hidden>Selecionar</option>
                    <option class="campoEmpresaEmpresa campoEmpresaEmpresa" value="aas">Aurora Alimentos Sustentáveis S.A.</option>
                    <option class="campoEmpresaEmpresa campoEmpresaEmpresa" value="hlil">Horizonte Logística Integrada Ltda.</option>
                    <option class="campoEmpresaEmpresa campoEmpresaEmpresa" value="vci">Vale Circular Indústria S.A.</option>
                </select>

                <input class="campoNome campoNomeNome"  type="text" id="nome" name="campoNome" placeholder="nome" required>
                <label for="nome"></label>

                <input class="campoEmail campoEmailEmail" type="email" id="email" name="campoEmail" placeholder="e-mail" required>
                <label for="email"></label>

                <input class="campoSenha campoSenhaSenha" type="password" id="senha" name="campoSenha" placeholder="senha" required>
                <label for="senha"></label>


                <button class="botaoEnvioRoxo tipografiaEnvio" type="submit" value="enviarFormulárioLogin">Entrar</button>

            </form>


        </div>
    </main>

</body>
</html>