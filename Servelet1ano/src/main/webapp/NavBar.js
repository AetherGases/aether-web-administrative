(function () {
    const aside = document.querySelector('.sidebar');
    if (!aside) return;

    // ---------- MENU RECOLHIDO ----------
    const botao = aside.querySelector('.sidebar-toggle');
    if (botao) {
        const consultaDesktop = window.matchMedia('(min-width: 1280px)');

        const definirEstado = function (aberto) {
            aside.classList.toggle('menu-aberto', aberto);
            botao.setAttribute('aria-expanded', String(aberto));
            botao.setAttribute('aria-label', aberto ? 'Fechar menu' : 'Abrir menu');
        };

        botao.addEventListener('click', function () {
            definirEstado(!aside.classList.contains('menu-aberto'));
        });

        document.addEventListener('keydown', function (evento) {
            if (evento.key === 'Escape' && aside.classList.contains('menu-aberto')) {
                definirEstado(false);
                botao.focus();
            }
        });

        consultaDesktop.addEventListener('change', function (evento) {
            if (evento.matches) definirEstado(false);
        });
    }

    // ---------- INDICADOR DO MENU ----------
    const ind = aside.querySelector('.navIndicador');
    if (!ind) return;

    const link = aside.querySelector('.sidebar-menu a.ativo');
    const alvo = link ? link.closest('li') : null;
    if (!alvo) return;

    function medir() {
        return { top: alvo.offsetTop, left: alvo.offsetLeft, w: alvo.offsetWidth, h: alvo.offsetHeight };
    }

    function posicionar(m) {
        ind.style.width = m.w + 'px';
        ind.style.height = m.h + 'px';
        ind.style.transform = 'translate(' + m.left + 'px,' + m.top + 'px)';
    }

    function indicadorVisivel() {
        return window.getComputedStyle(ind).display !== 'none';
    }

    function iniciar() {
        if (!indicadorVisivel()) {
            window.addEventListener('resize', function () {
                if (indicadorVisivel()) posicionar(medir());
            });
            return;
        }

        ind.style.transition = 'none';

        const atual = medir();
        const salvo = sessionStorage.getItem('navTop');

        posicionar(atual);

        if (salvo !== null && parseFloat(salvo) !== atual.top) {
            ind.animate(
                [
                    { transform: 'translate(' + atual.left + 'px,' + parseFloat(salvo) + 'px)' },
                    { transform: 'translate(' + atual.left + 'px,' + atual.top + 'px)' }
                ],
                { duration: 500, easing: 'cubic-bezier(0.34, 1.4, 0.64, 1)' }
            );
        }

        sessionStorage.setItem('navTop', atual.top);

        window.addEventListener('resize', function () {
            if (indicadorVisivel()) posicionar(medir());
        });
    }

    if (document.fonts && document.fonts.ready) {
        document.fonts.ready.then(iniciar);
    } else {
        iniciar();
    }
})();
