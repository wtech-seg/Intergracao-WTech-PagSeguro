package br.com.wtech.totem.controller;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaProcessandoController {

    @FXML private AnchorPane root;
    @FXML private AnchorPane logoContainer;

    @Autowired
    private NavegacaoUtil navegaPara;

    @Autowired
    private LeitorService leitorService;

    @FXML
    private void initialize() {
        if (root != null) {
            root.requestFocus();
        }

        Platform.runLater(this::verificarStatusDoTicketEContinuar);

        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        } else {
            System.err.println("imgLogo não encontrado!");
        }
    }

    /**
     * Contém a nova lógica de verificação de status.
     */
    private void verificarStatusDoTicketEContinuar() {
        Ticket ticket = leitorService.getTicketAtual();

        if (ticket == null) {
            System.err.println("PROCESSANDO: Nenhum ticket ativo. Voltando para a tela inicial.");
            navegaPara.trocaTela("/fxml/tela_inicial.fxml", root);
            return;
        }

        System.out.println("PROCESSANDO: Verificando status do ticket '" + ticket.getTicketCode() + "'. Status atual: " + ticket.getStatus());

        switch (ticket.getStatus()) {
            case 3: // PAGO
            case 4: // Saída Livre
            case 5: // Cortesia
                System.out.println("PROCESSANDO: Ticket liberado (Status " + ticket.getStatus() + "). Redirecionando para a tela de impressão.");
                navegaPara.trocaTela("/fxml/tela_impressao.fxml", root);
                break;

            case 2: // JÁ UTILIZADO
                System.err.println("PROCESSANDO: Ticket já utilizado (Status 2). Voltando para a tela inicial.");
                // Você pode adicionar um Label na sua tela para mostrar esta mensagem ao usuário
                PauseTransition delayUtilizado = new PauseTransition(Duration.seconds(3));
                delayUtilizado.setOnFinished(event -> {
                    leitorService.limparTicketAtual();
                    navegaPara.trocaTela("/fxml/tela_inicial.fxml", root);
                });
                delayUtilizado.play();
                break;

            case 6: // CANCELADO
                System.err.println("PROCESSANDO: Ticket cancelado (Status 6). Voltando para a tela inicial.");
                // Você pode adicionar um Label na sua tela para mostrar esta mensagem ao usuário
                PauseTransition delayCancelado = new PauseTransition(Duration.seconds(3));
                delayCancelado.setOnFinished(event -> {
                    leitorService.limparTicketAtual();
                    navegaPara.trocaTela("/fxml/tela_inicial.fxml", root);
                });
                delayCancelado.play();
                break;

            default: // ABERTO (pronto para pagar)
                System.out.println("PROCESSANDO: Ticket válido para pagamento (Status " + ticket.getStatus() + "). Indo para a tela de forma de pagamento.");
                navegaPara.trocaTela("/fxml/tela_forma_pagamento.fxml", root);
                break;
        }
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");
        navegaPara.trocaTela("/fxml/tela_nota_fiscal.fxml", (Node) event.getSource());
    }
}