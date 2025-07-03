package br.com.wtech.totem.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

@RestController
@RequestMapping("/api")
public class SessionController {

    @Value("suavagaarapiraca@gmail.com")
    private String email;
    @Value("${pagseguro.token}")
    private String token;

    private final HttpClient client = HttpClient.newHttpClient();

    @GetMapping("/session")
    public Map<String,String> getSession() throws Exception {
        // monta form-urlencoded
        String form = "email="   + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://ws.sandbox.pagseguro.uol.com.br/v2/sessions"))
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE
                        + ";charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Erro ao obter sessionId: " + resp.body());
        }

        // parse simples do XML recebido
        var xml = resp.body();
        var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        String sessionId = doc.getElementsByTagName("id").item(0).getTextContent();

        return Map.of("sessionId", sessionId);
    }
}
