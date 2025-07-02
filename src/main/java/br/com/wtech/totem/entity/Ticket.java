// src/main/java/br/com/wtech/totem/entity/Ticket.java
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

    /**
     * status no banco:
     * 1=GERADO, 2=CORTESIA, 3=PAGO, 4=LIDO_SAIDA, 5=CANCELADO
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    // getters e setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
