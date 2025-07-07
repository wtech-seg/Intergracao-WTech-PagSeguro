package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class TelaImpressaoController {

    @FXML private AnchorPane logoContainer;
    @FXML private Label labelValorTotal;

    @Autowired
    private LeitorService leitorService;

    @Autowired
    private NavegacaoUtil navegaPara;

    @FXML
    private void initialize() {
        System.out.println("Tela de Impressão carregada. Registrando data de saída...");

        leitorService.finalizarTicketComDataDeLeitura();

        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        } else {
            System.err.println("imgLogo não encontrado!");
        }

        labelValorTotal.setText(leitorService.getValorTotalFormatado());
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Passando de página");
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }
}