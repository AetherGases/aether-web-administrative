/* ==========================================================================

   AETHER — script.js

   Módulos:

     1. carrosselCelulares()     — carrossel de telas do app

     2. carrosselCards()         — carrossel de gases

     3. menuFuncionalidades()    — acordeão exclusivo

     4. alternarPlanos()         — mensal / trimestral

     5. abasCadastroLogin()      — alternância entre os formulários

     6. validarFormularios()     — validação básica no cliente

   Todos os módulos verificam a existência dos elementos antes de agir,

   para que o arquivo possa ser reutilizado em páginas parciais.

   ========================================================================== */

document.addEventListener("DOMContentLoaded", function () {

    carrosselCelulares();

    carrosselCards();

    menuFuncionalidades();

    alternarPlanos();

    abasCadastroLogin();

    validarFormularios();

});


/* --------------------------------------------------------------------------

   1. CARROSSEL DE CELULARES

   Mantém a lógica original, com a correção dos caminhos (sem "WEB-INF/")

   e com verificação de existência dos elementos.

   -------------------------------------------------------------------------- */

function carrosselCelulares() {

    const celulares = [

        "Imagens/CelularInterativo1.svg",

        "Imagens/CelularInterativo2.svg"

    ];

    const imagemCelular = document.getElementById("imagemCelular");

    const botaoAnterior = document.getElementById("botaoAnterior");

    const botaoProximo = document.getElementById("botaoProximo");

    if (!imagemCelular || !botaoAnterior || !botaoProximo) {

        return;

    }

    let indiceAtual = 0;

    function mostrarTela(indice) {

        indiceAtual = (indice + celulares.length) % celulares.length;

        imagemCelular.setAttribute("src", celulares[indiceAtual]);

        imagemCelular.setAttribute(

            "alt",

            "Tela " + (indiceAtual + 1) + " de " + celulares.length + " do aplicativo Aether"

        );

    }

    botaoProximo.addEventListener("click", function () {

        mostrarTela(indiceAtual + 1);

    });

    botaoAnterior.addEventListener("click", function () {

        mostrarTela(indiceAtual - 1);

    });

    // Pré-carrega as demais telas para evitar piscada na primeira troca.

    celulares.forEach(function (caminho) {

        const previa = new Image();

        previa.src = caminho;

    });

}


/* --------------------------------------------------------------------------

   2. CARROSSEL DE CARDS DE GASES

   O HTML original já tinha os botões #btnAnterior / #btnProximo e o trilho

   #trilhoCarrossel, mas não havia nenhum JavaScript ligado a eles.

   -------------------------------------------------------------------------- */

function carrosselCards() {

    const trilho = document.getElementById("trilhoCarrossel");

    const janela = document.querySelector(".janelaCarrosselCards");

    const botaoAnterior = document.getElementById("btnAnterior");

    const botaoProximo = document.getElementById("btnProximo");

    if (!trilho || !janela || !botaoAnterior || !botaoProximo) {

        return;

    }

    let deslocamento = 0;

    function larguraDoPasso() {

        const card = trilho.querySelector(".cardItem");

        if (!card) {

            return 0;

        }

        const estilo = window.getComputedStyle(trilho);

        const espacamento = parseFloat(estilo.columnGap || estilo.gap || "0") || 0;

        return card.getBoundingClientRect().width + espacamento;

    }

    function deslocamentoMaximo() {

        return Math.max(0, trilho.scrollWidth - janela.clientWidth);

    }

    function aplicar() {

        const limite = deslocamentoMaximo();

        deslocamento = Math.min(Math.max(deslocamento, 0), limite);

        trilho.style.transform = "translateX(" + -deslocamento + "px)";

        botaoAnterior.disabled = deslocamento <= 0;

        botaoProximo.disabled = deslocamento >= limite - 1;

    }

    botaoProximo.addEventListener("click", function () {

        deslocamento += larguraDoPasso();

        aplicar();

    });

    botaoAnterior.addEventListener("click", function () {

        deslocamento -= larguraDoPasso();

        aplicar();

    });

    // Navegação por teclado quando o carrossel estiver em foco.

    janela.setAttribute("tabindex", "0");

    janela.addEventListener("keydown", function (evento) {

        if (evento.key === "ArrowRight") {

            evento.preventDefault();

            deslocamento += larguraDoPasso();

            aplicar();

        } else if (evento.key === "ArrowLeft") {

            evento.preventDefault();

            deslocamento -= larguraDoPasso();

            aplicar();

        }

    });

    window.addEventListener("resize", aplicar);

    aplicar();

}


/* --------------------------------------------------------------------------

   3. ACORDEÃO DE FUNCIONALIDADES

   O atributo name em <details> ainda não é suportado por todos os

   navegadores; este módulo garante que apenas um item fique aberto.

   -------------------------------------------------------------------------- */

function menuFuncionalidades() {

    const itens = document.querySelectorAll('.agrupandoGrupo details[name="menuFuncionalidades"]');

    if (itens.length === 0) {

        return;

    }

    itens.forEach(function (item) {

        item.addEventListener("toggle", function () {

            if (!item.open) {

                return;

            }

            itens.forEach(function (outro) {

                if (outro !== item) {

                    outro.open = false;

                }

            });

        });

    });

}


/* --------------------------------------------------------------------------

   4. ALTERNÂNCIA DE PLANOS (MENSAL / TRIMESTRAL)

   Os valores ficam em atributos data-mensal e data-trimestral no HTML,

   para que a equipe comercial possa ajustá-los sem mexer no JavaScript.

   -------------------------------------------------------------------------- */

