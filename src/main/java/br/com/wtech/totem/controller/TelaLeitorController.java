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
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import javafx.util.Duration;

@Component
public class TelaLeitorController {

    // AJUSTE: Injetamos o Node que corresponde ao fx:id="logoContainer" do seu <fx:include>
    @FXML private Node logoContainer;
    @FXML private TextField inputLeitura;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {
        // AJUSTE: Usamos o lookup de forma segura, com verificação de nulo.
        if (logoContainer != null) {
            // Procura pelo nó com o ID "imgLogo" DENTRO do componente incluído.
            Node imgLogo = logoContainer.lookup("#imgLogo");
            if (imgLogo != null) {
                imgLogo.setOnMouseClicked(this::handleLogoClick);
            } else {
                System.err.println("AVISO: ImageView com fx:id='imgLogo' não foi encontrada dentro do componente logo.fxml.");
            }
        }

        Platform.runLater(() -> inputLeitura.requestFocus());
    }
    /**
     * Este método trata o clique na logo para testar o CANCELAMENTO/ESTORNO.
     */
    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("--- LOGO CLICADO: INICIANDO TESTE DE ESTORNO ---");
        String nsuParaCancelar = pagamentoTEFService.getUltimoNsuParaReimpressao();

        if (nsuParaCancelar == null || nsuParaCancelar.isBlank()) {
            System.err.println("Nenhum NSU armazenado para estornar. Realize uma transação aprovada primeiro.");
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Nenhuma transação anterior encontrada para estornar.").show());
            return;
        }

        pagamentoTEFService.iniciarCancelamentoAdministrativo(nsuParaCancelar);
    }
    /**
     * Este método trata o clique na logo para testar a reimpressão.
     */
    @FXML
    private void handleLogoClickOff(MouseEvent event) {
        System.out.println("--- LOGO CLICADO: INICIANDO TESTE DE REIMPRESSÃO ---");
        String nsuParaTeste = pagamentoTEFService.getUltimoNsuParaReimpressao();

        if (nsuParaTeste == null || nsuParaTeste.isBlank()) {
            System.err.println("Nenhum NSU armazenado para reimpressão. Realize uma transação aprovada primeiro.");
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Nenhuma transação anterior encontrada para reimprimir.").show());
            return;
        }

        pagamentoTEFService.iniciarReimpressao(nsuParaTeste);
    }

    /**
     * Este método trata a leitura de tickets normais.
     */
    @FXML
    private void handleLeitor() {
        // Se o campo já estiver desabilitado (em cooldown), não faz nada.
        if (inputLeitura.isDisabled()) {
            return;
        }

        String valorLido = inputLeitura.getText();
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