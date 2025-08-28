package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Este Controller gerencia a lógica APENAS para o componente do botão de cancelar.
 */
@Component
public class BotaoCancelarController {

    @Autowired
    private PagamentoTEFService pagamentoTEFService;

    @Autowired
    private NavegacaoUtil navegaPara;

    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("BOTÃO CANCELAR UNIVERSAL: Ação disparada.");

        Node source = (Node) event.getSource();
        String statusAtual = pagamentoTEFService.getStatus();

        if ("IDLE".equals(statusAtual) || "FINISHED".equals(statusAtual) || "ERROR".equals(statusAtual)) {
            System.out.println("   -> Nenhuma operação TEF em andamento. Apenas navegando para a tela inicial.");
            navegaPara.trocaTela("/fxml/tela_inicial.fxml", source);
        } else {
            System.out.println("   -> Operação TEF em andamento detectada. Solicitando cancelamento...");
            pagamentoTEFService.solicitarCancelamento();
        }
    }
}