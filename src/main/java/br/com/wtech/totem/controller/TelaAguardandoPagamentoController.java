package br.com.wtech.totem.controller;

// 1. ADICIONAR NOVOS IMPORTS
import br.com.wtech.totem.service.FormaPagamentoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.service.ResultadoTEF;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class TelaAguardandoPagamentoController {

    // 2. ADICIONAR O ELEMENTO RAIZ
    @FXML private AnchorPane root;
    @FXML private AnchorPane logoContainer;
    @FXML private HBox cancelarContainer;
    @FXML private Label labelValorTotal;

    // 3. INJETAR TODOS OS SERVIÇOS NECESSÁRIOS
    @Autowired private LeitorService leitorService;
    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private FormaPagamentoService formaPagamentoService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {
        // --- Sua lógica existente é mantida ---
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        }

        Button btnCancelar = (Button) cancelarContainer.lookup("#btnCancelar");
        if (btnCancelar != null) {
            btnCancelar.setOnAction(this::handleCancelar);
        }

        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        // 4. ADIÇÃO DA LÓGICA DE PAGAMENTO EM SEGUNDO PLANO
        iniciarProcessoDePagamento();
    }

    private void iniciarProcessoDePagamento() {
        BigDecimal valor = leitorService.getTicketAtual().getFinalValue();
        String formaPagamento = formaPagamentoService.getFormaPagamento();

        Task<ResultadoTEF> taskPagamento = new Task<>() {
            @Override
            protected ResultadoTEF call() throws Exception {
                return pagamentoTEFService.iniciarPagamento(valor, formaPagamento);
            }
        };

        // QUANDO A TAREFA TERMINAR (COM SUCESSO OU FALHA DE COMUNICAÇÃO)
        taskPagamento.setOnSucceeded(e -> {
            // Não importa o resultado, apenas navega para a tela de confirmação.
            Platform.runLater(() -> {
                navegaPara.trocaTela("/fxml/tela_pagamento_selecionado.fxml", root);
            });
        });

        taskPagamento.setOnFailed(e -> {
            taskPagamento.getException().printStackTrace();
            // Mesmo em caso de erro de comunicação, vamos para a tela de resultado.
            Platform.runLater(() -> {
                navegaPara.trocaTela("/fxml/tela_pagamento_selecionado.fxml", root);
            });
        });

        new Thread(taskPagamento).start();
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        // ATENÇÃO: Permitir navegação durante um pagamento pode ser arriscado.
        // Considere desabilitar este botão nesta tela.
        System.out.println("Passando de página");
        navegaPara.trocaTela("/fxml/tela_pagamento_selecionado.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("Voltando para a tela inicial");
        pagamentoTEFService.solicitarCancelamento();
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }
}