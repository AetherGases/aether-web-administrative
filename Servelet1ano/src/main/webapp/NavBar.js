(function () {
    const aside = document.querySelector('.sidebar');
    if (!aside) return;

    const ind = aside.querySelector('.navIndicador');
    if (!ind) return;

    const link = aside.querySelector('.sidebar-menu a.ativo');
    const alvo = link ? link.closest('li') : null;
    if (!alvo) return;

    function medir() {
        const a = aside.getBoundingClientRect();
        const r = alvo.getBoundingClientRect();
        return { top: r.top - a.top, left: r.left - a.left, w: r.width, h: r.height };
    }

    function posicionar(m) {
        ind.style.width = m.w + 'px';
        ind.style.height = m.h + 'px';
        ind.style.transform = 'translate(' + m.left + 'px,' + m.top + 'px)';
    }

    function iniciar() {
        ind.style.transition = 'none';

        const atual = medir();
        const salvo = sessionStorage.getItem('navTop');

        // estado final já aplicado
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
            posicionar(medir());
        });
    }

    if (document.fonts && document.fonts.ready) {
        document.fonts.ready.then(iniciar);
    } else {
        iniciar();
    }
})();