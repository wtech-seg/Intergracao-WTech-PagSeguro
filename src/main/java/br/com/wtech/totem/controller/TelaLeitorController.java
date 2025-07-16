package br.com.wtech.totem.controller;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import javafx.util.Duration;

@Component
public class TelaLeitorController {

    // Injetamos o Node do componente do logo e a ImageView da barra diretamente
    @FXML private Node logoContainer;
    @FXML private ImageView imgBarra;
    @FXML private TextField inputLeitura;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {
        // Liga o clique na LOGO para o teste de ESTORNO
        if (logoContainer != null) {
            Node imgLogo = logoContainer.lookup("#imgLogo");
            if (imgLogo != null) {
                imgLogo.setOnMouseClicked(this::handleLogoClick);
            }
        }

        // AJUSTE: Liga o clique na BARRA para o teste de REIMPRESSÃO
        if (imgBarra != null) {
            imgBarra.setOnMouseClicked(this::handleImgClick);
        }

        Platform.runLater(() -> inputLeitura.requestFocus());
    }

    /**
     * MÉTODO ATIVO (LOGO): Trata o clique na logo para testar o CANCELAMENTO/ESTORNO.
     */
    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("--- LOGO CLICADO: INICIANDO TESTE DE ESTORNO ---");
        String nsuParaCancelar = pagamentoTEFService.getUltimoNsuParaReimpressao();

        if (nsuParaCancelar == null || nsuParaCancelar.isBlank()) {
            System.err.println("Nenhum NSU armazenado para estornar.");
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Nenhuma transação anterior encontrada para estornar.").show());
            return;
        }

        pagamentoTEFService.iniciarCancelamentoAdministrativo(nsuParaCancelar);
    }

    /**
     * MÉTODO ATIVO (BARRA): Trata o clique na barra para testar a REIMPRESSÃO.
     */
    private void handleImgClick(MouseEvent event) {
        System.out.println("--- BARRA CLICADA: INICIANDO TESTE DE REIMPRESSÃO ---");
        String nsuParaTeste = pagamentoTEFService.getUltimoNsuParaReimpressao();

        if (nsuParaTeste == null || nsuParaTeste.isBlank()) {
            System.err.println("Nenhum NSU armazenado para reimprimir.");
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Nenhuma transação anterior encontrada para reimprimir.").show());
            return;
        }

        pagamentoTEFService.iniciarReimpressao(nsuParaTeste);
    }

    /**
     * Este método trata a leitura de tickets normais (fluxo de pagamento).
     */
    @FXML
    private void handleLeitor() {
        if (inputLeitura.isDisabled()) { return; }
        String valorLido = inputLeitura.getText();
        if (valorLido == null || valorLido.isBlank()) { return; }
        inputLeitura.setDisable(true);

        try {
            Ticket ticketEncontrado = leitorService.buscarTicket(valorLido);
            leitorService.setTicketAtual(ticketEncontrado);
            navegaPara.trocaTela("/fxml/tela_processando.fxml", inputLeitura);
        } catch (EmptyResultDataAccessException e) {
            System.err.println("TICKET NÃO ENCONTRADO: O código '" + valorLido + "' não é válido.");
            iniciarCooldownParaNovaLeitura();
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado ao buscar o ticket.");
            e.printStackTrace();
            iniciarCooldownParaNovaLeitura();
        }
    }

    private void iniciarCooldownParaNovaLeitura() {
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            inputLeitura.clear();
            inputLeitura.setDisable(false);
            inputLeitura.requestFocus();
        });
        delay.play();
    }
}