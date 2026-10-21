/* ==========================================================================

   AETHER — Script.js

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

   -------------------------------------------------------------------------- */

function carrosselCelulares() {
    const celulares = [
        "Imagens/CelularInterativo1.svg",
        "Imagens/CelularInterativo2.svg"
    ];

    const DURACAO = 500;
    const EASING = "ease-out";

    const ESCALA_TRAS = 0.9;
    const DESLOCAMENTO_TRAS = 45;

    const REDUCAO_POR_CAMADA = 0.1;
    const DESLOCAMENTO_EXTRA = 15;

    const PONTO_DE_TROCA = 0.35;

    const imagemInicial = document.getElementById("imagemCelular");
    const botaoAnterior = document.getElementById("botaoAnterior");
    const botaoProximo = document.getElementById("botaoProximo");

    if (!imagemInicial || !botaoAnterior || !botaoProximo) {
        return;
    }

    const container = imagemInicial.parentElement;

    if (container.dataset.carrosselIniciado === "true") {
        return;
    }
    container.dataset.carrosselIniciado = "true";

    if (getComputedStyle(container).position === "static") {
        container.style.position = "relative";
    }

    container.style.isolation = "isolate";

    const imagens = [];

    celulares.forEach(function (caminho, i) {
        let img;

        if (i === 0) {
            img = imagemInicial;
        } else {
            img = imagemInicial.cloneNode(false);
            img.removeAttribute("id");
            img.style.position = "absolute";
            img.style.margin = "0";
            img.style.maxWidth = "none";
            img.style.maxHeight = "none";
            container.appendChild(img);
        }

        img.setAttribute("src", caminho);
        img.setAttribute(
            "alt",
            "Tela " + (i + 1) + " de " + celulares.length + " do aplicativo Aether"
        );
        img.style.transformOrigin = "center bottom";
        img.style.willChange = "transform, opacity";
        img.style.pointerEvents = "none";

        imagens.push(img);
    });

    function sincronizarCopias() {
        imagens.forEach(function (img, i) {
            if (i === 0) {
                return;
            }
            img.style.left = imagemInicial.offsetLeft + "px";
            img.style.top = imagemInicial.offsetTop + "px";
            img.style.width = imagemInicial.offsetWidth + "px";
            img.style.height = imagemInicial.offsetHeight + "px";
        });
    }

    sincronizarCopias();
    imagemInicial.addEventListener("load", sincronizarCopias);
    window.addEventListener("resize", sincronizarCopias);

    if (typeof ResizeObserver !== "undefined") {
        new ResizeObserver(sincronizarCopias).observe(imagemInicial);
    }

    let indiceAtual = 0;
    let timerCamada = null;

    function posicaoNaPilha(i, atual) {
        const n = celulares.length;
        return (((i - atual) % n) + n) % n;
    }

    function aplicarTransform(img, pos) {
        if (pos === 0) {
            img.style.transform = "translateX(0) scale(1)";
            img.style.opacity = "1";
            return;
        }

        const camadasExtras = pos - 1;
        const escala = Math.max(ESCALA_TRAS - camadasExtras * REDUCAO_POR_CAMADA, 0.3);
        const deslocamento = DESLOCAMENTO_TRAS + camadasExtras * DESLOCAMENTO_EXTRA;

        img.style.transform =
            "translateX(" + deslocamento + "%) scale(" + escala + ")";
        img.style.opacity = pos === 1 ? "1" : "0";
    }

    function aplicarCamada(img, pos) {
        img.style.zIndex = String(celulares.length - pos);
    }

    function renderizar(comTransicao) {
        const transicao = comTransicao
            ? "transform " + DURACAO + "ms " + EASING + ", " +
            "opacity " + DURACAO + "ms " + EASING
            : "none";

        imagens.forEach(function (img, i) {
            img.style.transition = transicao;
            aplicarTransform(img, posicaoNaPilha(i, indiceAtual));
        });

        if (!comTransicao) {
            imagens.forEach(function (img, i) {
                aplicarCamada(img, posicaoNaPilha(i, indiceAtual));
            });
            return;
        }

        clearTimeout(timerCamada);
        timerCamada = setTimeout(function () {
            imagens.forEach(function (img, i) {
                aplicarCamada(img, posicaoNaPilha(i, indiceAtual));
            });
        }, DURACAO * PONTO_DE_TROCA);
    }

    botaoProximo.addEventListener("click", function () {
        indiceAtual += 1;
        renderizar(true);
    });

    botaoAnterior.addEventListener("click", function () {
        indiceAtual -= 1;
        renderizar(true);
    });

    renderizar(false);

    celulares.forEach(function (caminho) {
        const previa = new Image();
        previa.src = caminho;
    });
}


