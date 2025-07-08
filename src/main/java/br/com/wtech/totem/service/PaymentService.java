// src/main/java/br/com/wtech/totem/service/PaymentService.java
package br.com.wtech.totem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PaymentService {

    @Value("B8B9DF4E0BF2479E8676AF43AA2B5FF7")
    private String token;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Cria um pedido + cobrança no PagBank e retorna o status da charge ("PAID","CANCELED",...)
     */
    public String createAndPayOrder(String ticketCode,
                                    int amountInCents,
                                    String encryptedCard,
                                    String holderName,
                                    String holderTaxId,
                                    String holderEmail) throws Exception {

        // 0) Validação básica do e‑mail
        if (holderEmail == null
                || holderEmail.length() < 5
                || holderEmail.length() > 60
                || !holderEmail.matches("^([a-zA-Z0-9.!#$%&'*+\\\\/=?^_`{|}~-]+)@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("E‑mail do cliente inválido: " + holderEmail);
        }

        // Debug: imprime o e‑mail e tamanho
        System.out.println("Email do cliente: [" + holderEmail + "] (length=" + holderEmail.length() + ")");

        // 1) Monta o JSON do payload, incluindo customer
        String payload = """
        {
          "reference_id":"%s",
          "items":[
            {
              "reference_id":"%s",
              "name":"Estacionamento Drive-Thru",
              "quantity":1,
              "unit_amount":%d
            }
          ],
          "customer":{
            "name":"%s",
            "tax_id":"%s",
            "email":"%s"
          },
          "charges":[
            {
              "reference_id":"CHG-%s",
              "amount":{
                "value":%d,
                "currency":"BRL"
              },
              "payment_method":{
                "type":"CREDIT_CARD",
                "installments":1,
                "capture":true,
                "card":{
                  "encrypted":"%s",
                  "store":false
                },
                "holder":{
                  "name":"%s",
                  "tax_id":"%s"
                }
              }
            }
          ]
        }
        """.formatted(
                ticketCode,      // ex: "TKT12345"
                ticketCode,
                amountInCents,   // ex: 1250
                holderName,      // ex: "Maria Silva"
                holderTaxId,     // ex: "12345678909"
                holderEmail,     // ex: "maria.silva@example.com"
                ticketCode,
                amountInCents,
                encryptedCard,
                holderName,
                holderTaxId
        );

        // 2) Executa a requisição ao PagBank
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://sandbox.api.pagseguro.com/orders"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new RuntimeException("Erro PagBank: HTTP " +
                    resp.statusCode() + " - " + resp.body());
        }

        // 3) Lê o status da primeira charge e retorna
        JsonNode root = mapper.readTree(resp.body());
        JsonNode charge = root.path("charges").get(0);
        return charge.path("status").asText();
    }
}