function alternarPlanos() {

    const interruptor = document.getElementById("botaoAlternarPeriodo");

    const rotulos = document.querySelectorAll(".rotuloPeriodo");

    const valores = document.querySelectorAll(".valorPlano, .periodoPlano");

    if (!interruptor || valores.length === 0) {

        return;

    }

    function aplicarPeriodo(periodo) {

        valores.forEach(function (elemento) {

            const texto = elemento.getAttribute("data-" + periodo);

            if (texto !== null) {

                elemento.textContent = texto;

            }

        });

        rotulos.forEach(function (rotulo) {

            rotulo.classList.toggle(

                "rotuloPeriodoAtivo",

                rotulo.getAttribute("data-periodo") === periodo

            );

        });

        interruptor.setAttribute("aria-checked", periodo === "trimestral" ? "true" : "false");

    }

    interruptor.addEventListener("click", function () {

        const estaTrimestral = interruptor.getAttribute("aria-checked") === "true";

        aplicarPeriodo(estaTrimestral ? "mensal" : "trimestral");

    });

    rotulos.forEach(function (rotulo) {

        rotulo.addEventListener("click", function () {

            aplicarPeriodo(rotulo.getAttribute("data-periodo"));

        });

    });

    aplicarPeriodo("mensal");

}


/* --------------------------------------------------------------------------

   5. ABAS CADASTRO / LOGIN

   -------------------------------------------------------------------------- */

function abasCadastroLogin() {

    const abaLogin = document.getElementById("abaLogin");

    const abaCadastrar = document.getElementById("abaCadastrar");

    const formularioLogin = document.getElementById("formularioLogin");

    const formularioCadastro = document.getElementById("formularioCadastro");

    if (!abaLogin || !abaCadastrar || !formularioLogin || !formularioCadastro) {

        return;

    }

    function mostrar(destino) {

        const ehLogin = destino === "login";

        formularioLogin.hidden = !ehLogin;

        formularioCadastro.hidden = ehLogin;

        formularioLogin.classList.toggle("formularioOculto", !ehLogin);

        formularioCadastro.classList.toggle("formularioOculto", ehLogin);

        abaLogin.classList.toggle("abaCadastroAtiva", ehLogin);

        abaCadastrar.classList.toggle("abaCadastroAtiva", !ehLogin);

        abaLogin.setAttribute("aria-selected", String(ehLogin));

        abaCadastrar.setAttribute("aria-selected", String(!ehLogin));

    }

    abaLogin.addEventListener("click", function () {

        mostrar("login");

    });

    abaCadastrar.addEventListener("click", function () {

        mostrar("cadastro");

    });

    document.querySelectorAll(".linkTrocaAba").forEach(function (link) {

        link.addEventListener("click", function () {

            mostrar(link.getAttribute("data-destino"));

        });

    });

    mostrar("cadastro");

}


/* --------------------------------------------------------------------------

   6. VALIDAÇÃO DOS FORMULÁRIOS

   Validação apenas no cliente, para orientar o usuário. Toda verificação

   definitiva (inclusive de senha) precisa ser refeita no servidor.

   -------------------------------------------------------------------------- */

function validarFormularios() {

    const formularioCadastro = document.getElementById("formularioCadastro");

    const formularioLogin = document.getElementById("formularioLogin");

    function escrever(elemento, texto, sucesso) {

        if (!elemento) {

            return;

        }

        elemento.textContent = texto;

        elemento.classList.toggle("mensagemFormularioSucesso", Boolean(sucesso));

    }

    if (formularioCadastro) {

        formularioCadastro.addEventListener("submit", function (evento) {

            evento.preventDefault();

            const mensagem = document.getElementById("mensagemCadastro");

            const email = document.getElementById("emailCadastro");

            const senha = document.getElementById("senhaCadastro");

            const confirmacao = document.getElementById("confirmeSenha");

            const plano = document.getElementById("planoEscolhido");

            if (!email.value.trim() || !email.checkValidity()) {

                escrever(mensagem, "Informe um e-mail válido.", false);

                email.focus();

                return;

            }

            if (senha.value.length < 8) {

                escrever(mensagem, "A senha deve ter ao menos 8 caracteres.", false);

                senha.focus();

                return;

            }

            if (senha.value !== confirmacao.value) {

                escrever(mensagem, "As senhas não coincidem.", false);

                confirmacao.focus();

                return;

            }

            if (!plano.value) {

                escrever(mensagem, "Selecione um plano.", false);

                plano.focus();

                return;

            }

            escrever(mensagem, "Cadastro validado. Falta conectar o envio ao servidor.", true);

        });

    }

    if (formularioLogin) {

        formularioLogin.addEventListener("submit", function (evento) {

            evento.preventDefault();

            const mensagem = document.getElementById("mensagemLogin");

            const email = document.getElementById("emailLogin");

            const senha = document.getElementById("senhaLogin");

            if (!email.value.trim() || !email.checkValidity()) {

                escrever(mensagem, "Informe um e-mail válido.", false);

                email.focus();

                return;

            }

            if (!senha.value) {

                escrever(mensagem, "Informe sua senha.", false);

                senha.focus();

                return;

            }

            escrever(mensagem, "Dados validados. Falta conectar a autenticação ao servidor.", true);

        });

    }

}
