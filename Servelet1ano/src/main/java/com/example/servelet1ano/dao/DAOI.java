package com.example.servelet1ano.dao;

import java.util.List;

/**
 * Interface que define o contrato padrao de um DAO
 * @param <T> O tipo da entidade (ex: Employees, Units, Companies)
 */

public interface DAOI<T> {



    /**
     * Busca todos os registros ativos
     * @return List<T> com os registros encontrados
     */
    List<T> searchAll();



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
     * Desativa um registro pelo id (is_active = false)
     * @param id Valor unico do registro
     * @return true se foi desativado e false caso tenha dado erro
     */
    boolean delete(int id);
}
