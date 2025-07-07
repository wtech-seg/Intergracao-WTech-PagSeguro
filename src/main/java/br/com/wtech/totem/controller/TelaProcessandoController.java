package br.com.wtech.totem.controller;

import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class TelaProcessandoController {

    @FXML private AnchorPane logoContainer;

    @FXML
    private void initialize() {
        // Espera 2 segundos e navega para a tela de forma de pagamento
        PauseTransition espera = new PauseTransition(Duration.seconds(2));
        espera.setOnFinished(event -> {
            System.out.println("Tempo de espera concluído. Indo para a tela de forma de pagamento.");
            navegaPara.trocaTela("/fxml/tela_forma_pagamento.fxml", logoContainer);
        });
        espera.play();

        // Configura clique no logo (se ainda quiser manter)
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        } else {
            System.err.println("imgLogo não encontrado!");
        }
    }

    @Autowired
    private NavegacaoUtil navegaPara;

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");
        navegaPara.trocaTela("/fxml/tela_nota_fiscal.fxml", (Node) event.getSource());
    }
}