/* --------------------------------------------------------------------------

   2. CARROSSEL DE CARDS DE GASES

   -------------------------------------------------------------------------- */

(function () {
    var palco = document.getElementById("carrosselGases");
    if (!palco) return;

    var cards = Array.prototype.slice.call(palco.querySelectorAll(".cardGas"));
    var botaoAnterior = document.getElementById("gasesAnterior");
    var botaoProximo = document.getElementById("gasesProximo");
    var total = cards.length;
    var ativo = 0;

    function posicao(deslocamento) {
        if (deslocamento === 0) return "ativo";
        if (deslocamento === 1) return "dir";
        if (deslocamento === total - 1) return "esq";
        if (deslocamento === 2) return "dir2";
        if (deslocamento === total - 2) return "esq2";
        return "oculto";
    }

    function atualizar() {
        cards.forEach(function (card, i) {
            var pos = posicao((i - ativo + total) % total);
            card.dataset.pos = pos;
            card.setAttribute("aria-hidden", pos === "ativo" ? "false" : "true");
        });

        botaoAnterior.dataset.cor = cards[(ativo - 1 + total) % total].dataset.cor;
        botaoProximo.dataset.cor = cards[(ativo + 1) % total].dataset.cor;
    }

    function mover(passo) {
        ativo = (ativo + passo + total) % total;
        atualizar();
    }

    botaoAnterior.addEventListener("click", function () { mover(-1); });
    botaoProximo.addEventListener("click", function () { mover(1); });

    // Clicar num card lateral também navega
    cards.forEach(function (card) {
        card.addEventListener("click", function () {
            if (card.dataset.pos === "esq") mover(-1);
            if (card.dataset.pos === "dir") mover(1);
        });
    });

    palco.addEventListener("keydown", function (e) {
        if (e.key === "ArrowLeft") mover(-1);
        if (e.key === "ArrowRight") mover(1);
    });

    atualizar();
})();

/* --------------------------------------------------------------------------

   3. ACORDEÃO DE FUNCIONALIDADES

   -------------------------------------------------------------------------- */

