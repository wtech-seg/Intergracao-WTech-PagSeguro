package br.com.wtech.totem.controller;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.service.ImpressaoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelaImpressaoController {

    @FXML private AnchorPane root;
    @FXML private Label labelValorTotal;

    @Autowired private ImpressaoService impressaoService;
    @Autowired private LeitorService leitorService;
    @Autowired private NavegacaoUtil navegaPara;

    @FXML
    private void initialize() {
        System.out.println("TELA DE IMPRESSÃO: Iniciando processos finais...");

        Ticket ticketAtual = leitorService.getTicketAtual();

        // Garante que o valor da compra ainda seja exibido nesta tela.
        if (labelValorTotal != null) {
            labelValorTotal.setText(leitorService.getValorTotalFormatado());
        }

        // Executa as operações de finalização no banco de dados.
        // É importante fazer isso antes de limpar os dados do ticket.
        impressaoService.registrarOperacoesDeImpressao();

        if (ticketAtual.getTipoPagamento() != 6) {
            leitorService.finalizarTicketComDataDeLeitura();
        } else {
            System.out.println("TELA DE IMPRESSÃO: Pulando finalização do ticket pois o tipo de pagamento é 6.");
        }

        // Inicia o contador para retornar automaticamente à tela inicial.
        retornarAoInicioAposDelay();
    }

    /**
     * Simula a impressão e, após um atraso, limpa o estado do sistema
     * e volta para a tela inicial, preparando para o próximo cliente.
     */
    private void retornarAoInicioAposDelay() {
        System.out.println("CUPOM IMPRESSO (simulação). Voltando ao início em 5 segundos.");

        // Cria uma pausa de 5 segundos para o usuário ter tempo de ver a mensagem.
        PauseTransition delay = new PauseTransition(Duration.seconds(5));

        // Define a ação a ser executada quando a pausa terminar.
        delay.setOnFinished(event -> {
            System.out.println("Limpando dados da sessão para o próximo cliente.");

            // Limpa os dados do ticket atual para que o próximo cliente comece do zero.
            leitorService.limparTicketAtual();

            // Navega de volta para a tela inicial.
            navegaPara.trocaTela("/fxml/tela_inicial.fxml", root);
        });

        // Inicia a contagem da pausa.
        delay.play();
    }
}