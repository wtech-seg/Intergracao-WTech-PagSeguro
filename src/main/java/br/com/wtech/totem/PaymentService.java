//package br.com.wtech.totem;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//
//public class PaymentService {
//    private final String email;
//    private final String token;
//    private final HttpClient client;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public PaymentService(String email, String token) {
//        this.email = email;
//        this.token = token;
//        this.client = HttpClient.newBuilder()
//                .connectTimeout(Duration.ofSeconds(10))
//                .build();
//    }
//
//    /**
//     * Envia uma transação de cartão via Checkout Transparente v2.
//     * @param amountInCents Valor em centavos (ex: 1250 = R$12,50)
//     * @param cardToken     Token do cartão gerado no front-end
//     * @return JsonNode com o retorno da API
//     */
//    public JsonNode pagarComCartao(int amountInCents, String cardToken) throws Exception {
//        // Monta o JSON de requisição
//        JsonNode body = mapper.createObjectNode()
//                .put("email", email)
//                .put("token", token)
//                .put("paymentMode", "default")
//                .put("paymentMethod", "creditCard")
//                .put("currency", "BRL")
//                .put("itemId1", "001")
//                .put("itemDescription1", "Estacionamento")
//                .put("itemAmount1", String.format("%.2f", amountInCents / 100.0))
//                .put("itemQuantity1", "1")
//                .put("creditCardToken", cardToken)
//                .put("installmentQuantity", "1")
//                .put("installmentValue", String.format("%.2f", amountInCents / 100.0))
//                .put("noInterestInstallmentQuantity", "2");
//
//        String jsonBody = mapper.writeValueAsString(body);
//
//        // Prepara requisição HTTP
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("https://ws.pagseguro.uol.com.br/v2/transactions"))
//                .timeout(Duration.ofSeconds(20))
//                .header("Content-Type", "application/json; charset=UTF-8")
//                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
//                .build();
//
//        // Envia e retorna o JsonNode de resposta
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() != 200) {
//            throw new RuntimeException("Erro PagSeguro: " + response.body());
//        }
//        return mapper.readTree(response.body());
//    }
//}
