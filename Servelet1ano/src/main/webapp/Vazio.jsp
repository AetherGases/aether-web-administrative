<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="vazio">
    <svg class="vazio-mascote" viewBox="0 0 506 310" width="506" height="310" aria-hidden="true">
        <defs>
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
