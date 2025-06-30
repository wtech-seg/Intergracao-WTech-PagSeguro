package br.com.wtech.totem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;

@Service
public class PaymentService {

    @Value("${pagseguro.email}")
    private String email;
    @Value("${pagseguro.token}")
    private String token;

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Retorna o status ("1" = pago, etc.)
     */
    public String pagarComCartao(int amountInCents, String cardToken) throws Exception {
        // form-urlencoded com todos os campos obrigatórios
        String amount = String.format(Locale.US, "%.2f", amountInCents / 100.0);
        StringBuilder sb = new StringBuilder();
        sb.append("email=").append(URLEncoder.encode(email, StandardCharsets.UTF_8));
        sb.append("&token=").append(URLEncoder.encode(token, StandardCharsets.UTF_8));
        sb.append("&paymentMode=default");
        sb.append("&paymentMethod=creditCard");
        sb.append("&currency=BRL");
        // buyer (sender) — substitua pelos dados reais do front
        sb.append("&senderName=").append(URLEncoder.encode("Teste Comprador", StandardCharsets.UTF_8));
        sb.append("&senderCPF=").append(URLEncoder.encode("11122233344", StandardCharsets.UTF_8));
        sb.append("&senderAreaCode=11");
        sb.append("&senderPhone=999999999");
        sb.append("&senderEmail=").append(URLEncoder.encode("email@teste.com", StandardCharsets.UTF_8));
        // shipping (exemplo)
        sb.append("&shippingAddressStreet=").append(URLEncoder.encode("Av. Brasil", StandardCharsets.UTF_8));
        sb.append("&shippingAddressNumber=1000");
        sb.append("&shippingAddressDistrict=").append(URLEncoder.encode("Centro", StandardCharsets.UTF_8));
        sb.append("&shippingAddressPostalCode=01000000");
        sb.append("&shippingAddressCity=").append(URLEncoder.encode("São Paulo", StandardCharsets.UTF_8));
        sb.append("&shippingAddressState=SP");
        sb.append("&shippingAddressCountry=BRA");
        // billing (mesmos campos)
        sb.append("&billingAddressStreet=").append(URLEncoder.encode("Av. Brasil", StandardCharsets.UTF_8));
        sb.append("&billingAddressNumber=1000");
        sb.append("&billingAddressDistrict=").append(URLEncoder.encode("Centro", StandardCharsets.UTF_8));
        sb.append("&billingAddressPostalCode=01000000");
        sb.append("&billingAddressCity=").append(URLEncoder.encode("São Paulo", StandardCharsets.UTF_8));
        sb.append("&billingAddressState=SP");
        sb.append("&billingAddressCountry=BRA");
        // cartão
        sb.append("&creditCardToken=").append(URLEncoder.encode(cardToken, StandardCharsets.UTF_8));
        sb.append("&installmentQuantity=1");
        sb.append("&installmentValue=").append(URLEncoder.encode(amount, StandardCharsets.UTF_8));
        sb.append("&noInterestInstallmentQuantity=1");
        sb.append("&creditCardHolderName=").append(URLEncoder.encode("Maria Silva", StandardCharsets.UTF_8));
        sb.append("&creditCardHolderCPF=").append(URLEncoder.encode("11122233344", StandardCharsets.UTF_8));
        sb.append("&billingAddressSameAsShipping=true");
        // itens
        sb.append("&itemId1=001");
        sb.append("&itemDescription1=").append(URLEncoder.encode("Estacionamento", StandardCharsets.UTF_8));
        sb.append("&itemAmount1=").append(URLEncoder.encode(amount, StandardCharsets.UTF_8));
        sb.append("&itemQuantity1=1");

        String form = sb.toString();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://ws.sandbox.pagseguro.uol.com.br/v2/transactions"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Erro PagSeguro: HTTP "
                    + resp.statusCode() + " - " + resp.body());
        }

        // extrai <status> do XML
        var xml = resp.body();
        var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return doc.getElementsByTagName("status").item(0).getTextContent();
    }
}
