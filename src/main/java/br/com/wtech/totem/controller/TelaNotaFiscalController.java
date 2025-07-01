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

public class TelaNotaFiscalController {

    @FXML
    private ImageView imgLogo;
    
    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");

        String fxmlPath = "/fxml/tela_forma_pagamento.fxml";
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println("Não encontrou: " + fxmlPath);
            return;
        }

        try {
            Parent telaFormaPagamento = new FXMLLoader(url).load();
            Scene sceneFormaPagamento = new Scene(telaFormaPagamento);

            Stage stage = (Stage) imgLogo.getScene().getWindow();
            stage.setScene(sceneFormaPagamento);
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
    private void handlePessoaFisica(javafx.event.Event event) {
        System.out.println("Botão Pessoa Física apertado");
        // aqui você pode chamar outra tela ou lógica
    }

    @FXML
    private void handlePessoaJuridica(javafx.event.Event event) {
        System.out.println("Botão Pessoa Jurídica apertado");
        // aqui você pode chamar outra tela ou lógica
    }

    @FXML
    private void handleConfirmar(javafx.event.Event event) {
        System.out.println("Botão Confirmar apertado");
        // lógica de confirmação
    }
}
