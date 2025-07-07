package br.com.wtech.totem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "est_tickets")
public class Ticket {

    @Id
    @Column(name = "cd_ticket", length = 50)
    private String code;

    @Column(name = "dt_inicial", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "dt_final")
    private LocalDateTime exitTime;

    @Column(name = "vl_final")
    private BigDecimal finalValue;

    @Column(name = "cd_tipo_pagamento")
    private Integer tipoPagamento;

    @Column(name = "fl_status", nullable = false)
    private Integer status;

    // --- Getters e Setters ---

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    // Alias para uso no RowMapper
    public String getTicketCode() {
        return code;
    }

    public void setTicketCode(String ticketCode) {
        this.code = ticketCode;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getFinalValue() {
        return finalValue;
    }

    public void setFinalValue(BigDecimal finalValue) {
        this.finalValue = finalValue;
    }

    public Integer getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(Integer tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
