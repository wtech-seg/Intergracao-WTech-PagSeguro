package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.FormaPagamentoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaFormaPagamentoController {

    @FXML private Label labelValorTotal;
    @FXML private AnchorPane root;
    @FXML private Button btnDebito;
    @FXML private Button btnCredito;
    @FXML private Button btnPix;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private FormaPagamentoService formaPagamentoService;

    @FXML
    private void initialize() {
        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        // Garante que os botões não mantenham o foco visual após o clique
        if (btnDebito != null) btnDebito.setFocusTraversable(false);
        if (btnCredito != null) btnCredito.setFocusTraversable(false);
        if (btnPix != null) btnPix.setFocusTraversable(false);
    }

    /**
     * Método central para lidar com a escolha do pagamento.
     * @param forma O nome da forma de pagamento (ex: "Débito").
     * @param sourceNode O botão que acionou o evento, para contexto da navegação.
     */
    private void iniciarFluxoDePagamento(String forma, Node sourceNode) {
        System.out.println("Forma de pagamento escolhida: " + forma);

        // 1. Salva a escolha do usuário no serviço apropriado.
        formaPagamentoService.setFormaPagamento(forma);

        // 2. Navega para a primeira tela de aguarde, onde o processo TEF realmente começará.
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", sourceNode);
    }

    @FXML
    private void handleDebito(ActionEvent event) {
        iniciarFluxoDePagamento("Débito", (Node) event.getSource());
    }

    @FXML
    private void handleCredito(ActionEvent event) {
        iniciarFluxoDePagamento("Crédito", (Node) event.getSource());
    }

    @FXML
    private void handlePix(ActionEvent event) {
        iniciarFluxoDePagamento("Pix", (Node) event.getSource());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        // Se o usuário cancelar aqui, simplesmente volta para a tela inicial.
        System.out.println("Voltando para a tela inicial");
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }
}