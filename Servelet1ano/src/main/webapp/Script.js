/* ==========================================================================
   AETHER — script.js

   Módulos:
     1. carrosselCelulares()     — carrossel de telas do app
     2. carrosselGases()         — carrossel de cards de gases
     3. menuFuncionalidades()    — acordeão exclusivo
     4. alternarPlanos()         — mensal / trimestral
     5. dropdownsCadastro()      — dropdowns personalizados do cadastro
     6. validarFormularios()     — validação básica no cliente
     7. cardsEtapas()            — cards da análise de dados (saiba mais)
     8. animarCardsEscopo()      — animação dos cards de escopo
     9. carrosselAlunos()        — fotos 1º / 2º Médio

   Todos os módulos verificam a existência dos elementos antes de agir,
   e cada um é iniciado UMA única vez, pela função iniciar() no final.
   ========================================================================== */


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

    // Pré-carrega as imagens
    celulares.forEach(function (caminho) {
        const previa = new Image();
        previa.src = caminho;
    });
}


/* --------------------------------------------------------------------------
   2. CARROSSEL DE CARDS DE GASES
   -------------------------------------------------------------------------- */

function carrosselGases() {
    const palco = document.getElementById("carrosselGases");
    if (!palco) return;

    const cards = Array.prototype.slice.call(palco.querySelectorAll(".cardGas"));
    const botaoAnterior = document.getElementById("gasesAnterior");
    const botaoProximo = document.getElementById("gasesProximo");

    if (cards.length === 0 || !botaoAnterior || !botaoProximo) return;

    if (palco.dataset.iniciado === "true") return;
    palco.dataset.iniciado = "true";

    const total = cards.length;
    let ativo = 0;

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
            const pos = posicao((i - ativo + total) % total);
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
F
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
}


/* --------------------------------------------------------------------------
   3. ACORDEÃO DE FUNCIONALIDADES
   -------------------------------------------------------------------------- */