function menuFuncionalidades() {
    const DURACAO = 600;                 // ms
    const EASING = "ease-in-out";
    const DESLOCAMENTO_CONTEUDO = 12;    // px

    const itens = Array.from(document.querySelectorAll(".agrupandoGrupo details"));

    if (itens.length === 0) {
        console.warn("menuFuncionalidades: nenhum <details> encontrado dentro de .agrupandoGrupo");
        return;
    }

    const grupo = itens[0].closest(".agrupandoGrupo");

    if (grupo.dataset.menuIniciado === "true") {
        return;
    }
    grupo.dataset.menuIniciado = "true";

    const reduzirMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const duracao = reduzirMovimento ? 0 : DURACAO;

    grupo.style.setProperty("--duracao", duracao + "ms");
    grupo.style.setProperty("--easing", EASING);

    const animacoes = new WeakMap();

    function conteudoDe(item) {
        return Array.from(item.children).filter(function (el) {
            return el.tagName !== "SUMMARY";
        });
    }

    function opacidadeAtual(item) {
        if (!item.open) {
            return 0;
        }
        const primeiro = conteudoDe(item)[0];
        return primeiro ? parseFloat(getComputedStyle(primeiro).opacity) : 1;
    }

    function alturaFechado(item) {
        const estavaAberto = item.open;
        item.open = false;
        const altura = item.offsetHeight;
        item.open = estavaAberto;
        return altura;
    }

    function limpar(item) {
        const lista = animacoes.get(item);
        if (lista) {
            lista.forEach(function (animacao) {
                animacao.onfinish = null;
                animacao.cancel();
            });
            animacoes.delete(item);
        }
        item.style.overflow = "";
        item.style.boxSizing = "";
    }

    function animar(item, alturaInicial, alturaFinal, opacidadeInicial, opacidadeFinal, aoTerminar) {
        item.style.overflow = "hidden";
        item.style.boxSizing = "border-box";

        const opcoes = { duration: duracao, easing: EASING, fill: "forwards" };

        const animacaoItem = item.animate(
            [{ height: alturaInicial + "px" }, { height: alturaFinal + "px" }],
            opcoes
        );

        const lista = [animacaoItem];

        conteudoDe(item).forEach(function (el) {
            lista.push(
                el.animate(
                    [
                        {
                            opacity: opacidadeInicial,
                            transform: "translateY(" + (-DESLOCAMENTO_CONTEUDO * (1 - opacidadeInicial)) + "px)"
                        },
                        {
                            opacity: opacidadeFinal,
                            transform: "translateY(" + (-DESLOCAMENTO_CONTEUDO * (1 - opacidadeFinal)) + "px)"
                        }
                    ],
                    opcoes
                )
            );
        });

        animacoes.set(item, lista);

        animacaoItem.onfinish = function () {
            limpar(item);
            if (aoTerminar) {
                aoTerminar();
            }
        };
    }

    function abrir(item) {
        const alturaInicial = item.offsetHeight;
        const opacidadeInicial = opacidadeAtual(item);

        limpar(item);
        item.classList.add("ativo");
        item.open = true;

        const alturaFinal = item.offsetHeight;

        animar(item, alturaInicial, alturaFinal, opacidadeInicial, 1, null);
    }

    function fechar(item) {
        const alturaInicial = item.offsetHeight;
        const opacidadeInicial = opacidadeAtual(item);

        limpar(item);
        item.classList.remove("ativo");

        const alturaFinal = alturaFechado(item);

        animar(item, alturaInicial, alturaFinal, opacidadeInicial, 0, function () {
            item.open = false;
        });
    }

    itens.forEach(function (item) {
        const resumo = item.querySelector("summary");

        item.removeAttribute("name");

        if (item.open) {
            item.classList.add("ativo");
        }

        resumo.addEventListener("click", function (evento) {
            evento.preventDefault();

            if (item.classList.contains("ativo")) {
                fechar(item);
                return;
            }

            itens.forEach(function (outro) {
                if (outro !== item && outro.classList.contains("ativo")) {
                    fechar(outro);
                }
            });

            abrir(item);
        });
    });
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", menuFuncionalidades);
} else {
    menuFuncionalidades();
}

/* --------------------------------------------------------------------------

   4. ALTERNÂNCIA DE PLANOS (MENSAL / TRIMESTRAL)

   -------------------------------------------------------------------------- */

