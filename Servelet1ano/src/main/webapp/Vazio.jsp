<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    Estado vazio da tabela — Figma: TELA CRUDV2 - SEM FUNCIONÁRIOS (nó 4938:8858).
    Deve ser incluído DENTRO de <section class="cartao-tabela">.
    Parâmetro: destaque = trecho final em roxo (ex.: "nenhum funcionário cadastrado.").

    Coordenadas do SVG relativas ao ponto (656, 306.16) do frame:
      nuvem      = Ellipse 24/25/26 + Rectangle 101 (422 x 240.5), contorno interno #D8DAE3
      olhos      = 44 x 66.9 em x=774 e x=915, y=474
      bochechas  = 34 x 23 em x=746 e x=953, y=534
      boca       = Rectangle 100 (36.75 x 51.14) em 847, 534
      exclamação = barra inclinada (1051..1106, 310..409) + ponto 40 x 39 em 1032, 417
--%>
<div class="vazio">
    <svg class="vazio-mascote" viewBox="0 0 506 310" width="506" height="310" aria-hidden="true">
        <defs>
            <!-- Contorno interno de 8px: a nuvem branca é a mesma forma "erodida" em 8px -->
            <filter id="vazioRecuo" x="0" y="0" width="100%" height="100%">
                <feMorphology in="SourceGraphic" operator="erode" radius="8"/>
            </filter>
            <g id="vazioNuvem">
                <ellipse cx="349.34" cy="242.35" rx="72.66"  ry="67.24"/>
                <ellipse cx="211.00" cy="167.36" rx="132.75" ry="98.28"/>
                <ellipse cx="88.03"  cy="228.13" rx="88.03"  ry="81.47"/>
                <rect x="83.84" y="244.94" width="268.29" height="64.66"/>
            </g>
        </defs>

        <!-- Nuvem: forma cheia na cor do contorno + a mesma forma recuada em branco -->
        <use href="#vazioNuvem" fill="#D8DAE3"/>
        <use href="#vazioNuvem" fill="#FFF" filter="url(#vazioRecuo)"/>

        <g fill="#D8DAE3">
            <!-- Olhos -->
            <rect x="118.25" y="167.84" width="44" height="66.9" rx="22"/>
            <rect x="259.00" y="167.84" width="44" height="66.9" rx="22"/>
            <!-- Bochechas -->
            <ellipse cx="107.00" cy="239.34" rx="17" ry="11.5"/>
            <ellipse cx="315.00" cy="239.34" rx="17" ry="11.5"/>
            <!-- Boca -->
            <rect x="190.96" y="228.07" width="36.75" height="51.14" rx="18.37"/>
            <!-- Ponto de exclamação -->
            <line x1="438" y1="15.84" x2="407" y2="90.84" stroke="#D8DAE3" stroke-width="22" stroke-linecap="round"/>
            <ellipse cx="396" cy="130.34" rx="20" ry="19.5"/>
        </g>
    </svg>

    <p class="vazio-texto">
        <span class="roxo">Ops!</span> Parece que você ainda não tem
        <span class="roxo"><c:out value="${param.destaque}"/></span>
    </p>
</div>
