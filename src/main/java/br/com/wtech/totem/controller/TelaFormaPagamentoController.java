package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.FormaPagamentoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.util.NavegacaoUtil;
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

@Component
public class TelaFormaPagamentoController {

    @FXML private Label labelValorTotal;
    @FXML private AnchorPane logoContainer;
    @FXML private HBox cancelarContainer;
    @FXML private Button btnDebito;
    @FXML private Button btnCredito;
    @FXML private Button btnPix;

    @Autowired
    private NavegacaoUtil navegaPara;

    @Autowired
    private LeitorService leitorService;

    @Autowired
    private FormaPagamentoService formaPagamentoService;

    @FXML
    private void initialize() {
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        }

        Button btnCancelar = (Button) cancelarContainer.lookup("#btnCancelar");
        if (btnCancelar != null) {
            btnCancelar.setOnAction(this::handleCancelar);
            btnCancelar.setFocusTraversable(false);
        }

        if (btnDebito != null) btnDebito.setFocusTraversable(false);
        if (btnCredito != null) btnCredito.setFocusTraversable(false);
        if (btnPix != null) btnPix.setFocusTraversable(false);

        labelValorTotal.setText(leitorService.getValorTotalFormatado());
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleDebito(ActionEvent event) {
        System.out.println("Pagamento com Débito selecionado.");
        formaPagamentoService.atualizarTipoPagamentoNoBanco(1);

        formaPagamentoService.setFormaPagamento("Débito");
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCredito(ActionEvent event) {
        System.out.println("Pagamento com Crédito selecionado.");
        formaPagamentoService.atualizarTipoPagamentoNoBanco(2);

        formaPagamentoService.setFormaPagamento("Crédito");
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }

    @FXML
    private void handlePix(ActionEvent event) {
        System.out.println("Pagamento com PIX selecionado.");
        formaPagamentoService.atualizarTipoPagamentoNoBanco(3);

        formaPagamentoService.setFormaPagamento("Pix");
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }
}