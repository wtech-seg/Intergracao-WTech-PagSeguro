package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaServidorConectadoController {

    @FXML private AnchorPane root;
    @FXML private Label labelValorTotal;

    @Autowired private LeitorService leitorService;
    @Autowired private PagamentoTEFService pagamentoTEFService;
    @Autowired private NavegacaoUtil navegaPara;

    private ChangeListener<String> tefStatusListener;

    @FXML
    private void initialize() {
        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        this.tefStatusListener = (obs, oldStatus, newStatus) -> {
            Platform.runLater(() -> {
                boolean shouldNavigate = false;
                String destination = "";

                switch (newStatus) {
                    case "WAITING_FOR_CARD":
                        destination = "/fxml/tela_aguardando_pagamento.fxml";
                        shouldNavigate = true;
                        break;
                    case "ERROR":
                        destination = "/fxml/tela_pagamento_selecionado.fxml";
                        shouldNavigate = true;
                        break;
                    case "CANCELLED":
                        destination = "/fxml/tela_forma_pagamento.fxml";
                        shouldNavigate = true;
                        break;
                }

                if (shouldNavigate) {
                    // A SOLUÇÃO DEFINITIVA: O próprio listener se remove antes de navegar.
                    pagamentoTEFService.tefStatusProperty().removeListener(this.tefStatusListener);
                    navegaPara.trocaTela(destination, root);
                }
            });
        };

        pagamentoTEFService.tefStatusProperty().addListener(this.tefStatusListener);
    }
}