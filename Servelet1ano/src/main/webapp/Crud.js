(function () {
    "use strict";

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

    // Reabre o formulário quando o servlet devolve erro de validação.
    document.querySelectorAll(".overlay[data-abrir-ao-carregar='true']").forEach(abrir);

    // Pesquisa local nas linhas carregadas.
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


    // Exclusão em lote
    const formLote = document.getElementById("formExcluirLote");
    const botaoLote = document.getElementById("botaoExcluirLote");
    const quantidade = document.getElementById("quantidadeSelecionada");

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
            const selecionadas = caixasSelecionadas();

            if (selecionadas.length === 0 ||
                selecionadas.length > 100) {
                evento.preventDefault();
                return;
            }

            if (!window.confirm(
                "Desativar os " + selecionadas.length +
                " registros selecionados? Esta ação pode ser " +
                "recusada se houver vínculos ou registros protegidos."
            )) {
                evento.preventDefault();
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
        });
    }

    atualizarSelecao();

    // Selecionar todos.
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
