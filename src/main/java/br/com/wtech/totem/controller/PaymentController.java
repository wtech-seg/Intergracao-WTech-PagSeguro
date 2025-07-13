// src/main/java/br/com/wtech/totem/controller/PaymentController.java
package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.TefService;
import br.com.wtech.totem.service.TicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final TefService   tefService;
    private final TicketService ticketService;

    @Value("${tef.cnpj}")
    private String tefCnpj;

    @Value("${tef.numero-pdv}")
    private String numeroPdv;

    @Value("${tef.codigo-loja}")
    private String codigoLoja;

    public PaymentController(TefService tefService,
                             TicketService ticketService) {
        this.tefService    = tefService;
        this.ticketService = ticketService;
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody PayRequest req) {
        try {
            // 1) Gera NSU e data
            String nsu  = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 2) Formata valor (em centavos -> string com vírgula)
            String valorFormatado = String.format("%d", req.getAmountInCents());
            // (a sua DLL pode já aceitar o valor em centavos direto)

            // 3) Executa TEF
            String tefResponse = tefService.processarPagamento(
                    1,                    // operação (1 = crédito à vista)
                    tefCnpj,
                    1,                    // parcelas
                    req.getTicketCode(),  // cupom
                    valorFormatado,
                    nsu,
                    data,
                    numeroPdv,
                    codigoLoja
            );

            // 4) Atualiza o ticket e histórico
            ticketService.payTicket(
                    req.getTicketCode(),
                    req.getAmountInCents(),
                    tefResponse
            );

            return ResponseEntity.ok(Map.of("status", tefResponse));
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // DTO interno para receber JSON
    public static class PayRequest {
        private String ticketCode;
        private int    amountInCents;
        // getters & setters
        public String getTicketCode() { return ticketCode; }
        public void setTicketCode(String c) { this.ticketCode = c; }
        public int getAmountInCents() { return amountInCents; }
        public void setAmountInCents(int v) { this.amountInCents = v; }
    }
}
