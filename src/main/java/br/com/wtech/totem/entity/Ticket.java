package br.com.wtech.totem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TicketStatus status;

    public Ticket() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
}
