package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.service.ResultadoTEF;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaPagamentoSelecionadoController {

    @FXML private AnchorPane root;
    @FXML private Label labelStatus;
    @FXML private Label labelDetalhes1;
    @FXML private Label labelDetalhes2;
    @FXML private Label labelValorTotal;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {
        labelValorTotal.setText(leitorService.getValorTotalFormatado());
        ResultadoTEF resultado = pagamentoTEFService.getUltimoResultado();

        if (resultado != null && resultado.isAprovado()) {
            processarSucesso();
        } else if (resultado != null && "CANCELADO".equals(resultado.getStatus())) {
            processarCancelamento();
        } else {
            processarRecusa(resultado);
        }
    }

    private void processarSucesso() {
        labelStatus.setText("APROVADO");
        labelDetalhes1.setText("Obrigado! Retire seu comprovante.");
        leitorService.atualizarStatusParaPago();
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> navegaPara.trocaTela("/fxml/tela_impressao.fxml", root));
        delay.play();
    }

    private void processarRecusa(ResultadoTEF resultado) {
        labelStatus.setText("NÃO AUTORIZADO");
        String detalhes = (resultado != null) ? resultado.getMensagemDetalhada() : "Tente novamente.";
        labelDetalhes1.setText("Por favor, tente outra forma de pagamento.");
        labelDetalhes2.setText(detalhes);
        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> navegaPara.trocaTela("/fxml/tela_forma_pagamento.fxml", root));
        delay.play();
    }

    private void processarCancelamento() {
        labelStatus.setText("CANCELADO");
        labelDetalhes1.setText("A operação foi cancelada.");
        labelDetalhes2.setText("Você será redirecionado em breve.");
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> {
            leitorService.limparTicketAtual();
            pagamentoTEFService.resetStatusParaIdle();
            navegaPara.trocaTela("/fxml/tela_inicial.fxml", root);
        });
        delay.play();
    }

    // NOTA: Para este código funcionar, sua classe ResultadoTEF
    // precisa ter um método getStatus() que retorne a String do status ("CANCELADO", "ERRO", etc.).
}