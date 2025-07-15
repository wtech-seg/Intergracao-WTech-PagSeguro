// src/main/java/br/com/wtech/totem/service/TefOperation.java
package br.com.wtech.totem.service;

/**
 * Define as operações TEF e o código inteiro que a DLL espera.
 */
public enum TefOperation {
    CREDIT(0),    // crédito à vista
    DEBIT(1),     // débito à vista
    CANCEL(5),    // cancelamento
    REPRINT(6);  // reimpressão

    private final int code;

    TefOperation(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
