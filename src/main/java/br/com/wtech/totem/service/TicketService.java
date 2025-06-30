package br.com.wtech.totem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.wtech.totem.repository.TicketRepository;
import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.entity.TicketStatus;
import java.time.LocalDateTime;

@Service
public class TicketService {
    private final TicketRepository ticketRepo;
    private final GateService gateService;
    private final PaymentService paymentService;

    public TicketService(TicketRepository ticketRepo,
                         GateService gateService,
                         PaymentService paymentService) {
        this.ticketRepo     = ticketRepo;
        this.gateService    = gateService;
        this.paymentService = paymentService;
    }

    @Transactional
    public String payTicket(String ticketCode, int amountInCents, String cardToken) throws Exception {
        // 1. Executa o pagamento na PagBank
        String status = paymentService.pagarComCartao(amountInCents, cardToken);

        if ("1".equals(status)) { // "1" == pago
            // 2. Marca o ticket no banco como PAGO
            Ticket ticket = ticketRepo.findById(ticketCode)
                    .orElseGet(() -> {
                        Ticket t = new Ticket();
                        t.setCode(ticketCode);
                        return t;
                    });

            // Substitua setPaid(true) por:
            ticket.setStatus(TicketStatus.PAGO);
            ticket.setExitTime(LocalDateTime.now());

            ticketRepo.save(ticket);

            // 3. Abre a cancela
            gateService.openGate(ticketCode);
        }

        return status;
    }
}
