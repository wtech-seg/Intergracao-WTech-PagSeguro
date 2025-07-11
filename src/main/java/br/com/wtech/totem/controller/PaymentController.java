// src/main/java/br/com/wtech/totem/controller/PaymentController.java
package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.TefService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final TefService tefService;

    // Valores configuráveis em application.properties
    @Value("${tef.cnpj}")
    private String tefCnpj;

    @Value("${tef.numero-pdv}")
    private String numeroPdv;

    @Value("${tef.codigo-loja}")
    private String codigoLoja;

    public PaymentController(TefService tefService) {
        this.tefService = tefService;
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody PayRequest req) {
        try {
            // 1) Gera NSU único (8 chars hex) e data no formato yyyyMMdd
            String nsu  = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
            String data = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 2) Formata valor (em centavos) para "0,00"
            String valorFormatado = formatarValor(req.getAmountInCents());

            // 3) Invoca o fluxo TEF (código 1 = crédito à vista)
            String retorno = tefService.processarPagamento(
                    1,               // operação (1 = crédito à vista)
                    tefCnpj,         // seu CNPJ
                    1,               // parcelas (sempre 1 para à vista)
                    req.getTicketCode(), // cupom (você pode usar ticketCode)
                    valorFormatado,
                    nsu,
                    data,
                    numeroPdv,
                    codigoLoja
            );

            return ResponseEntity.ok(Map.of("status", retorno));
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private String formatarValor(int amountInCents) {
        BigDecimal valor = BigDecimal.valueOf(amountInCents, 2);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        DecimalFormat fmt = new DecimalFormat("0.00", symbols);
        // substitui ponto por vírgula
        return fmt.format(valor).replace('.', ',');
    }
}
