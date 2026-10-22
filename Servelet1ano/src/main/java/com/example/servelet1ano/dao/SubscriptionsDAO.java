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
     * Select base das assinaturas, com o join da empresa
     * Termina com espaco, entao é so colocar o where depois
     */
    private static final String SELECT_SUBSCRIPTIONS =
            "select s.*, s.company_id as companyId, s.plan_id as planId, " +
                    "c.id as idCompany, c.name as companyName " +
                    "from subscriptions s " +
                    "join companies c on c.id = s.company_id ";

    /**
     * Abre a conexao com o banco
     * @return A conexao aberta
     * @throws IllegalStateException se nao for possivel conectar
     */
    private Connection requireConnection() {
        Connection conn = ConnectionFactory.connect();

        if (conn == null) {
            throw new IllegalStateException(
                    "Não foi possível estabelecer conexão com o banco de dados."
            );
        }

        return conn;
    }

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
     * Monta um objeto Subscriptions a partir da linha atual do ResultSet (com a empresa)
     * @param rs O ResultSet posicionado na linha que sera lida (vindo do SELECT_SUBSCRIPTIONS)
     * @return O Subscriptions com os dados da linha
     */
    private Subscriptions mapSubscription(ResultSet rs) throws SQLException {

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

        return subscription;
    }


    /**
     * Metodo que busca as assinaturas ativas aplicando os filtros informados
     * Os campos do filtro que vierem preenchidos entram na consulta; os que vierem nulos sao ignorados
     * Se o filter vier null, busca sem filtro nenhum
     * @param filter Objeto com os campos opcionais para filtrar a busca
     * @return List<Subscriptions> com as assinaturas encontradas
     */
    @Override
    public List<Subscriptions> searchAll(SubscriptionsFilter filter) {

        List<Subscriptions> subscriptions = new ArrayList<>();

        if (filter == null) {
            filter = new SubscriptionsFilter();
        }

        StringBuilder sql = new StringBuilder(
                SELECT_SUBSCRIPTIONS + "where s.is_active = true"
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

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    subscriptions.add(mapSubscription(rs));
                }
            }

            return subscriptions;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar assinaturas.",
                    e
            );
        }
    }

    /**
     * metodo para buscar por Id (somente ativas)
     * @param id Numero unico da assinatura
     * @return Retorna a assinatura encontrada ou null caso nao exista
     */
    @Override
    public Subscriptions searchById(int id) {

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        SELECT_SUBSCRIPTIONS + "where s.id = ? and s.is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return mapSubscription(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar assinatura pelo ID: " + id,
                    e
            );
        }
    }

    /**
     * metodo para cadastrar novas assinaturas no banco (sempre ativa na criacao)
     * A empresa e o plano precisam existir e estar ativos (conferido no proprio comando)
     * @param subscription A assinatura que sera cadastrada
     * @return true se foi registrada e false se os dados forem invalidos ou a empresa/plano nao estiverem ativos
     */
    @Override
    public boolean register(Subscriptions subscription) {

        if (!isValid(subscription)) {
            return false;
        }

        try (Connection conn = requireConnection()) {

            return registerConn(subscription, conn);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar assinatura.",
                    e
            );
        }
    }

    /**
     * metodo para cadastrar uma assinatura usando uma conexao que ja foi aberta, habilitando rollback
     * Nao abre nem fecha a conexao e nao faz commit nem rollback: quem chamou controla isso
     * Mesmas regras do register: a empresa e o plano precisam existir e estar ativos
     * @param subscription A assinatura que sera cadastrada
     * @param conn A conexao ja aberta que sera usada
     * @return true se foi registrada e false se os dados forem invalidos ou a empresa/plano nao estiverem ativos
     */
    public boolean registerConn(Subscriptions subscription, Connection conn) {

        if (!isValid(subscription)) {
            return false;
        }

        try (
                PreparedStatement pstmt = conn.prepareStatement(
                        "insert into subscriptions (company_id, plan_id, is_active, installments) " +
                                "select c.id, p.id, true, ? " +
                                "from companies c, plans p " +
                                "where c.id = ? and c.is_active = true " +
                                "and p.id = ? and p.is_active = true"
                )
        ) {

            pstmt.setBoolean(1, subscription.isInstallments());
            pstmt.setInt(2, subscription.getCompanyId());
            pstmt.setInt(3, subscription.getPlanId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar assinatura.",
                    e
            );
        }
    }

    /**
     * metodo para desativar a assinatura por id (is_active = false)
     * Faz a mesma coisa que deactivateSubscription -- mantido separado pra bater com a interface DAOI
     * @param id Valor unico de cada assinatura
     * @return true se foi desativada e false caso a assinatura nao exista ou ja esteja desativada
     */
    @Override
    public boolean delete(int id) {

        return deactivateSubscription(id);
    }

    /**
     * metodo que desativa as assinaturas de uma empresa (para quando uma empresa for desativada)
     * Se a empresa nao tiver assinaturas devolve false, e isso nao e um erro
     * @param companyId O id da empresa
     * @return true se foi desativada e false caso a empresa nao tenha assinaturas ativas
     */
    public boolean deleteByCompanyId(int companyId) {

        try (Connection conn = requireConnection()) {

            return deleteByCompanyId(conn, companyId);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar assinaturas da empresa: " + companyId,
                    e
            );
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

        try (
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
     * @return true se foi mudada e false se os dados forem invalidos ou alguma regra acima nao passar
     */
    @Override
    public boolean update(Subscriptions subscription, int id) {

        if (!isValid(subscription)) {
            return false;
        }

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions s set plan_id = ?, " +
                                "installments = ?, " +
                                "updated_at = current_timestamp " +
                                "where s.id = ? and s.company_id = ? and s.is_active = true " +
                                "and exists (select 1 from plans p where p.id = ? and p.is_active = true)"
                )
        ) {

            pstmt.setInt(1, subscription.getPlanId());
            pstmt.setBoolean(2, subscription.isInstallments());
            pstmt.setInt(3, id);
            pstmt.setInt(4, subscription.getCompanyId());
            pstmt.setInt(5, subscription.getPlanId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar assinatura pelo ID: " + id,
                    e
            );
        }
    }

    /**
     * metodo para ativar uma assinatura
     * So ativa se a empresa e o plano da assinatura estiverem ativos
     * @param id Id da assinatura
     * @return true se foi ativada e false caso a assinatura nao exista ou a empresa/plano nao estejam ativos
     */
    public boolean activateSubscription(int id) {

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions s set is_active = true, deactivated_at = null, updated_at = current_timestamp " +
                                "where s.id = ? " +
                                "and exists (select 1 from companies c where c.id = s.company_id and c.is_active = true) " +
                                "and exists (select 1 from plans p where p.id = s.plan_id and p.is_active = true)"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao ativar assinatura pelo ID: " + id,
                    e
            );
        }
    }

    /**
     * metodo para desativar uma assinatura
     * Se a assinatura ja estava desativada nao faz nada (assim o deactivated_at dela nao é sobrescrito)
     * @param id Id da assinatura
     * @return true se foi desativada e false caso a assinatura nao exista ou ja esteja desativada
     */
    public boolean deactivateSubscription(int id) {

        try (
                Connection conn = requireConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "update subscriptions set is_active = false, deactivated_at = current_timestamp, updated_at = current_timestamp " +
                                "where id = ? and is_active = true"
                )
        ) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao desativar assinatura pelo ID: " + id,
                    e
            );
        }
    }

}