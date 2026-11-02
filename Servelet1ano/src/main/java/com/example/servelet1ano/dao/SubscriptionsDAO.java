package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.filter.SubscriptionsFilter;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Subscriptions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsDAO implements DAOI<Subscriptions, SubscriptionsFilter> {

    /**
     * Select base usado na busca geral e na busca por id
     * A juncao e left join pra nao sumir uma assinatura que esteja sem empresa
     */
    private static final String SELECT_SUBSCRIPTIONS =
            "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                    "c.id as idCompany, c.name as companyName " +
                    "from subscriptions s " +
                    "left join companies c on c.id = s.company_id ";

    /**
     * Metodo que busca as assinaturas ativas aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * @param filter Objeto com os campos opcionais para filtrar a busca (se for null, busca todas)
     * @return List<Subscriptions> com as assinaturas encontradas
     */
    @Override
    public List<Subscriptions> searchAll(SubscriptionsFilter filter){

        if (filter == null) {
            filter = new SubscriptionsFilter();
        }

        List<Subscriptions> subscriptions = new ArrayList<>();

        StringBuilder sql = new StringBuilder(SELECT_SUBSCRIPTIONS + "where s.is_active = true");

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and s.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and s.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        sql.append(" order by s.id");

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()){
                    subscriptions.add(mapSubscription(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar as assinaturas no banco de dados.", e);
        }

        return subscriptions;
    }

    /**
     * metodo para buscar por Id (somente ativas)
     * @param id Numero unico da assinatura
     * @return Retorna a assinatura encontrada, ou null se nao existir ou estiver desativada
     */
    @Override
    public Subscriptions searchById(int id){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        SELECT_SUBSCRIPTIONS + "where s.id = ? and s.is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()){
                    return mapSubscription(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar a assinatura de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar novas assinaturas no banco (sempre ativa na criacao)
     * Cada empresa so pode ter uma assinatura ativa por vez
     * @param subscription A assinatura que sera cadastrada
     * @return true se foi registrada e false se a empresa ou o plano nao existirem (ou estiverem desativados)
     * ou se a empresa ja tiver uma assinatura ativa
     */
    @Override
    public boolean register(Subscriptions subscription){

        try (Connection conn = ConnectionFactory.connect()){

            return registerConn(subscription, conn);

        } catch (SQLException e) {

            if (isUniqueViolation(e)) {
                return false;
            }

            throw new RuntimeException("Erro ao cadastrar a assinatura no banco de dados.", e);
        }
    }

    /**
     * metodo para cadastrar uma nova assinatura usando uma conexao ja aberta (sempre ativa na criacao)
     * (permite que quem chamou controle a transacao, ex: cadastrar a empresa e a assinatura juntas)
     * Dentro de uma transacao, a empresa recem-cadastrada na mesma conexao ja e enxergada pela conferencia
     * Cada empresa so pode ter uma assinatura ativa por vez
     * @param subscription A assinatura que sera cadastrada
     * @param conn A conexao (nao e fechada aqui)
     * @return true se foi registrada e false se a empresa ou o plano nao existirem (ou estiverem desativados)
     * ou se a empresa ja tiver uma assinatura ativa
     * @throws SQLException se der erro no banco
     */
    public boolean registerConn(Subscriptions subscription, Connection conn) throws SQLException {

        if (subscription == null
                || !existsActive(conn, "companies", subscription.getCompanyId())
                || !existsActive(conn, "plans", subscription.getPlanId())
                || hasActiveSubscription(conn, subscription.getCompanyId())) {
            return false;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "insert into subscriptions (company_id, plan_id, is_active, installments) " +
                        "values (?, ?, ?, ?)"
        )){

            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, subscription.isInstallments());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo para desativar a assinatura por id (is_active = false)
     * Faz a mesma coisa que deactivateSubscription -- mantido separado pra bater com a interface DAOI
     * @param id Valor unico de cada assinatura
     * @return true se foi desativada e false se a assinatura nao existe ou ja estava desativada
     */
    @Override
    public boolean delete(int id){
        return deactivateSubscription(id);
    }

    /**
     * metodo que desativa as assinaturas de uma empresa (para quando uma empresa for desativada)
     * @param companyId O id da empresa
     * @return true se alguma assinatura foi desativada e false se a empresa nao tinha assinaturas ativas
     */
    public boolean deleteByCompanyId(int companyId){

        try(Connection conn = ConnectionFactory.connect()) {

            return deleteByCompanyId(companyId, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar as assinaturas da empresa de id " + companyId + ".", e);
        }
    }

    /**
     * metodo que desativa as assinaturas de uma empresa usando uma conexao ja aberta
     * (permite que quem chamou controle a transacao, ex: desativar a empresa e tudo o que depende dela juntos)
     * @param companyId O id da empresa
     * @param conn A conexao (nao e fechada aqui)
     * @return true se alguma assinatura foi desativada e false se a empresa nao tinha assinaturas ativas
     * @throws SQLException se der erro no banco
     */
    public boolean deleteByCompanyId(int companyId, Connection conn) throws SQLException {

        try(PreparedStatement pstmt = conn.prepareStatement(
                "update subscriptions set is_active = false, deactivated_at = current_timestamp, updated_at = current_timestamp " +
                        "where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que muda os values da assinatura selecionada pelo id
     * A assinatura nao pode mudar de empresa; so o plano e o parcelamento
     * @param subscription Os valores da assinatura para ser atualizada
     * @param id O valor unico da assinatura que quer atualizar
     * @return true se foi mudada e false se o plano nao existir/estiver desativado ou a assinatura nao existir/estiver desativada
     */
    @Override
    public boolean update(Subscriptions subscription, int id){

        if (subscription == null) {
            return false;
        }

        try(Connection conn = ConnectionFactory.connect()){

            if (!existsActive(conn, "plans", subscription.getPlanId())) {
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "update subscriptions set plan_id = ?, " +
                            "installments = ?, " +
                            "updated_at = current_timestamp " +
                            "where id = ? and company_id = ? and is_active = true"
            )) {

                pstmt.setInt(1, subscription.getPlanId());
                pstmt.setBoolean(2, subscription.isInstallments());
                pstmt.setInt(3, id);
                pstmt.setInt(4, subscription.getCompanyId());

                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar a assinatura de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para ativar uma assinatura
     * So reativa se a empresa da assinatura estiver ativa e nao tiver outra assinatura ativa
     * @param id Id da assinatura
     * @return true se foi ativada e false se a assinatura nao existe, ja estava ativa, a empresa esta desativada
     * ou a empresa ja tem outra assinatura ativa
     */
    public boolean activateSubscription(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = true, deactivated_at = null, updated_at = current_timestamp " +
                                "where id = ? and is_active = false " +
                                "and exists (select 1 from companies c where c.id = subscriptions.company_id and c.is_active = true) " +
                                "and not exists (select 1 from subscriptions o where o.company_id = subscriptions.company_id and o.is_active = true)"
                )
        ){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {

            if (isUniqueViolation(e)) {
                return false;
            }

            throw new RuntimeException("Erro ao ativar a assinatura de id " + id + " no banco de dados.", e);
        }
    }

    /**
     * metodo para desativar uma assinatura
     * @param id Id da assinatura
     * @return true se foi desativada e false se a assinatura nao existe ou ja estava desativada
     */
    public boolean deactivateSubscription(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = false, deactivated_at = current_timestamp, updated_at = current_timestamp " +
                                "where id = ? and is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar a assinatura de id " + id + " no banco de dados.", e);
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Monta uma assinatura (com a empresa) a partir da linha atual do ResultSet
     * Como a juncao e left join, a empresa fica null se a assinatura nao tiver vinculo
     */
    private Subscriptions mapSubscription(ResultSet rs) throws SQLException {

        Companies company = null;

        int idCompany = rs.getInt("idCompany");
        if (!rs.wasNull()) {
            company = new Companies(idCompany, rs.getString("companyName"));
        }

        Subscriptions subscription = new Subscriptions(
                rs.getInt("id"),
                rs.getInt("companyId"),
                rs.getInt("planId"),
                rs.getBoolean("is_active"),
                rs.getBoolean("installments"),
                company
        );

        subscription.setDeactivatedAt(rs.getTimestamp("deactivated_at"));
        subscription.setCreatedAt(rs.getTimestamp("created_at"));
        subscription.setUpdatedAt(rs.getTimestamp("updated_at"));

        return subscription;
    }

    /**
     * Confere se existe um registro ativo com esse id
     * @param table Nome da tabela: use somente valores fixos do codigo ("companies" ou "plans"), nunca texto digitado
     */
    private boolean existsActive(Connection conn, String table, int id) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select 1 from " + table + " where id = ? and is_active = true"
        )) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Confere se a empresa ja tem uma assinatura ativa
     */
    private boolean hasActiveSubscription(Connection conn, int companyId) throws SQLException {

        try (PreparedStatement pstmt = conn.prepareStatement(
                "select 1 from subscriptions where company_id = ? and is_active = true"
        )) {

            pstmt.setInt(1, companyId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Confere se o erro do banco foi de valor duplicado (unique violation, SQLState 23505)
     * Aqui acontece se duas assinaturas ativas da mesma empresa forem gravadas ao mesmo tempo
     * @param e O erro do banco
     * @return true se foi duplicidade
     */
    private boolean isUniqueViolation(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

}