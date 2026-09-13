package com.example.servelet1ano.dao;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Subscriptions;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsDAO implements DAOI<Subscriptions> {

    /**
     * Metodo que busca todas as assinaturas ativas
     * @return List<Subscriptions> Lista das assinaturas
     */
    @Override
    public List<Subscriptions> searchAll(){

        List<Subscriptions> subscriptions = new ArrayList<>();
        Companies company = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                                "c.id as idCompany, c.name as companyName " +
                                "from subscriptions s " +
                                "join companies c on c.id = s.company_id " +
                                "where s.is_active = true"
                )
        ){

            ResultSet rs = pstmt.executeQuery();

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
     * metodo para buscar por Id (somente ativas)
     * @param id Numero unico da assinatura
     * @return Retorna a assinatura encontrada
     */
    @Override
    public Subscriptions searchById(int id){

        Subscriptions subscription = null;
        Companies company = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                                "c.id as idCompany, c.name as companyName " +
                                "from subscriptions s " +
                                "join companies c on c.id = s.company_id " +
                                "where s.id = ? and s.is_active = true"
                )
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
     * metodo para buscar assinaturas pela empresa (somente ativas)
     * @param companyId O id da empresa
     * @return List<Subscriptions> com as assinaturas encontradas
     */
    public List<Subscriptions> searchByCompanyId(int companyId){

        List<Subscriptions> subscriptions = new ArrayList<>();
        Companies company = null;

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                                "c.id as idCompany, c.name as companyName " +
                                "from subscriptions s " +
                                "join companies c on c.id = s.company_id " +
                                "where c.id = ? and s.is_active = true"
                )
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
     * metodo para cadastrar novas assinaturas no banco (sempre ativa na criacao)
     * @param subscription A assinatura que sera cadastrada
     * @return true se foi registrada e false caso tenha dado erro
     */
    @Override
    public boolean register(Subscriptions subscription){

        try (
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into subscriptions (company_id, plan_id, is_active, installments) " +
                                "values (?, ?, ?, ?)"
                )
        ){

            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, true);
            pstmt.setBoolean(4, subscription.isInstallments());

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

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = false, updated_at = current_timestamp where id = ?"
                )
        ) {

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
    @Override
    public boolean update(Subscriptions subscription, int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set company_id = ?, " +
                                "plan_id = ?, " +
                                "installments = ?, " +
                                "updated_at = current_timestamp " +
                                "where id = ?"
                )
        ){

            pstmt.setInt(1, subscription.getCompanyId());
            pstmt.setInt(2, subscription.getPlanId());
            pstmt.setBoolean(3, subscription.isInstallments());
            pstmt.setInt(4, id);

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

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = true, updated_at = current_timestamp where id = ?"
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
     * @param id Id da assinatura
     * @return true se foi desativada e false caso tenha dado erro
     */
    public boolean deactivateSubscription(int id){

        try(
                Connection conn = ConnectionFactory.connect();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = false, updated_at = current_timestamp where id = ?"
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