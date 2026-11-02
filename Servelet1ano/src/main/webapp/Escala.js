(function () {
    "use strict";

    // ---------- ESCALA DA TELA ----------
    const LARGURA_MINIMA = 1024;
    const MARGEM_LATERAL = 80;
    const MARGEM_SUPERIOR = 90;
    const MARGEM_INFERIOR = 50;
    const ESCALA_MAXIMA = 2;
    const IGNORADOS = ".blur, .voltar";

    function medirConteudo(palco) {
        let esquerda = Infinity;
        let topo = Infinity;
        let direita = 0;
        let base = 0;
        const origem = palco.getBoundingClientRect();

        Array.prototype.forEach.call(palco.children, function (filho) {
            if (filho.matches(IGNORADOS)) return;
            const r = filho.getBoundingClientRect();
            if (r.width === 0 || r.height === 0) return;
            esquerda = Math.min(esquerda, r.left - origem.left);
            topo = Math.min(topo, r.top - origem.top);
            direita = Math.max(direita, r.right - origem.left);
            base = Math.max(base, r.bottom - origem.top);
        });

        if (esquerda === Infinity) return null;
        return { esquerda: esquerda, topo: topo, largura: direita - esquerda, altura: base - topo };
    }

    function reiniciar(palco) {
        palco.style.zoom = "";
        palco.style.width = "";
        palco.style.height = "";
        palco.style.removeProperty("--dx");
        palco.style.removeProperty("--dy");
    }

    function ajustarEscala() {
        const palco = document.querySelector(".tela, .telaErro");
        if (!palco) return;

        reiniciar(palco);
        if (window.innerWidth < LARGURA_MINIMA) return;

        const conteudo = medirConteudo(palco);
        if (!conteudo) return;

        const escala = Math.min(
            ESCALA_MAXIMA,
            window.innerWidth / (conteudo.largura + 2 * MARGEM_LATERAL),
            window.innerHeight / (conteudo.altura + MARGEM_SUPERIOR + MARGEM_INFERIOR)
        );
        const largura = window.innerWidth / escala;
        const altura = window.innerHeight / escala;
        const topoCentrado = Math.max((altura - conteudo.altura) / 2, MARGEM_SUPERIOR);

        palco.style.zoom = escala;
        palco.style.width = largura + "px";
        palco.style.height = altura + "px";
        palco.style.setProperty("--dx", (largura - conteudo.largura) / 2 - conteudo.esquerda + "px");
        palco.style.setProperty("--dy", topoCentrado - conteudo.topo + "px");
    }

    ajustarEscala();
    window.addEventListener("resize", ajustarEscala);
    window.addEventListener("load", ajustarEscala);
})();
