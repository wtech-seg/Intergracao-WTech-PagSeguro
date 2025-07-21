// src/main/java/br/com/wtech/totem/service/TefOperation.java
package br.com.wtech.totem.service;

/**
 * Define as operações TEF e o código inteiro que a DLL espera.
 */
public enum TefOperation {
    CREDIT(0),    // crédito à vista
    DEBIT(4),     // débito à vista
    CANCEL(5),    // cancelamento
    REPRINT(6),  // reimpressão
    PIX(51),
    CANCEL_PIX(54),
    REPRINT_PIX(59);

    private final int code;

    TefOperation(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
