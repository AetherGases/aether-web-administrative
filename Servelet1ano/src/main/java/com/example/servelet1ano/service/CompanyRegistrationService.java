
package com.example.servelet1ano.service;

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

