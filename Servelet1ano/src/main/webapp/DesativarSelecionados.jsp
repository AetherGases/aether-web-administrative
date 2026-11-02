<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- ---------- OVERLAY DESATIVAR SELECIONADOS ---------- --%>
<div class="overlay" id="overlayDesativarSelecionados" aria-hidden="true">
    <div class="modal modal-excluir" role="dialog" aria-modal="true" aria-labelledby="tituloDesativarSelecionados">
        <div class="modal-topo">
            <img src="${ctx}/Imagens/IconeDesativar.svg" alt="">
            <h2 id="tituloDesativarSelecionados">Desativar</h2>
            <button type="button" class="modal-fechar" data-fechar aria-label="Fechar">
                <svg viewBox="0 0 28 28" aria-hidden="true"><path d="M5 5l18 18M23 5L5 23" stroke="#FFF" stroke-width="3" stroke-linecap="round"/></svg>
            </button>
        </div>

        <div class="corpo-excluir">
            <img src="${ctx}/Imagens/AlertaExcluir.svg" alt="">
            <p>Tem certeza que deseja desativar <span id="resumoSelecionados">os registros selecionados</span>? Esta ação pode ser recusada se houver vínculos ou registros protegidos.</p>
        </div>

        <div class="botoes-excluir">
            <button type="button" class="botao-cancelar" data-fechar>Cancelar</button>
            <button type="button" class="botao-desativar" id="confirmarDesativarSelecionados">Desativar</button>
        </div>
    </div>
</div>
