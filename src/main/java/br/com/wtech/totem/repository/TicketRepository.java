package br.com.wtech.totem.repository;

import br.com.wtech.totem.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD básico para tickets.
 * A PK é a String “code”.
 */
public interface TicketRepository extends JpaRepository<Ticket, String> {
    // Você pode adicionar métodos tipo findByStatus(...) se quiser
}
