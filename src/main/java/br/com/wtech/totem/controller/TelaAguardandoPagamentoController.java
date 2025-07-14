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
public class TelaAguardandoPagamentoController {

    @FXML private AnchorPane root;
    @FXML private Label labelValorTotal;

    @Autowired private LeitorService leitorService;
    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    private ChangeListener<String> tefStatusListener;

    @FXML
    private void initialize() {
        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        this.tefStatusListener = (obs, oldStatus, newStatus) -> {
            Platform.runLater(() -> {
                boolean isTransactionOver = false;
                String destination = "";

                System.out.println("newStatus: " + newStatus);

                switch (newStatus) {
                    case "FINISHED":
                        destination = "/fxml/tela_pagamento_selecionado.fxml";
                        isTransactionOver = true;
                        break;
                    case "ERROR":
                        destination = "/fxml/tela_forma_pagamento.fxml";
                        isTransactionOver = true;
                        break;
                }

                if (isTransactionOver) {
                    // A SOLUÇÃO DEFINITIVA: O próprio listener se remove antes de navegar.
                    pagamentoTEFService.tefStatusProperty().removeListener(this.tefStatusListener);
                    navegaPara.trocaTela(destination, root);
                }
            });
        };

        pagamentoTEFService.tefStatusProperty().addListener(this.tefStatusListener);
    }
}