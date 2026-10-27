package com.example.servelet1ano.util.service;

import com.example.servelet1ano.connection.DatabaseConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class EmailService {

    private static final String api = "https://api.brevo.com/v3/smtp/email";

    private static final HttpClient cliente =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

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

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

