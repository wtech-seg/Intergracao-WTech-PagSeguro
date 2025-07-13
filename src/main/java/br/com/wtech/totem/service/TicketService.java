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
    private final JdbcTemplate jdbc;

    public TicketService(TicketRepository ticketRepo,
                         JdbcTemplate jdbc) {
        this.ticketRepo = ticketRepo;
        this.jdbc       = jdbc;
    }

    /**
     * Atualiza o ticket no banco conforme o retorno da TEF.
     * Se tefResponse indicar sucesso (começa com "[RETORNO]"), marca como PAGO (status=3),
     * grava exitTime e insere no histórico; caso contrário, marca como CANCELADO (status=5).
     */
    @Transactional
    public void payTicket(String ticketCode,
                          int amountInCents,
                          String tefResponse) {

        // 1) Busca o ticket
        Ticket ticket = ticketRepo.findById(ticketCode)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + ticketCode));

        // 2) Decide pelo status
        if (tefResponse.startsWith("[RETORNO]")) {
            // pago
            ticket.setStatus(3);              // PAGO
            ticket.setExitTime(LocalDateTime.now());
            ticketRepo.save(ticket);

            // insere no histórico (ajuste a tabela/colunas conforme seu schema)
            jdbc.update(
                    "INSERT INTO ace_qr_code_historico (no_qr_code, dt_pagamento, valor_cents) VALUES (?,?,?)",
                    ticketCode,
                    LocalDateTime.now(),
                    amountInCents
            );
        } else {
            // cancelado
            ticket.setStatus(5);              // CANCELADO
            ticketRepo.save(ticket);
        }
    }
}
