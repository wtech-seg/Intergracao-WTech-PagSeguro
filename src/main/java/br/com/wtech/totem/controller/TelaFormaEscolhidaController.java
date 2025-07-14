package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.FormaPagamentoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class TelaFormaEscolhidaController {

    @FXML private AnchorPane root;
    @FXML private Label labelFormaEscolhida;
    @FXML private Label labelValorTotal;
    @FXML private Button btnCancelar;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private FormaPagamentoService formaPagamentoService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    private ChangeListener<String> tefStatusListener;

    @FXML
    private void initialize() {
        String forma = formaPagamentoService.getFormaPagamento();
        BigDecimal valor = leitorService.getTicketAtual().getFinalValue();

        labelFormaEscolhida.setText(forma != null ? forma : "Forma não definida");
        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        this.tefStatusListener = (obs, oldStatus, newStatus) -> {
            Platform.runLater(() -> {
                boolean shouldNavigate = false;
                String destination = "";

                switch (newStatus) {
                    case "SERVER_CONNECTED":
                        destination = "/fxml/tela_servidor_conectado.fxml";
                        shouldNavigate = true;
                        break;
                    case "ERROR":
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
        pagamentoTEFService.iniciarProcessoCompletoDePagamento(valor, forma);
    }
}