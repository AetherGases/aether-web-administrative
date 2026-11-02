package com.example.servelet1ano.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validação compartilhada dos IDs recebidos para desativação em lote. */
public final class Selection {
    private static final int LIMITE = 100;

    /**
     * Impede a instanciação da classe utilitária.
     */

    private Selection() {}

    /**
     * Lê e valida os identificadores enviados para desativação em lote.
     * Rejeita parâmetros inesperados, ações incorretas, IDs repetidos,
     * valores inválidos e seleções superiores a 100 registros.
     *
     * @param request A requisição com os identificadores selecionados
     * @return Uma lista imutável com os identificadores validados
     * @throws IllegalArgumentException Se a seleção for inválida
     */

    public static List<Integer> parse(HttpServletRequest request) {
        if (!request.getParameterMap().keySet().stream()
                .allMatch(Set.of("acao", "csrfToken", "ids")::contains)) {
            throw new IllegalArgumentException("Campos inesperados no formulário.");
        }
        String[] acoes = request.getParameterValues("acao");
        String[] ids = request.getParameterValues("ids");
        if (acoes == null || acoes.length != 1 || !"excluirLote".equals(acoes[0])
                || ids == null || ids.length == 0 || ids.length > LIMITE) {
            throw new IllegalArgumentException("Selecione entre 1 e 100 registros.");
        }
        List<Integer> result = new ArrayList<>(ids.length);
        Set<Integer> unicos = new HashSet<>();
        for (String raw : ids) {
            if (raw == null || !raw.matches("[1-9][0-9]{0,9}")) {
                throw new IllegalArgumentException("ID inválido.");
            }
            final int id;
            try { id = Integer.parseInt(raw); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("ID inválido."); }
            if (!unicos.add(id)) {
                throw new IllegalArgumentException("IDs duplicados.");
            }
            result.add(id);
        }
        return List.copyOf(result);
    }
}
