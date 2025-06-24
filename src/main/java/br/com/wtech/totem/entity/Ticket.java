package br.com.wtech.totem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Ticket {
    @Id
    private String code;

    @Column(nullable = false)
    private boolean paid;

    // getters e setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}