package com.example.servelet1ano.util.service;

import com.example.servelet1ano.connection.DatabaseConfig;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Classe que envia e-mails pela API da Brevo
 * Por enquanto so envia o e-mail com o codigo de recuperacao de senha
 */
public class EmailService {

    /**
     * Endereco da API da Brevo usado para enviar e-mails
     */
    private static final String api = "https://api.brevo.com/v3/smtp/email";

    /**
     * Cliente HTTP reaproveitado em todos os envios (tempo maximo de 10 segundos para conectar)
     */
    private static final HttpClient cliente =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    /**
     * metodo que envia o e-mail com o codigo de recuperacao de senha
     * Usa a chave da API e os dados do remetente configurados no .env
     * @param recipientEmail O e-mail que vai receber o codigo
     * @param code O codigo de recuperacao (precisa ter exatamente seis digitos)
     * @throws IllegalStateException se as configuracoes da Brevo estiverem ausentes no .env ou se a API nao confirmar o envio
     * @throws IllegalArgumentException se o e-mail do destinatario estiver vazio ou o codigo for invalido
     * @throws IOException se der erro de comunicacao com a API
     * @throws InterruptedException se o envio for interrompido
     */
    public void sendRecoveryCode(
            String recipientEmail,
            String code
    ) throws IOException, InterruptedException {

        String apiKey = DatabaseConfig.getBrevoApiKey();
        String remetente = DatabaseConfig.getBrevoFromEmail();
        String nomeRemetente = DatabaseConfig.getBrevoFromName();

        if (apiKey == null || apiKey.isBlank()
                || remetente == null || remetente.isBlank()
                || nomeRemetente == null || nomeRemetente.isBlank()) {

            throw new IllegalStateException(
                    "Configurações do SMTP não foram encontradas."
            );
        }

        if (recipientEmail == null
                || recipientEmail.isBlank()
                || code == null
                || !code.matches("\\d{6}")) {

            throw new IllegalArgumentException(
                    "Destinatário ou código inválido."
            );
        }

        String htmlContent =
                "<html><body>"
                        + "<h2>Recuperação de senha - Aether</h2>"
                        + "<p>Você solicitou a recuperação da sua senha.</p>"
                        + "<p>Seu código de verificação é:</p>"
                        + "<h1>" + code + "</h1>"
                        + "<p>O código expira em 10 minutos.</p>"
                        + "<p>Se você não solicitou essa recuperação, "
                        + "ignore este e-mail.</p>"
                        + "</body></html>";

        String requestBody =
                "{"
                        + "\"sender\":{"
                        + "\"name\":\"" + escapeJson(nomeRemetente) + "\","
                        + "\"email\":\"" + escapeJson(remetente) + "\""
                        + "},"
                        + "\"to\":[{"
                        + "\"email\":\"" + escapeJson(recipientEmail) + "\""
                        + "}],"
                        + "\"subject\":\"Recuperação de senha - Aether\","
                        + "\"htmlContent\":\"" + escapeJson(htmlContent) + "\""
                        + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api))
                .timeout(Duration.ofSeconds(20))
                .header("accept", "application/json")
                .header("api-key", apiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = cliente.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 201) {
            throw new IllegalStateException(
                    "Falha no envio do e-mail. HTTP "
                            + response.statusCode()
            );
        }
    }

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Escapa os caracteres especiais do texto (barra invertida, aspas, quebras de linha e tab)
     * para que ele possa ser colocado dentro de um valor JSON sem quebrar o corpo da requisicao
     * @param value O texto original
     * @return O texto pronto para ir dentro de um valor JSON
     */
    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}