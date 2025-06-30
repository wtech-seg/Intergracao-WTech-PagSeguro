// src/main/java/br/com/wtech/totem/controller/PaymentController.java
package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final TicketService ticketService;

    public PaymentController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody PayRequest req) {
        try {
            String chargeStatus = ticketService.payTicket(
                    req.getTicketCode(),
                    req.getAmountInCents(),
                    req.getEncryptedCard(),
                    req.getHolderName(),
                    req.getHolderTaxId()
            );
            return ResponseEntity.ok().body(Map.of("status", chargeStatus));
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
