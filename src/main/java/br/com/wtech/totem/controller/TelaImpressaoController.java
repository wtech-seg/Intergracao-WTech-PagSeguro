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

        if (ticketAtual == null) {
            System.err.println("ERRO: Tela de impressão acessada sem um ticket ativo.");
            retornarAoInicioAposDelay();
            return;
        }

        if (labelValorTotal != null) {
            labelValorTotal.setText(leitorService.getValorTotalFormatado());
        }

        // --- AJUSTE FINAL, CONFORME SOLICITADO ---
        // 1. Verifica se o ticket pertence a um mensalista válido.
        if (leitorService.isTicketVinculadoAPessoa(ticketAtual.getTicketCode())) {
            // --- CAMINHO 1: FLUXO DE MENSALISTA ---
            System.out.println("TELA DE IMPRESSÃO: Ticket de mensalista detectado. Inativando acesso.");

            impressaoService.registrarOperacoesDeImpressaoMensalista();
        } else {
            // --- CAMINHO 2: FLUXO DE PAGAMENTO NORMAL ---
            System.out.println("TELA DE IMPRESSÃO: Ticket de pagamento normal. Registrando operações de impressão.");

            // A lógica de impressão normal que já funcionava.
            impressaoService.registrarOperacoesDeImpressao();
            leitorService.finalizarTicketComDataDeLeitura();
        }

        retornarAoInicioAposDelay();
    }

    private void retornarAoInicioAposDelay() {
        System.out.println("CUPOM IMPRESSO (simulação). Voltando ao início em 5 segundos.");
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> {
            System.out.println("Limpando dados da sessão para o próximo cliente.");
            leitorService.limparTicketAtual();
            navegaPara.trocaTela("/fxml/tela_inicial.fxml", root);
        });
        delay.play();
    }
}