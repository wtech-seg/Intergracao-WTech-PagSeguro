package br.com.wtech.totem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class TelaImpressaoController {

    @FXML private AnchorPane logoContainer;

    @FXML
    private void initialize() {
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        } else {
            System.err.println("imgLogo não encontrado!");
        }
    }

    private void navegaPara(String fxmlPath, Node anyNode) {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println("Não encontrou: " + fxmlPath);
            return;
        }
        try {
            Parent tela = new FXMLLoader(url).load();
            Scene cena = new Scene(tela);
            Stage stage = (Stage) anyNode.getScene().getWindow();
            stage.setScene(cena);
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");
        navegaPara("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }
}