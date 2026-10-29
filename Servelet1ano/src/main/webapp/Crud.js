/*
 * Crud.js — comportamento compartilhado das telas internas.
 *
 *  - [data-abrir="idDoOverlay"]  abre um overlay; se estiver dentro de uma <tr data-*>,
 *    copia os data-* da linha para os campos do overlay que tiverem [data-campo="nome"].
 *  - [data-fechar]               fecha o overlay em que está.
 *  - Clique fora do modal ou tecla Esc também fecham.
 *  - #pesquisa filtra as linhas da tabela pelo texto digitado.
 *  - #botaoSelecionarTodos marca/desmarca todos os checkboxes.
 */
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

    // Selecionar todos.
    const selecionarTodos = document.getElementById("botaoSelecionarTodos");
    if (selecionarTodos) {
        selecionarTodos.addEventListener("click", function () {
            const caixas = document.querySelectorAll(".tabela tbody tr:not([hidden]) .check");
            const marcar = Array.prototype.some.call(caixas, function (c) { return !c.checked; });
            caixas.forEach(function (c) { c.checked = marcar; });
        });
    }
})();
