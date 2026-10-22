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
     * Valida os dados da assinatura antes de gravar (empresa e plano precisam ser informados)
     * @param subscription A assinatura a validar
     * @return true se a assinatura existir e tiver o id da empresa e do plano
     */
    private boolean isValid(Subscriptions subscription) {

        if (subscription == null) {
            return false;
        }

        if (subscription.getCompanyId() <= 0) {
            return false;
        }

        if (subscription.getPlanId() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Metodo que busca as assinaturas ativas aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Subscriptions> com as assinaturas encontradas
     */
    @Override
    public List<Subscriptions> searchAll(SubscriptionsFilter filter){

        List<Subscriptions> subscriptions = new ArrayList<>();

        if (filter == null) {
            filter = new SubscriptionsFilter();
        }

        StringBuilder sql = new StringBuilder(
                "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                        "c.id as idCompany, c.name as companyName " +
                        "from subscriptions s " +
                        "join companies c on c.id = s.company_id " +
                        "where s.is_active = true"
        );

        List<Object> parametros = new ArrayList<>();

        if (filter.getId() != null) {
            sql.append(" and s.id = ?");
            parametros.add(filter.getId());
        }

        if (filter.getCompanyId() != null) {
            sql.append(" and s.company_id = ?");
            parametros.add(filter.getCompanyId());
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return subscriptions;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ){

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()){

                    Companies company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

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

                    subscriptions.add(subscription);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subscriptions;
    }

    /**
     * metodo para buscar por Id (somente ativas)
     * @param id Numero unico da assinatura
     * @return Retorna a assinatura encontrada ou null caso nao exista
     */
    @Override
    public Subscriptions searchById(int id){

        Subscriptions subscription = null;

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return null;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                                "c.id as idCompany, c.name as companyName " +
                                "from subscriptions s " +
                                "join companies c on c.id = s.company_id " +
                                "where s.id = ? and s.is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if(rs.next()){
                    Companies company = new Companies(
                            rs.getInt("idCompany"),
                            rs.getString("companyName")
                    );

                    subscription = new Subscriptions(
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
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subscription;
    }

    /**
     * metodo para cadastrar novas assinaturas no banco (sempre ativa na criacao)
     * A empresa e o plano precisam existir e estar ativos (conferido no proprio comando)
     * @param subscription A assinatura que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    @Override
    public boolean register(Subscriptions subscription){

        if (!isValid(subscription)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try (
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into subscriptions (company_id, plan_id, is_active, installments) " +
                                "select c.id, p.id, true, ? " +
                                "from companies c, plans p " +
                                "where c.id = ? and c.is_active = true " +
                                "and p.id = ? and p.is_active = true"
                )
        ){

            pstmt.setBoolean(1, subscription.isInstallments());
            pstmt.setInt(2, subscription.getCompanyId());
            pstmt.setInt(3, subscription.getPlanId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar a assinatura por id (is_active = false)
     * Faz a mesma coisa que deactivateSubscription -- mantido separado pra bater com a interface DAOI
     * @param id Valor unico de cada assinatura
     * @return true se foi desativada e false caso tenha dado erro
     */
    @Override
    public boolean delete(int id){

        return deactivateSubscription(id);
    }

    /**
     * metodo que desativa as assinaturas de uma empresa (para quando uma empresa for desativada)
     * Se a empresa nao tiver assinaturas devolve false, e isso nao e um erro
     * @param companyId O id da empresa
     * @return true se foi desativada e false caso tenha dado erro
     */
    public boolean deleteByCompanyId(int companyId){

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(conn) {

            return deleteByCompanyId(conn, companyId);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que desativa as assinaturas de uma empresa dentro de uma transacao que ja esta aberta
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Nao mexe nas assinaturas que ja estavam desativadas (assim o deactivated_at delas nao é sobrescrito)
     * Se a empresa nao tiver assinaturas ativas devolve false, e isso nao e um erro
     * @param conn A conexao da transacao
     * @param companyId O id da empresa
     * @return true se alguma assinatura foi desativada
     * @throws SQLException se o banco der erro, pra quem chamou desfazer a transacao (rollback)
     */
    public boolean deleteByCompanyId(Connection conn, int companyId) throws SQLException {

        try(
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = false, deactivated_at = current_timestamp, updated_at = current_timestamp " +
                                "where company_id = ? and is_active = true"
                )
        ) {

            pstmt.setInt(1, companyId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * metodo que muda os values da assinatura selecionada pelo id
     * A assinatura nao pode ser levada pra outra empresa: o companyId da assinatura passada é usado pra conferir
     * que a assinatura é mesmo daquela empresa
     * Nao atualiza assinatura desativada, e o plano precisa existir e estar ativo
     * @param subscription Os valores da assinatura para ser atualizada
     * @param id O valor unico da assinatura que quer atualizar
     * @return true se foi mudada e false caso tenha dado erro
     */
    @Override
    public boolean update(Subscriptions subscription, int id){

        if (!isValid(subscription)) {
            return false;
        }

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions s set plan_id = ?, " +
                                "installments = ?, " +
                                "updated_at = current_timestamp " +
                                "where s.id = ? and s.company_id = ? and s.is_active = true " +
                                "and exists (select 1 from plans p where p.id = ? and p.is_active = true)"
                )
        ){

            pstmt.setInt(1, subscription.getPlanId());
            pstmt.setBoolean(2, subscription.isInstallments());
            pstmt.setInt(3, id);
            pstmt.setInt(4, subscription.getCompanyId());
            pstmt.setInt(5, subscription.getPlanId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para ativar uma assinatura
     * So ativa se a empresa e o plano da assinatura estiverem ativos
     * @param id Id da assinatura
     * @return true se foi ativada e false caso tenha dado erro
     */
    public boolean activateSubscription(int id){

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions s set is_active = true, deactivated_at = null, updated_at = current_timestamp " +
                                "where s.id = ? " +
                                "and exists (select 1 from companies c where c.id = s.company_id and c.is_active = true) " +
                                "and exists (select 1 from plans p where p.id = s.plan_id and p.is_active = true)"
                )
        ){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar uma assinatura
     * Se a assinatura ja estava desativada nao faz nada (assim o deactivated_at dela nao é sobrescrito)
     * @param id Id da assinatura
     * @return true se foi desativada e false caso tenha dado erro
     */
    public boolean deactivateSubscription(int id){

        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            return false;
        }

        try(
                conn;
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = false, deactivated_at = current_timestamp, updated_at = current_timestamp " +
                                "where id = ? and is_active = true"
                )
        ){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}