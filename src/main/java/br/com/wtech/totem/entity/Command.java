package br.com.wtech.totem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ace_tag_leitura")
public class Command {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ex: "OPEN_GATE" */
    @Column(nullable = false, length = 50)
    private String command;

    /** para rastrear qual ticket disparou */
    @Column(name = "NU_HASH_TAG", length = 50)
    private String ticketCode;

    /** corresponde ao DEVICE=201 que seu ExecutarComandos entende */
    @Column(name = "CD_PORTA", nullable = false, length = 20)
    private String deviceCode;

    /** 0 = pendente; 1 = executado */
    @Column(nullable = false)
    private Integer status;

    @Column(name = "DML_DATA", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "DT_LEITURA")
    private LocalDateTime executedAt = LocalDateTime.now();

    public Command() {}

    public Long getId() { return id; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}
