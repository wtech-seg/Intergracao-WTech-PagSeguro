package br.com.wtech.totem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.xml.parsers.DocumentBuilderFactory;

@Service
public class PaymentService {
    private final String email;
    private final String token;
    private final HttpClient client;

    public PaymentService(
            @Value("${pagseguro.email}") String email,
            @Value("${pagseguro.token}") String token) {
        this.email = email;
        this.token = token;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String pagarComCartao(int amountInCents, String cardToken) throws Exception {
        StringBuilder form = new StringBuilder();
        form.append("email=").append(URLEncoder.encode(email, StandardCharsets.UTF_8));
        form.append("&token=").append(URLEncoder.encode(token, StandardCharsets.UTF_8));
        form.append("&paymentMode=default");
        form.append("&paymentMethod=creditCard");
        form.append("&currency=BRL");
        form.append("&itemId1=").append(URLEncoder.encode("001", StandardCharsets.UTF_8));
        form.append("&itemDescription1=").append(URLEncoder.encode("Estacionamento", StandardCharsets.UTF_8));
        form.append("&itemAmount1=").append(URLEncoder.encode(
                String.format("%.2f", amountInCents / 100.0), StandardCharsets.UTF_8));
        form.append("&itemQuantity1=1");
        form.append("&creditCardToken=").append(URLEncoder.encode(cardToken, StandardCharsets.UTF_8));
        form.append("&installmentQuantity=1");
        form.append("&installmentValue=").append(URLEncoder.encode(
                String.format("%.2f", amountInCents / 100.0), StandardCharsets.UTF_8));
        form.append("&noInterestInstallmentQuantity=2");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ws.sandbox.pagseguro.uol.com.br/v2/transactions"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("Erro PagSeguro: HTTP " + response.statusCode() + " - " + response.body());
        }

        var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)));
        return doc.getElementsByTagName("status").item(0).getTextContent();
    }
}