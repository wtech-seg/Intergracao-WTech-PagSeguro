// src/main/java/br/com/wtech/totem/service/TicketService.java
package br.com.wtech.totem.service;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.repository.TicketRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TicketService {

    private final TicketRepository ticketRepo;
    private final PaymentService paymentService;
    private final JdbcTemplate jdbc;

    public TicketService(TicketRepository ticketRepo,
                         PaymentService paymentService,
                         JdbcTemplate jdbc) {
        this.ticketRepo     = ticketRepo;
        this.paymentService = paymentService;
        this.jdbc           = jdbc;
    }

    /**
     * Executa o pagamento e, se for PAID, atualiza o ticket (status=3), grava exitTime
     * e insere diretamente no histórico de pagamentos.
     */
    @Transactional
    public String payTicket(String ticketCode,
                            int amountInCents,
                            String encryptedCard,
                            String holderName,
                            String holderTaxId, String holderEmail) throws Exception {

        // 1) chama PagBank e obtém status
        String chargeStatus = paymentService.createAndPayOrder(
                ticketCode, amountInCents, encryptedCard, holderName, holderTaxId, holderEmail
        );

        // 2) busca o ticket
        Ticket ticket = ticketRepo.findById(ticketCode)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + ticketCode));

        // 3) lógica por status
        switch (chargeStatus) {
            case "PAID":
                // 3.1) atualiza o ticket
                ticket.setStatus(3);  // PAGO
                ticket.setExitTime(LocalDateTime.now());
                ticketRepo.save(ticket);

                // 3.2) insere linha direta no banco em outra tabela
                jdbc.update(
                        "INSERT INTO ace_qr_code (no_qr_code, fl_situacao, dt_validade_ini, cd_porta) VALUES (?,'A',?,'/dev/usb/lp1')",
                        ticketCode,
                        LocalDateTime.now()
                );
                break;

            case "CANCELED":
                ticket.setStatus(5);  // CANCELADO
                ticketRepo.save(ticket);
                break;

            default:
                throw new IllegalStateException("Status inesperado: " + chargeStatus);
        }

        return chargeStatus;
    }
}
