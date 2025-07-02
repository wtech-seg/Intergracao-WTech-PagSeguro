// src/main/java/br/com/wtech/totem/service/TicketService.java
package br.com.wtech.totem.service;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TicketService {

    private final TicketRepository ticketRepo;
    private final GateCommandProducer gateCommandProducer;
    private final PaymentService paymentService;

    public TicketService(TicketRepository ticketRepo,
                         GateCommandProducer gateCommandProducer,
                         PaymentService paymentService) {
        this.ticketRepo     = ticketRepo;
        this.gateCommandProducer = gateCommandProducer;
        this.paymentService = paymentService;
    }

    /**
     * Executa o pagamento e, se for PAID, atualiza o ticket (status=3), grava exitTime e abre a cancela.
     * Retorna "PAID","CANCELED", etc.
     */
    @Transactional
    public String payTicket(String ticketCode,
                            int amountInCents,
                            String encryptedCard,
                            String holderName,
                            String holderTaxId) throws Exception {

        // 1) Chama PagBank e obtém status da charge
        String chargeStatus = paymentService.createAndPayOrder(
                ticketCode, amountInCents, encryptedCard, holderName, holderTaxId
        );

        // 2) Mapeia para o status numérico do seu ticket
        int novoStatus;
        switch (chargeStatus) {
            case "PAID":     novoStatus = 3; break;  // PAGO
            case "CANCELED": novoStatus = 5; break;  // CANCELADO
            default:
                throw new IllegalStateException("Status inesperado: " + chargeStatus);
        }

        // 3) Atualiza o ticket no banco
        Ticket ticket = ticketRepo.findById(ticketCode)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + ticketCode));

        ticket.setStatus(novoStatus);
        ticket.setExitTime(LocalDateTime.now());
        ticketRepo.save(ticket);

        // 4) Abre a cancela
        //gateService.openGate(ticketCode);

        // 4) Enfileira o comando de abertura da cancela
        gateCommandProducer.enqueueOpenGate(ticketCode);

        return chargeStatus;
    }
}
