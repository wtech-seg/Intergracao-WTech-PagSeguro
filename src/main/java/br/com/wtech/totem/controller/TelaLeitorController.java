package br.com.wtech.totem.controller;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition; // Importe a classe PauseTransition
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration; // Importe a classe Duration
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

@Component
public class TelaLeitorController {

    @FXML private AnchorPane logoContainer;
    @FXML private HBox cancelarContainer;
    @FXML private TextField inputLeitura;

    @Autowired
    private NavegacaoUtil navegaPara;

    @Autowired
    private LeitorService leitorService;

    @Autowired
    private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {
        // ... seu código initialize permanece o mesmo ...
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        }

        Button btnCancelar = (Button) cancelarContainer.lookup("#btnCancelar");
        if (btnCancelar != null) {
            btnCancelar.setOnAction(this::handleCancelar);
        }

        Platform.runLater(() -> inputLeitura.requestFocus());
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        navegaPara.trocaTela("/fxml/tela_processando.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("Voltando para a tela inicial");
        pagamentoTEFService.solicitarCancelamento();
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleLeitor() {
        // Se o campo já estiver desabilitado (em cooldown), não faz nada.
        if (inputLeitura.isDisabled()) {
            return;
        }

        String valorLido = "311";
        if (valorLido == null || valorLido.isBlank()) {
            return;
        }

        // 1. Desabilita o campo IMEDIATAMENTE para evitar novas leituras
        inputLeitura.setDisable(true);
        System.out.println("Valor lido do QR/Leitor: " + valorLido);

        try {
            Ticket ticketEncontrado = leitorService.buscarTicket(valorLido);
            System.out.println("Ticket encontrado: " + ticketEncontrado.getTicketCode());

            // AQUI ESTÁ A MUDANÇA: Guardamos o objeto inteiro no serviço
            leitorService.setTicketAtual(ticketEncontrado);

            // Navega para a próxima tela (que pode ser a de pagamento)
            navegaPara.trocaTela("/fxml/tela_processando.fxml", inputLeitura);

        } catch (EmptyResultDataAccessException e) {
            // 2. SUBSTITUI O ALERT POR UMA MENSAGEM NO CONSOLE
            System.err.println("TICKET NÃO ENCONTRADO: O código '" + valorLido + "' não é válido.");

            // 3. INICIA O DELAY MESMO SE DER ERRO
            iniciarCooldownParaNovaLeitura();

        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado ao buscar o ticket.");
            e.printStackTrace();
            iniciarCooldownParaNovaLeitura();
        }
    }

    /**
     * Método que cria uma pausa de 2 segundos e depois limpa e reabilita o campo.
     */
    private void iniciarCooldownParaNovaLeitura() {
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            inputLeitura.clear();
            inputLeitura.setDisable(false);
            inputLeitura.requestFocus(); // Foca no campo novamente, pronto para o próximo scan
            System.out.println("--- Sistema pronto para nova leitura ---");
        });
        delay.play();
    }
}