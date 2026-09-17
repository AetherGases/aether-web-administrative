package com.example.servelet1ano.dao;

import java.util.List;

/**
 * Interface generica que define o contrato padrao de um DAO
 * @param <T> O tipo da entidade
 * @param <F> O tipo do filtro usado na busca
 */
public interface DAOI<T, F> {

    /**
     * Busca todos os registros ativos que baterem com o filtro
     * @param filtro Objeto com os campos opcionais para filtrar a busca
     * @return List<T> com os registros encontrados
     */
    List<T> searchAll(F filtro);

    /**
     * Busca um registro ativo pelo id
     * @param id Numero unico do registro
     * @return O registro encontrado, ou null se nao existir ou estiver desativado
     */
    T searchById(int id);

    /**
     * Cadastra um novo registro
     * @param entity O registro que sera cadastrado
     * @return true se foi registrado e false caso tenha dado erro
     */
    boolean register(T entity);

    /**
     * Atualiza um registro existente pelo id
     * @param entity Os valores para atualizar
     * @param id O valor unico do registro que quer atualizar
     * @return true se foi atualizado e false caso tenha dado erro
     */
    boolean update(T entity, int id);

    /**
     * Desativa um registro pelo id (is_active = false) -- nao apaga a linha
     * @param id Valor unico do registro
     * @return true se foi desativado e false caso tenha dado erro
     */
    boolean delete(int id);
}