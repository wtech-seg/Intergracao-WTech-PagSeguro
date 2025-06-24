package br.com.wtech.totem.service;

import br.com.wtech.totem.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.wtech.totem.repository.TicketRepository;
import br.com.wtech.totem.entity.Ticket;

@Service
public class TicketService {
    private final TicketRepository ticketRepo;
    private final GateService gateService;
    private final PaymentService paymentService;

    public TicketService(TicketRepository ticketRepo, GateService gateService, PaymentService paymentService) {
        this.ticketRepo = ticketRepo;
        this.gateService = gateService;
        this.paymentService = paymentService;
    }

    @Transactional
    public String payTicket(String ticketCode, int amountInCents, String cardToken) throws Exception {
        // 1. Executa pagamento
        String status = paymentService.pagarComCartao(amountInCents, cardToken);
        if ("1".equals(status)) { // 1 = pago
            // 2. Marca no banco como pago
            Ticket ticket = ticketRepo.findById(ticketCode)
                    .orElseGet(() -> { Ticket t = new Ticket(); t.setCode(ticketCode); return t; });
            ticket.setPaid(true);
            ticketRepo.save(ticket);

            // 3. Abre a cancela
            gateService.openGate(ticketCode);
        }
        return status;
    }
}