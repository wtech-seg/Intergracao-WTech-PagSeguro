package br.com.wtech.totem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class TelaProcessandoController {

    @FXML
    private ImageView imgLogo;

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");

        String fxmlPath = "/fxml/tela_nota_fiscal.fxml";
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println("Não encontrou: " + fxmlPath);
            return;
        }

        try {
            Parent telaNotaFiscal = new FXMLLoader(url).load();
            Scene sceneNotaFiscal = new Scene(telaNotaFiscal);

            Stage stage = (Stage) imgLogo.getScene().getWindow();
            stage.setScene(sceneNotaFiscal);
            stage.setFullScreen(true);

            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar " + fxmlPath);
            e.printStackTrace();
        }
    }
}