package com.example.servelet1ano.util.service;

import com.example.servelet1ano.connection.ConnectionFactory;
import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.SubscriptionsDAO;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Subscriptions;

import java.sql.Connection;
import java.sql.SQLException;

public class CompanyRegistrationService {

    private final CompaniesDAO companiesDAO = new CompaniesDAO();
    private final SubscriptionsDAO subscriptionsDAO =
            new SubscriptionsDAO();

    /**
     * Cadastra a empresa e a assinatura juntas na mesma transacao
     * @param company A empresa que sera cadastrada
     * @param subscription A assinatura da empresa (o companyId e preenchido aqui)
     * @return O id da empresa cadastrada, ou -1 se o cnpj ja estiver cadastrado
     * (nesse caso nada e gravado). Se os dados forem invalidos ou der outro erro, lanca RuntimeException
     */
    public int register(
            Companies company,
            Subscriptions subscription
    ) {
        try (Connection conn = ConnectionFactory.connect()) {


            conn.setAutoCommit(false);

            try {

                int companyId =
                        companiesDAO.registerReturningId(company, conn);

                if (companyId <= 0) {
                    throw new IllegalStateException(
                            "Não foi possível cadastrar a empresa."
                    );
                }


                subscription.setCompanyId(companyId);


                boolean registered =
                        subscriptionsDAO.registerConn(subscription, conn);

                if (!registered) {
                    throw new IllegalStateException(
                            "Não foi possível cadastrar a assinatura."
                    );
                }

                conn.commit();

                return companyId;

            } catch (SQLException | RuntimeException e) {


                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                // cnpj duplicado (unique violation, SQLState 23505): o servlet precisa saber pra avisar o usuario
                if (e instanceof SQLException
                        && "23505".equals(((SQLException) e).getSQLState())) {
                    return -1;
                }

                throw new RuntimeException(
                        "Não foi possível concluir o cadastro da empresa "
                                + "e da assinatura.",
                        e
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro na conexão ou na transação do banco de dados.",
                    e
            );
        }
    }
}