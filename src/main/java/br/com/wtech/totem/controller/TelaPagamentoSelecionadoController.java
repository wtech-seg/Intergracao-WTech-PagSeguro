package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.FormaPagamentoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.service.ResultadoTEF;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaPagamentoSelecionadoController {

    @FXML private AnchorPane root;
    @FXML private Label labelStatus;
    @FXML private Label labelDetalhes;
    @FXML private Label labelValorTotal;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private FormaPagamentoService formaPagamentoService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {
        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        ResultadoTEF resultado = pagamentoTEFService.getUltimoResultado();

        // Verifica o resultado da transação para decidir o que mostrar e fazer
        if (resultado != null && resultado.isAprovado()) {
            processarSucesso();
        } else {
            processarRecusa();
        }
    }

    /**
     * Lida com o cenário de pagamento APROVADO.
     */
    private void processarSucesso() {
        labelStatus.setText("APROVADO");
        labelDetalhes.setText("Pagamento com " + formaPagamentoService.getFormaPagamento());

        // Espera 3 segundos e avança para a impressão
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            navegaPara.trocaTela("/fxml/tela_impressao.fxml", root);
        });
        delay.play();
    }

    /**
     * Lida com o cenário de pagamento RECUSADO ou com erro.
     */
    private void processarRecusa() {
        labelStatus.setText("RECUSADO");
        labelDetalhes.setText("Por favor, tente novamente ou escolha outra opção.");

        // Espera 4 segundos e volta para a tela de escolha de pagamento
        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> {
            navegaPara.trocaTela("/fxml/tela_forma_pagamento.fxml", root);
        });
        delay.play();
    }
}