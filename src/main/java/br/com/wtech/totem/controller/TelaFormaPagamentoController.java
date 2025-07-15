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
     * Agora também recebe o código do tipo de pagamento.
     */
    private void iniciarFluxoDePagamento(String forma, int tipoPagamentoCodigo, Node sourceNode) {
        System.out.println("Forma de pagamento escolhida: " + forma + " (Código: " + tipoPagamentoCodigo + ")");

        // 1. Atualiza o tipo de pagamento no banco de dados.
        formaPagamentoService.atualizarTipoPagamentoNoBanco(tipoPagamentoCodigo);

        // 2. Salva o nome da forma de pagamento no serviço para uso posterior.
        formaPagamentoService.setFormaPagamento(forma);

        // 3. Navega para a primeira tela de aguarde.
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", sourceNode);
    }

    /**
     * AJUSTE: Passa o código 1 para Débito.
     */
    @FXML
    private void handleDebito(ActionEvent event) {
        iniciarFluxoDePagamento("Débito", 1, (Node) event.getSource());
    }

    /**
     * AJUSTE: Passa o código 1 para Crédito.
     */
    @FXML
    private void handleCredito(ActionEvent event) {
        iniciarFluxoDePagamento("Crédito", 1, (Node) event.getSource());
    }

    /**
     * AJUSTE: Passa o código 2 para Pix.
     */
    @FXML
    private void handlePix(ActionEvent event) {
        iniciarFluxoDePagamento("Pix", 2, (Node) event.getSource());
    }
}