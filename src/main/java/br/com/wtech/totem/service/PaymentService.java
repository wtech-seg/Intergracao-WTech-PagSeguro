// src/main/java/br/com/wtech/totem/service/PaymentService.java
package br.com.wtech.totem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PaymentService {

    @Value("${pagbank.token}")
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
                                    String holderTaxId) throws Exception {

        // 1) Monta o JSON do payload
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
                ticketCode,        // ex: "TKT12345"
                ticketCode,
                amountInCents,     // ex: 1250
                ticketCode,
                amountInCents,
                encryptedCard,     // do JS
                holderName,        // ex: "Maria Silva"
                holderTaxId        // ex: "12345678909"
        );

        // 2) Executa a requisição
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

        // 3) Lê o status da primeira charge
        JsonNode root = mapper.readTree(resp.body());
        JsonNode charge = root.path("charges").get(0);
        return charge.path("status").asText();
    }
}
