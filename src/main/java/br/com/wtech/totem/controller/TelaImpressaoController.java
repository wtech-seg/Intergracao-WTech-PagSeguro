package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.ImpressaoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.application.Platform; // Adicione este import
import javafx.fxml.FXML;
import javafx.scene.control.Label; // Adicionado para o valor total
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaImpressaoController {

    @FXML private AnchorPane root;
    @FXML private Label labelValorTotal; // Adicionado para exibir o valor

    @Autowired private ImpressaoService impressaoService;
    @Autowired private LeitorService leitorService;
    @Autowired private NavegacaoUtil navegaPara;

    @FXML
    private void initialize() {
        System.out.println("TELA DE IMPRESSÃO: Iniciando processos finais...");

        // Executa as operações de banco de dados
        impressaoService.registrarOperacoesDeImpressao();
        leitorService.finalizarTicketComDataDeLeitura();

        // Exibe o valor na tela
        if(labelValorTotal != null) {
            labelValorTotal.setText(leitorService.getValorTotalFormatado());
        }

        // Inicia o retorno automático para a tela inicial
        retornarAoInicioAposDelay();
    }

    private void retornarAoInicioAposDelay() {
        System.out.println("CUPOM IMPRESSO (simulação). Voltando ao início em 5 segundos.");
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> {
            // CORREÇÃO: Envolve a navegação no Platform.runLater
            Platform.runLater(() -> {
                leitorService.limparTicketAtual();
                navegaPara.trocaTela("/fxml/tela_inicial.fxml", root);
            });
        });
        delay.play();
    }
}