function alternarPlanos() {

    const interruptor = document.getElementById("botaoAlternarPeriodo");

    const rotulos = document.querySelectorAll(".rotuloPeriodo");

    const cards = document.querySelectorAll(".cardPlano");

    if (!interruptor || cards.length === 0) {

        return;

    }
    if (interruptor.getAttribute("data-iniciado") === "true") {

        return;

    }

    interruptor.setAttribute("data-iniciado", "true");

    function aplicarPeriodo(periodo) {

        cards.forEach(function (card) {

            card.classList.toggle(

                "cardPlanoAtivo",

                card.getAttribute("data-periodo") === periodo

            );

        });

        // Destaca o rótulo do período escolhido
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

    interruptor.addEventListener("keydown", function (evento) {

        if (evento.key === "ArrowLeft") {

            aplicarPeriodo("mensal");

        } else if (evento.key === "ArrowRight") {

            aplicarPeriodo("trimestral");

        }

    });

    rotulos.forEach(function (rotulo) {

        rotulo.addEventListener("click", function () {

            aplicarPeriodo(rotulo.getAttribute("data-periodo"));

        });

    });

    aplicarPeriodo("mensal");

}

if (document.readyState === "loading") {

    document.addEventListener("DOMContentLoaded", alternarPlanos);

} else {

    alternarPlanos();

}

/* --------------------------------------------------------------------------

   5. ABAS CADASTRO / LOGIN

   -------------------------------------------------------------------------- */

function abasCadastroLogin() {

    if (abasCadastroLogin.iniciado) {
        return;
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", abasCadastroLogin);
        return;
    }

    abasCadastroLogin.iniciado = true;

    function todosDropdowns() {
        return document.querySelectorAll(".dropdownPersonalizado");
    }

    function opcoesVisiveis(dd) {
        return Array.from(dd.querySelectorAll(".dropdownOpcao:not([hidden])"));
    }

    function fechar(dd) {
        dd.classList.remove("dropdownAberto");
        dd.querySelector(".dropdownBotao").setAttribute("aria-expanded", "false");
    }

    function fecharTodos(excecao) {
        todosDropdowns().forEach(function (dd) {
            if (dd !== excecao) {
                fechar(dd);
            }
        });
    }

    function abrir(dd) {
        fecharTodos(dd);
        dd.classList.add("dropdownAberto");
        dd.querySelector(".dropdownBotao").setAttribute("aria-expanded", "true");
    }

    function selecionar(dd, opcao, devolverFoco) {
        const campo = dd.querySelector('input[type="hidden"]');
        const texto = dd.querySelector(".dropdownTexto");

        campo.value = opcao.getAttribute("data-valor");
        campo.dispatchEvent(new Event("change", { bubbles: true }));
        texto.textContent = opcao.textContent;
        dd.classList.add("dropdownPreenchido");

        dd.querySelectorAll(".dropdownOpcao").forEach(function (o) {
            const ehEscolhida = o === opcao;
            o.hidden = ehEscolhida;
            o.setAttribute("aria-selected", String(ehEscolhida));
        });

        fechar(dd);
        if (devolverFoco) {
            dd.querySelector(".dropdownBotao").focus();
        }
    }

    todosDropdowns().forEach(function (dd) {
        const inicial = dd.querySelector(".dropdownOpcao[data-selecionado]");
        if (inicial) {
            selecionar(dd, inicial, false);
        }
    });

    document.addEventListener("click", function (evento) {
        const opcao = evento.target.closest(".dropdownOpcao");
        if (opcao) {
            const ddOpcao = opcao.closest(".dropdownPersonalizado");
            if (ddOpcao) {
                selecionar(ddOpcao, opcao, true);
            }
            return;
        }

        const botao = evento.target.closest(".dropdownBotao");
        if (botao) {
            const dd = botao.closest(".dropdownPersonalizado");
            if (dd.classList.contains("dropdownAberto")) {
                fechar(dd);
            } else {
                abrir(dd);
            }
            return;
        }

        fecharTodos();
    });

    document.addEventListener("keydown", function (evento) {
        const opcao = evento.target.closest ? evento.target.closest(".dropdownOpcao") : null;
        const botao = evento.target.closest ? evento.target.closest(".dropdownBotao") : null;

        if (evento.key === "Escape") {
            const ddAtual = evento.target.closest ? evento.target.closest(".dropdownPersonalizado") : null;
            fecharTodos();
            if (ddAtual) {
                ddAtual.querySelector(".dropdownBotao").focus();
            }
            return;
        }

        if (botao && evento.key === "ArrowDown") {
            const dd = botao.closest(".dropdownPersonalizado");
            evento.preventDefault();
            abrir(dd);
            const primeira = opcoesVisiveis(dd)[0];
            if (primeira) {
                primeira.focus();
            }
            return;
        }

        if (opcao) {
            const dd = opcao.closest(".dropdownPersonalizado");
            const lista = opcoesVisiveis(dd);
            const indice = lista.indexOf(opcao);

            if (evento.key === "Enter" || evento.key === " ") {
                evento.preventDefault();
                selecionar(dd, opcao, true);
            } else if (evento.key === "ArrowDown") {
                evento.preventDefault();
                (lista[indice + 1] || lista[0]).focus();
            } else if (evento.key === "ArrowUp") {
                evento.preventDefault();
                (lista[indice - 1] || lista[lista.length - 1]).focus();
            }
        }
    });

}

abasCadastroLogin();


/* --------------------------------------------------------------------------

   6. VALIDAÇÃO DOS FORMULÁRIOS

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


/* --------------------------------------------------------------------------

   7. CARDS DAS ETAPAS (ANÁLISE DE DADOS)

   -------------------------------------------------------------------------- */

(function () {
    var ALTURA_FECHADO = 250;
    var ALTURA_MIN_ABERTO = 432;

    var cards = document.querySelectorAll(".cardDados");
    if (cards.length === 0) return;

    cards.forEach(function (card) {
        var botao = card.querySelector(".saibaMais");
        if (!botao) return;

        botao.addEventListener("click", function () {
            var abrir = botao.getAttribute("aria-expanded") !== "true";

            if (abrir) {
                card.classList.add("aberto");
                card.style.height = "auto";
                var alvo = Math.max(card.offsetHeight, ALTURA_MIN_ABERTO);
                card.style.height = ALTURA_FECHADO + "px";
                void card.offsetHeight;
                card.style.height = alvo + "px";
            } else {
                card.classList.remove("aberto");
                card.style.height = ALTURA_FECHADO + "px";
            }

            botao.setAttribute("aria-expanded", String(abrir));
        });
    });
})();

window.addEventListener("load", () => {
    document
        .querySelectorAll(".cardEscopo1, .cardEscopo2, .cardEscopo3")
        .forEach((card) => card.classList.add("ativo"));
});

function carrosselAlunos() {
    const FOTOS = [
        "Imagens/1anoFoto.jpg",
        "Imagens/2anoFoto.png"
    ];

    const raiz = document.getElementById("carrosselAlunos");

    if (!raiz) {
        return;
    }

    if (raiz.dataset.iniciado === "true") {
        return;
    }
    raiz.dataset.iniciado = "true";

    const botoes = Array.from(raiz.querySelectorAll("[data-turma]"));
    const fotos = Array.from(raiz.querySelectorAll(".carrosselAlunosFoto"));
    const legendas = Array.from(raiz.querySelectorAll(".carrosselAlunosLegendaTexto"));

    let atual = 0;

    fotos.forEach(function (foto, indice) {
        if (FOTOS[indice]) {
            foto.style.backgroundImage = 'url("' + FOTOS[indice] + '")';
        }
    });

    function mostrar(indice) {
        if (indice === atual) {
            return;
        }
        atual = indice;

        fotos.forEach(function (foto, i) {
            foto.classList.toggle("visivel", i === indice);
        });

        legendas.forEach(function (legenda, i) {
            legenda.classList.toggle("visivel", i === indice);
        });

        botoes.forEach(function (botao, i) {
            botao.setAttribute("aria-pressed", i === indice ? "true" : "false");
        });
    }

    botoes.forEach(function (botao) {
        botao.addEventListener("click", function () {
            mostrar(Number(botao.dataset.turma));
        });
    });
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", carrosselAlunos);
} else {
    carrosselAlunos();
}