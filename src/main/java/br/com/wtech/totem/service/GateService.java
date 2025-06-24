package br.com.wtech.totem.service;

import org.springframework.stereotype.Service;

@Service
public class GateService {
    /**
     * Dispara sinal para liberar a cancela.
     * Implementar comunicação com hardware específico.
     */
    public void openGate(String ticketCode) {
        // Exemplo: chamar API do controlador GPIO/Ethernet
        System.out.println("Cancela liberada para ticket: " + ticketCode);
    }
}
