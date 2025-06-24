package br.com.wtech.totem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.wtech.totem.service.TicketService;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final TicketService ticketService;

    public PaymentController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody Map<String, String> body) {
        try {
            String code = body.get("ticketCode");
            int amount = Integer.parseInt(body.get("amountInCents"));
            String token = body.get("cardToken");
            String status = ticketService.payTicket(code, amount, token);
            return ResponseEntity.ok(Map.of("status", status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoints /status e /refund podem ser adicionados de forma similar
}