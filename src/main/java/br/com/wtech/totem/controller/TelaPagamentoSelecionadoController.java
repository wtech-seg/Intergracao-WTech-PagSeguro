package br.com.wtech.totem.controller;

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
        } else {
            // Se o resultado for nulo ou não aprovado, trata como recusa/erro.
            processarRecusa(resultado);
        }
    }

    /**
     * Lida com o cenário de pagamento APROVADO.
     */
    private void processarSucesso() {
        labelStatus.setText("APROVADO");
        labelDetalhes1.setText("Obrigado! Retire seu comprovante.");

        // MANTIDO: Atualiza o status do ticket no banco de dados.
        leitorService.atualizarStatusParaPago();

        // Pausa de 3 segundos para o usuário ler a mensagem e depois avança para a impressão.
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            navegaPara.trocaTela("/fxml/tela_impressao.fxml", root);
        });
        delay.play();
    }

    /**
     * Lida com o cenário de pagamento RECUSADO ou com erro.
     */
    private void processarRecusa(ResultadoTEF resultado) {
        labelStatus.setText("NÃO AUTORIZADO");
        // Mostra o motivo, se houver, ou uma mensagem padrão.
        String detalhes = resultado != null ? resultado.getMensagemDetalhada() : "Tente novamente.";
        labelDetalhes1.setText("Por favor, tente outra forma de pagamento.");
        labelDetalhes2.setText(detalhes);

        // Pausa de 4 segundos e volta para a tela de escolha de pagamento.
        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> {
            navegaPara.trocaTela("/fxml/tela_forma_pagamento.fxml", root);
        });
        delay.play();
    }
}