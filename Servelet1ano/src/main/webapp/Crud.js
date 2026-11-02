(function () {
    "use strict";

    // ---------- ESCALA DA TELA ----------
    const LARGURA_BASE = 1440;
    const ALTURA_BASE = 1024;
    const ALTURA_MINIMA = 760;
    const LARGURA_MINIMA = 1280;

    function ajustarEscala() {
        const palcos = document.querySelectorAll(".tela, .perm-tela");
        const modais = document.querySelectorAll(".modal");
        const desktop = window.innerWidth >= LARGURA_MINIMA;
        let escala = 1;
        let compressao = 0;
        let altura = 0;

        if (desktop) {
            escala = Math.min(window.innerWidth / LARGURA_BASE, window.innerHeight / ALTURA_MINIMA);
            altura = window.innerHeight / escala;
            compressao = Math.min(1, Math.max(0, (ALTURA_BASE - altura) / (ALTURA_BASE - ALTURA_MINIMA)));
        }

        palcos.forEach(function (palco) {
            palco.style.zoom = desktop ? escala : "";
            palco.style.minHeight = desktop ? Math.floor(altura) + "px" : "";
            if (desktop) {
                palco.style.setProperty("--k", compressao);
            } else {
                palco.style.removeProperty("--k");
            }
        });

        modais.forEach(function (modal) {
            modal.style.zoom = desktop ? escala : "";
        });
    }

    ajustarEscala();
    window.addEventListener("resize", ajustarEscala);

    // ---------- OVERLAYS ----------
    function abrir(overlay) {
        if (!overlay) return;
        overlay.classList.add("ativo");
        overlay.setAttribute("aria-hidden", "false");
        const primeiro = overlay.querySelector(".modal-corpo input:not([readonly]), .modal-corpo select, .modal-corpo textarea")
            || overlay.querySelector(".botao-cancelar, .modal-fechar");
        if (primeiro) primeiro.focus({ preventScroll: true });
    }

    function fechar(overlay) {
        if (!overlay) return;
        overlay.classList.remove("ativo");
        overlay.setAttribute("aria-hidden", "true");
    }

    function preencher(overlay, linha) {
        if (!overlay || !linha) return;
        overlay.querySelectorAll("[data-campo]").forEach(function (campo) {
            const chave = campo.getAttribute("data-campo");
            const valor = linha.dataset[chave] !== undefined ? linha.dataset[chave] : "";
            if ("value" in campo) {
                campo.value = valor;
            } else {
                campo.textContent = valor;
            }
        });
    }

    document.addEventListener("click", function (evento) {
        const botaoAbrir = evento.target.closest("[data-abrir]");
        if (botaoAbrir) {
            const overlay = document.getElementById(botaoAbrir.getAttribute("data-abrir"));
            preencher(overlay, botaoAbrir.closest("tr[data-id]"));
            abrir(overlay);
            return;
        }

        const botaoFechar = evento.target.closest("[data-fechar]");
        if (botaoFechar) {
            fechar(botaoFechar.closest(".overlay"));
            return;
        }

        if (evento.target.classList && evento.target.classList.contains("overlay")) {
            fechar(evento.target);
        }
    });

    document.addEventListener("keydown", function (evento) {
        if (evento.key === "Escape") {
            document.querySelectorAll(".overlay.ativo").forEach(fechar);
        }
    });

    document.querySelectorAll(".overlay[data-abrir-ao-carregar='true']").forEach(abrir);

    // ---------- PESQUISA ----------
    const pesquisa = document.getElementById("pesquisa");
    const linhas = Array.prototype.slice.call(document.querySelectorAll(".tabela tbody tr[data-id]"));

    if (pesquisa) {
        pesquisa.addEventListener("input", function () {
            const termo = pesquisa.value.trim().toLowerCase();
            linhas.forEach(function (linha) {
                const mostrar = !termo || linha.textContent.toLowerCase().indexOf(termo) !== -1;
                linha.hidden = !mostrar;
            });
        });
    }

    // ---------- DESATIVAÇÃO EM LOTE ----------
    const formLote = document.getElementById("formExcluirLote");
    const botaoLote = document.getElementById("botaoExcluirLote");
    const quantidade = document.getElementById("quantidadeSelecionada");
    const overlayLote = document.getElementById("overlayDesativarSelecionados");
    const resumoLote = document.getElementById("resumoSelecionados");
    const confirmarLote = document.getElementById("confirmarDesativarSelecionados");

    function caixasSelecionadas() {
        return Array.from(
            document.querySelectorAll(
                ".tabela tbody tr[data-id] .check:checked"
            )
        );
    }

    function atualizarSelecao() {
        const total = caixasSelecionadas().length;

        if (quantidade) {
            quantidade.textContent = total;
        }

        if (botaoLote) {
            botaoLote.disabled = total === 0 || total > 100;

            botaoLote.title = total > 100
                ? "Selecione no máximo 100 registros por operação."
                : "";
        }
    }

    document.querySelectorAll(".tabela tbody .check")
        .forEach(function (caixa) {
            caixa.addEventListener("change", atualizarSelecao);
        });

    if (formLote) {
        formLote.addEventListener("submit", function (evento) {
            evento.preventDefault();

            const selecionadas = caixasSelecionadas();

            if (selecionadas.length === 0 ||
                selecionadas.length > 100 ||
                !overlayLote) {
                return;
            }

            if (resumoLote) {
                resumoLote.textContent = selecionadas.length === 1
                    ? "o registro selecionado"
                    : "os " + selecionadas.length + " registros selecionados";
            }

            abrir(overlayLote);
        });
    }

    if (formLote && confirmarLote) {
        confirmarLote.addEventListener("click", function () {
            const selecionadas = caixasSelecionadas();

            if (selecionadas.length === 0 ||
                selecionadas.length > 100) {
                fechar(overlayLote);
                return;
            }

            formLote.querySelectorAll('input[name="ids"]')
                .forEach(function (input) {
                    input.remove();
                });

            selecionadas.forEach(function (caixa) {
                const linha = caixa.closest("tr[data-id]");
                const input = document.createElement("input");

                input.type = "hidden";
                input.name = "ids";
                input.value = linha.dataset.id;

                formLote.appendChild(input);
            });

            formLote.submit();
        });
    }

    atualizarSelecao();

    // ---------- SELECIONAR TODOS ----------
    const selecionarTodos = document.getElementById("botaoSelecionarTodos");
    if (selecionarTodos) {
        selecionarTodos.addEventListener("click", function () {
            const caixas = document.querySelectorAll(".tabela tbody tr:not([hidden]) .check:not(:disabled)");
            const marcar = Array.prototype.some.call(caixas, function (c) { return !c.checked; });
            caixas.forEach(function (c) { c.checked = marcar; });
            atualizarSelecao();
        });
    }
})();
