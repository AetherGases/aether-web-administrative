(() => {
    const cabecalho = document.querySelector('.cabecalho');
    const botao = document.getElementById('botaoMenuMobile');
    const menu = document.getElementById('menuPrincipal');
    if (!cabecalho || !botao || !menu) return;

    const consultaDesktop = window.matchMedia('(min-width: 1025px)');

    const definirEstado = (aberto) => {
        cabecalho.classList.toggle('menuAberto', aberto);
        botao.setAttribute('aria-expanded', String(aberto));
        botao.setAttribute('aria-label', aberto ? 'Fechar menu' : 'Abrir menu');
    };

    const estaAberto = () => cabecalho.classList.contains('menuAberto');

    botao.addEventListener('click', () => definirEstado(!estaAberto()));

    menu.addEventListener('click', (evento) => {
        if (evento.target.closest('a')) definirEstado(false);
    });

    document.addEventListener('click', (evento) => {
        if (estaAberto() && !cabecalho.contains(evento.target)) definirEstado(false);
    });

    document.addEventListener('keydown', (evento) => {
        if (evento.key === 'Escape' && estaAberto()) {
            definirEstado(false);
            botao.focus();
        }
    });

    consultaDesktop.addEventListener('change', (evento) => {
        if (evento.matches) definirEstado(false);
    });
})();