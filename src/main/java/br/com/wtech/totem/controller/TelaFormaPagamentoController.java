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

public class TelaFormaPagamentoController {

    @FXML
    private ImageView imgLogo;

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");

        String fxmlPath = "/fxml/tela_forma_escolhida.fxml";
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println("Não encontrou: " + fxmlPath);
            return;
        }

        try {
            Parent telaFormaEscolhida = new FXMLLoader(url).load();
            Scene sceneFormaEscolhida = new Scene(telaFormaEscolhida);

            Stage stage = (Stage) imgLogo.getScene().getWindow();
            stage.setScene(sceneFormaEscolhida);
            stage.setFullScreen(true);

            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar(javafx.event.Event event) {
        System.out.println("Voltando para a tela inicial");

        String fxmlPath = "/fxml/tela_inicial.fxml";
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println("Não encontrou: " + fxmlPath);
            return;
        }

        try {
            Parent telaInicial = new FXMLLoader(url).load();
            Scene sceneInicial = new Scene(telaInicial);

            Stage stage = (Stage) imgLogo.getScene().getWindow();
            stage.setScene(sceneInicial);
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDebito(javafx.event.Event event) {
        System.out.println("Botão Débito apertado");
        // aqui você pode chamar outra tela ou lógica
    }

    @FXML
    private void handleCredito(javafx.event.Event event) {
        System.out.println("Botão Crédito apertado");
        // aqui você pode chamar outra tela ou lógica
    }

    @FXML
    private void handlePix(javafx.event.Event event) {
        System.out.println("Botão Pix apertado");
        // aqui você pode chamar outra tela ou lógica
    }
}
