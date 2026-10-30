<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Paginação (Figma: Group 427321410 em x=783, y=914) e texto "Mostrando 10 por página". --%>
<p class="contagem">Mostrando 10 por página</p>

<button type="button" class="pagina-seta pagina-anterior" aria-label="Página anterior">
    <svg viewBox="0 0 19 19" aria-hidden="true">
        <path d="M13 2.5L5 9.5l8 7" fill="none" stroke="#FFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
</button>

<span class="pagina-atual" aria-current="page">1</span>

<button type="button" class="pagina-seta pagina-proxima" aria-label="Próxima página">
    <svg viewBox="0 0 19 19" aria-hidden="true">
        <path d="M6 2.5l8 7-8 7" fill="none" stroke="#FFF" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
</button>
