package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Subscriptions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsDAO {

    /**
     * Metodo que busca todas as assinaturas
     *
     * @return List<Subscriptions> Lista das assinaturas
     */
    public List<Subscriptions> searchAll(){

        List<Subscriptions> subscriptions = new ArrayList<>();
        Companies company = null;

        String sql = "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                "c.id as idCompany, c.name as companyName " +
                "from subscriptions s " +
                "join companies c on c.id = s.company_id";

        try (
                Connection conn = ConnectionFactory.connect();
                Statement stmt = conn.createStatement()
        ){

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                subscriptions.add(
                        new Subscriptions(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("planId"),
                                rs.getBoolean("is_active"),
                                rs.getBoolean("installments"),
                                company
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        finally {
            return subscriptions;
        }
    }

    /**
     * metodo para buscar por Id
     * @param id Numero unico da assinatura
     * @return Retorna a assinatura encontrada
     */
    public Subscriptions searchById(int id){
        String sql = "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                "c.id as idCompany, c.name as companyName " +
                "from subscriptions s " +
                "join companies c on c.id = s.company_id " +
                "where s.id = ?";

        Subscriptions subscription = null;
        Companies company = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                company = new Companies(
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return subscription;
        }
    }

    /**
     * metodo para buscar assinaturas pela empresa
     * @param companyId O id da empresa
     * @return List<Subscriptions> com as assinaturas encontradas
     */
    public List<Subscriptions> searchByCompanyId(int companyId){
        String sql = "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                "c.id as idCompany, c.name as companyName " +
                "from subscriptions s " +
                "join companies c on c.id = s.company_id " +
                "where c.id = ?";

        List<Subscriptions> subscriptions = new ArrayList<>();
        Companies company = null;

        try (Connection conn = ConnectionFactory.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, companyId);

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                subscriptions.add(
                        new Subscriptions(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("planId"),
                                rs.getBoolean("is_active"),
                                rs.getBoolean("installments"),
                                company
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return subscriptions;
        }
    }

    /**
     * metodo para buscar assinaturas ativas
     * @return List<Subscriptions> com as assinaturas ativas
     */
    public List<Subscriptions> searchAllActive(){
        String sql = "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                "c.id as idCompany, c.name as companyName " +
                "from subscriptions s " +
                "join companies c on c.id = s.company_id " +
                "where s.is_active = true";

        List<Subscriptions> subscriptions = new ArrayList<>();
        Companies company = null;

        try (Connection conn = ConnectionFactory.connect();
             Statement stmt = conn.createStatement()
        ){

            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){

                company = new Companies(
                        rs.getInt("idCompany"),
                        rs.getString("companyName")
                );

                subscriptions.add(
                        new Subscriptions(
                                rs.getInt("id"),
                                rs.getInt("companyId"),
                                rs.getInt("planId"),
                                rs.getBoolean("is_active"),
                                rs.getBoolean("installments"),
                                company
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return subscriptions;
        }
    }

    /**
     * metodo para cadastrar novas assinaturas no banco
     * @param subscription A assinatura que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    public boolean register(Subscriptions subscription){

        String sql = "insert into subscriptions (company_id, plan_id, is_active, installments) " +
                "values (?, ?, ?, ?)";

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ){

            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, subscription.isActive());
            pstmt.setBoolean(4, subscription.isInstallments());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * metodo para deletar a assinatura por id
     * @param id Valor unico de cada assinatura
     * @return true se foi deletada e false caso tenha dado erro
     */
    public boolean deleteById(int id){
        String sql = "delete from subscriptions where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo que muda os values da assinatura selecionada pelo id
     * @param subscription Os valores da assinatura para ser atualizada
     * @param id O valor unico da assinatura que quer atualizar
     * @return true se foi mudada e false caso tenha dado erro
     */
    public boolean updateById(Subscriptions subscription, int id){
        String sql = "update subscriptions set company_id = ?, " +
                "plan_id = ?, " +
                "is_active = ?, " +
                "installments = ?, " +
                "updated_at = current_timestamp " +
                "where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, subscription.isActive());
            pstmt.setBoolean(4, subscription.isInstallments());
            pstmt.setInt(5, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para ativar uma assinatura
     * @param id Id da assinatura
     * @return true se foi ativada e false caso tenha dado erro
     */
    public boolean activateSubscription(int id){
        String sql = "update subscriptions set is_active = true, updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * metodo para desativar uma assinatura
     * @param id Id da assinatura
     * @return true se foi desativada e false caso tenha dado erro
     */
    public boolean deactivateSubscription(int id){
        String sql = "update subscriptions set is_active = false, updated_at = current_timestamp where id = ?";

        try(Connection conn = ConnectionFactory.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
