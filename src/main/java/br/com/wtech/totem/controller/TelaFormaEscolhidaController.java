package br.com.wtech.totem.controller;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.service.*;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.function.Consumer;

@Component
public class TelaFormaEscolhidaController {

    @FXML private AnchorPane root;
    @FXML private Label labelFormaEscolhida;
    @FXML private Label labelValorTotal;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private FormaPagamentoService formaPagamentoService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    // O listener será usado apenas para o fluxo de cartão
    private ChangeListener<String> tefStatusListener;

    @FXML
    private void initialize() {
        String forma = formaPagamentoService.getFormaPagamento();
        BigDecimal valor = leitorService.getTicketAtual().getFinalValue();
        String ticket = leitorService.getTicketAtual().getTicketCode();

        labelFormaEscolhida.setText("Processando pagamento com " + forma + "...");
        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        // --- AJUSTE: LÓGICA HÍBRIDA ---
        if ("Pix".equalsIgnoreCase(forma)) {
            // --- Caminho Expresso para o PIX ---

            // 1. Define o que fazer quando a transação PIX terminar
            Consumer<ResultadoTEF> aoFinalizarPix = (resultado) -> {
                pagamentoTEFService.setUltimoResultado(resultado); // Guarda o resultado
                navegaPara.trocaTela("/fxml/tela_pagamento_selecionado.fxml", root); // Navega para a tela de resultado
            };

            // 2. Inicia o PIX e entrega as instruções
            pagamentoTEFService.iniciarProcessoPix(valor, aoFinalizarPix, ticket);

        } else {
            // --- Caminho Padrão para Cartão (que já funciona) ---

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
                            destination = "/fxml/tela_pagamento_selecionado.fxml";
                            shouldNavigate = true;
                            break;
                        case "CANCELLED":
                            destination = "/fxml/tela_forma_pagamento.fxml";
                            shouldNavigate = true;
                            break;
                    }

                    if (shouldNavigate) {
                        pagamentoTEFService.tefStatusProperty().removeListener(this.tefStatusListener);
                        navegaPara.trocaTela(destination, root);
                    }
                });
            };

            pagamentoTEFService.tefStatusProperty().addListener(this.tefStatusListener);
            pagamentoTEFService.iniciarProcessoCompletoDePagamento(valor, forma, ticket);
        }
    }
}