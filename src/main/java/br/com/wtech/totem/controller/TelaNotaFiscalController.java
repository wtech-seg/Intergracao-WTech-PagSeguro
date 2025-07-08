package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class TelaNotaFiscalController {

    @FXML private AnchorPane logoContainer;
    @FXML private HBox cancelarContainer;

    @FXML
    private void initialize() {
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        } else {
            System.err.println("imgLogo não encontrado!");
        }

        Button btnCancelar = (Button) cancelarContainer.lookup("#btnCancelar");
        if (btnCancelar != null) {
            btnCancelar.setOnAction(this::handleCancelar);
        } else {
            System.err.println("btnCancelar não encontrado!");
        }
    }

    @Autowired
    private NavegacaoUtil navegaPara;

    @Autowired
    private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");
        navegaPara.trocaTela("/fxml/tela_forma_pagamento.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("Voltando para a tela inicial");
        pagamentoTEFService.solicitarCancelamento();
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
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