function menuFuncionalidades() {
    const DURACAO = 600;
    const EASING = "ease-in-out";
    const DESLOCAMENTO_CONTEUDO = 12;

    const itens = Array.from(document.querySelectorAll(".agrupandoGrupo details"));

    if (itens.length === 0) {
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


/* --------------------------------------------------------------------------
   4. ALTERNÂNCIA DE PLANOS (MENSAL / TRIMESTRAL)
   -------------------------------------------------------------------------- */

function alternarPlanos() {
    const interruptor = document.getElementById("botaoAlternarPeriodo");
    const rotulos = document.querySelectorAll(".rotuloPeriodo");
    const cards = document.querySelectorAll(".cardPlano");
    const tabela = document.querySelector(".tabelaPlanos");

    if (!interruptor || cards.length === 0) {
        return;
    }

    if (interruptor.getAttribute("data-iniciado") === "true") {
        return;
    }
    interruptor.setAttribute("data-iniciado", "true");

    function aplicarPeriodo(periodo) {
        cards.forEach(function (card) {
            card.classList.toggle("cardPlanoAtivo", card.getAttribute("data-periodo") === periodo);
        });

        rotulos.forEach(function (rotulo) {
            rotulo.classList.toggle("rotuloPeriodoAtivo", rotulo.getAttribute("data-periodo") === periodo);
        });

        if (tabela) {
            tabela.setAttribute("data-periodo", periodo);
        }

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

    aplicarPeriodo(interruptor.getAttribute("aria-checked") === "true" ? "trimestral" : "mensal");
}


/* --------------------------------------------------------------------------
   5. DROPDOWNS PERSONALIZADOS DO CADASTRO
   -------------------------------------------------------------------------- */

function dropdownsCadastro() {
    if (dropdownsCadastro.iniciado) {
        return;
    }
    dropdownsCadastro.iniciado = true;

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
        const alvo = evento.target;
        const opcao = alvo.closest ? alvo.closest(".dropdownOpcao") : null;
        const botao = alvo.closest ? alvo.closest(".dropdownBotao") : null;

        if (evento.key === "Escape") {
            const ddAtual = alvo.closest ? alvo.closest(".dropdownPersonalizado") : null;
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


/* --------------------------------------------------------------------------
   6. VALIDAÇÃO DO FORMULÁRIO DE CADASTRO EMPRESARIAL
   -------------------------------------------------------------------------- */

function validarFormularios() {
    const formulario = document.getElementById("formularioEmpresarial");
    if (!formulario) {
        return;
    }

    if (formulario.dataset.iniciado === "true") {
        return;
    }
    formulario.dataset.iniciado = "true";

    const mensagem = document.getElementById("mensagemEmpresarial");

    function escrever(texto, sucesso) {
        if (!mensagem) {
            return;
        }
        mensagem.textContent = texto;
        mensagem.classList.toggle("mensagemFormularioSucesso", Boolean(sucesso));
    }

    formulario.addEventListener("submit", function (evento) {
        evento.preventDefault();

        const empresa = document.getElementById("empresaTipo");
        const tamanho = document.getElementById("empresaTamanho");
        const cnpj = document.getElementById("empresaCnpj");
        const email = document.getElementById("empresaEmail");
        const plano = document.getElementById("empresaPlano");

        if (!empresa.value) {
            escrever("Selecione a empresa.", false);
            return;
        }

        if (!tamanho.value) {
            escrever("Selecione o tamanho da empresa.", false);
            return;
        }

        const cnpjNumeros = cnpj.value.replace(/\D/g, "");
        if (cnpjNumeros.length !== 14) {
            escrever("Informe um CNPJ válido (14 dígitos).", false);
            cnpj.focus();
            return;
        }

        if (!email.value.trim() || !email.checkValidity()) {
            escrever("Informe um e-mail válido.", false);
            email.focus();
            return;
        }

        if (!plano.value) {
            escrever("Selecione um plano.", false);
            return;
        }

        escrever("Cadastro validado. Falta conectar o envio ao servidor.", true);
    });
}


/* --------------------------------------------------------------------------
   7. CARDS DAS ETAPAS (ANÁLISE DE DADOS)
   -------------------------------------------------------------------------- */

function cardsEtapas() {
    const ALTURA_FECHADO = 250;
    const ALTURA_MIN_ABERTO = 432;

    const cards = document.querySelectorAll(".cardDados");
    if (cards.length === 0) return;

    cards.forEach(function (card) {
        const botao = card.querySelector(".saibaMais");
        if (!botao) return;

        if (botao.dataset.iniciado === "true") return;
        botao.dataset.iniciado = "true";

        botao.addEventListener("click", function () {
            const abrir = botao.getAttribute("aria-expanded") !== "true";

            if (abrir) {
                card.classList.add("aberto");
                card.style.height = "auto";
                const alvo = Math.max(card.offsetHeight, ALTURA_MIN_ABERTO);
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
}


/* --------------------------------------------------------------------------
   8. CARDS DE ESCOPO
   -------------------------------------------------------------------------- */

function animarCardsEscopo() {
    document
        .querySelectorAll(".cardEscopo1, .cardEscopo2, .cardEscopo3")
        .forEach(function (card) {
            card.classList.add("ativo");
        });
}


/* --------------------------------------------------------------------------
   9. CARROSSEL DE ALUNOS (1º Médio / 2º Médio)
   -------------------------------------------------------------------------- */

function carrosselAlunos() {
    const FOTOS = [
        "Imagens/1anoFoto.png",
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


/* ==========================================================================
   INICIALIZAÇÃO
   ========================================================================== */

function iniciar() {
    [
        carrosselCelulares,
        carrosselGases,
        menuFuncionalidades,
        alternarPlanos,
        dropdownsCadastro,
        validarFormularios,
        cardsEtapas,
        carrosselAlunos
    ].forEach(function (modulo) {
        try {
            modulo();
        } catch (erro) {
            console.error("Erro no módulo " + modulo.name + ":", erro);
        }
    });
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", iniciar);
} else {
    iniciar();
}

window.addEventListener("load", animarCardsEscopo);