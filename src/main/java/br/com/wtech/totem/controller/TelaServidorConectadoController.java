package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class TelaServidorConectadoController {

    @FXML private AnchorPane logoContainer;
    @FXML private HBox cancelarContainer;
    @FXML private Label labelValorTotal;

    @Autowired
    private LeitorService leitorService;

    @Autowired
    private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {

        PauseTransition espera = new PauseTransition(Duration.seconds(2));
        espera.setOnFinished(event -> {
            System.out.println("Tempo de espera concluído. Indo para a tela de aguardar pagamento.");
            navegaPara.trocaTela("/fxml/tela_aguardando_pagamento.fxml", logoContainer);
        });
        espera.play();

        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        } else {
            System.err.println("imgLogo não encontrado!");
        }

        Button btnCancelar = (Button) cancelarContainer.lookup("#btnCancelar");
        if (btnCancelar != null) {
            btnCancelar.setOnAction(this::handleCancelar);
            btnCancelar.setFocusTraversable(false);
        } else {
            System.err.println("btnCancelar não encontrado!");
        }

        labelValorTotal.setText(leitorService.getValorTotalFormatado());
    }

    @Autowired
    private NavegacaoUtil navegaPara;

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");
        navegaPara.trocaTela("/fxml/tela_aguardando_pagamento.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("Voltando para a tela inicial");
        pagamentoTEFService.solicitarCancelamento();
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }
}