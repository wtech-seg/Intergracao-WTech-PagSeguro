// src/main/java/br/com/wtech/totem/service/GateCommandProducer.java
package br.com.wtech.totem.service;

import br.com.wtech.totem.entity.AceComando;
import br.com.wtech.totem.repository.AceComandoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GateCommandProducer {

    private final AceComandoRepository repo;

    /** Device code configurável (ex: "201") */
    @Value("${gate.device}")
    private String deviceCode;

    public GateCommandProducer(AceComandoRepository repo) {
        this.repo = repo;
    }

    /**
     * Insere na tabela ace_comandos um OPEN_GATE pendente (status = 0).
     */
    public void enqueueOpenGate(String ticketCode) {
        AceComando cmd = new AceComando();
        cmd.setCommand("OPEN_GATE");
        cmd.setTicketCode(ticketCode);
        cmd.setDeviceCode(deviceCode);
        cmd.setStatus(0);
        repo.save(cmd);
    }
}
