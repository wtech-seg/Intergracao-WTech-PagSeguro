// src/main/java/br/com/wtech/totem/controller/PayRequest.java
package br.com.wtech.totem.controller;

/** DTO para receber o JSON de pagamento */
public class PayRequest {
    private String ticketCode;
    private int amountInCents;
    private String encryptedCard;
    private String holderName;
    private String holderTaxId;
    private String holderEmail;
    // getters e setters
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public int getAmountInCents() { return amountInCents; }
    public void setAmountInCents(int amountInCents) { this.amountInCents = amountInCents; }
    public String getEncryptedCard() { return encryptedCard; }
    public void setEncryptedCard(String encryptedCard) { this.encryptedCard = encryptedCard; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public String getHolderTaxId() { return holderTaxId; }
    public void setHolderTaxId(String holderTaxId) { this.holderTaxId = holderTaxId; }public String getHolderEmail() { return holderEmail; }
    public void setHolderEmail(String holderEmail) { this.holderEmail = holderEmail; }
}
