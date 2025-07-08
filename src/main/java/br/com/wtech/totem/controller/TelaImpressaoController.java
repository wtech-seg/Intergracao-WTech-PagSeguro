package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition; // Importe a classe PauseTransition
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration; // Importe a classe Duration
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaImpressaoController {

    @FXML private AnchorPane root; // Adicione o fx:id="root" ao seu FXML
    @FXML private AnchorPane logoContainer;
    @FXML private Label labelValorTotal;

    @Autowired
    private LeitorService leitorService;

    @Autowired
    private NavegacaoUtil navegaPara;

    @FXML
    private void initialize() {
        System.out.println("TELA DE IMPRESSÃO: Registrando data de saída no banco...");

        // 1. Finaliza o ticket no banco de dados (como você já fazia)
        leitorService.finalizarTicketComDataDeLeitura();

        // 2. Exibe o valor final na tela (como você já fazia)
        labelValorTotal.setText(leitorService.getValorTotalFormatado());

        // Simula a impressão e retorna ao início automaticamente
        retornarAoInicioAposDelay();

        // Mantém o clique na logo como um atalho para testes
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        } else {
            System.err.println("imgLogo não encontrado!");
        }
    }

    /**
     * Simula um tempo para impressão e depois limpa os dados e volta à tela inicial.
     */
    private void retornarAoInicioAposDelay() {
        System.out.println("Imprimindo cupom (simulação)... Aguardando 5 segundos.");
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> {
            voltarParaOInicio(root);
        });
        delay.play();
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("Atalho de teste: Voltando para a tela inicial...");
        voltarParaOInicio((Node) event.getSource());
    }

    /**
     * Centraliza a lógica de limpeza de dados e navegação para a tela inicial.
     */
    private void voltarParaOInicio(Node noDeReferencia) {
        // 3. PASSO CRÍTICO: Limpa os dados do ticket anterior
        System.out.println("Limpando dados do ticket anterior.");
        leitorService.limparTicketAtual();

        // Navega de volta para o começo
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", noDeReferencia);
    }
}