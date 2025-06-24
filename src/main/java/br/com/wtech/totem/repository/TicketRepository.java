package br.com.wtech.totem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.wtech.totem.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, String> {
}