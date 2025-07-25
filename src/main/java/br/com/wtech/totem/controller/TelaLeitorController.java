package br.com.wtech.totem.controller;

import br.com.wtech.totem.entity.Ticket;
import br.com.wtech.totem.service.ImpressaoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.service.ResultadoTEF;
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
import java.util.function.Consumer;

@Component
public class TelaLeitorController {

    @FXML private Node logoContainer;
    @FXML private ImageView imgBarra;
    @FXML private TextField inputLeitura;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private PagamentoTEFService pagamentoTEFService;
    @Autowired private ImpressaoService impressaoService;

    // Variável para guardar o código do último ticket lido com sucesso
    private String ultimoTicketCodeLido;

    @FXML
    private void initialize() {
        if (logoContainer != null) {
            Node imgLogo = logoContainer.lookup("#imgLogo");
            if (imgLogo != null) {
                imgLogo.setOnMouseClicked(this::handleLogoClick);
            }
        }
        if (imgBarra != null) {
            imgBarra.setOnMouseClicked(this::handleImgClick);
        }
        Platform.runLater(() -> inputLeitura.requestFocus());
    }

    /**
     * Usa o NSU da última transação e o CÓDIGO do último ticket lido para o ESTORNO.
     */
    @FXML
    private void handleLogoClick(MouseEvent event) {
        System.out.println("--- LOGO CLICADO: INICIANDO TESTE DE ESTORNO ---");

        final String nsuParaCancelar = pagamentoTEFService.getNSUPago();
        final String codigoDoTicket = this.ultimoTicketCodeLido;

        if (codigoDoTicket == null || codigoDoTicket.isBlank()) {
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Leia um ticket válido antes de tentar o estorno.").show());
            return;
        }
        if (nsuParaCancelar == null || nsuParaCancelar.isBlank()) {
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Nenhuma transação anterior encontrada para estornar.").show());
            return;
        }

        // Define o que fazer QUANDO o estorno terminar
        Consumer<ResultadoTEF> acaoAoFinalizar = (resultado) -> {
            if (resultado != null && resultado.isAprovado()) {
                System.out.println("Estorno TEF bem-sucedido. Registrando no banco para o ticket: " + codigoDoTicket);
                impressaoService.registrarOperacoesDeCancelamento(codigoDoTicket);
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Transação estornada com sucesso!").show());
            } else {
                System.err.println("Falha no estorno TEF.");
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Falha ao estornar transação.").show());
            }
        };

        // AJUSTE: Passa o callback como segundo argumento para o serviço.
        pagamentoTEFService.iniciarCancelamentoAdministrativo(acaoAoFinalizar);
    }

    /**
     * Usa o NSU da última transação e o CÓDIGO do último ticket lido para a REIMPRESSÃO.
     */
    private void handleImgClick(MouseEvent event) {
        System.out.println("--- BARRA CLICADA: INICIANDO TESTE DE REIMPRESSÃO ---");

        final String nsuParaReimprimir = pagamentoTEFService.getNSUPago();
        final String codigoDoTicket = this.ultimoTicketCodeLido;

        if (codigoDoTicket == null || codigoDoTicket.isBlank()) {
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Leia um ticket válido antes de tentar a reimpressão.").show());
            return;
        }
        if (nsuParaReimprimir == null || nsuParaReimprimir.isBlank()) {
            Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Nenhuma transação anterior encontrada para reimprimir.").show());
            return;
        }

        Consumer<ResultadoTEF> acaoAoFinalizar = (resultado) -> {
            if (resultado != null && resultado.isAprovado()) {
                System.out.println("Reimpressão TEF bem-sucedida. Registrando no banco para o ticket: " + codigoDoTicket);
                impressaoService.registrarOperacoesDeReimpressao(codigoDoTicket);
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Reimpressão concluída.").show());
            } else {
                System.err.println("Falha na reimpressão TEF.");
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Falha ao reimprimir.").show());
            }
        };

        // AJUSTE: Passa o callback como segundo argumento para o serviço.
        pagamentoTEFService.iniciarReimpressao(acaoAoFinalizar);
    }

    /**
     * Este método agora guarda o código do ticket lido para uso posterior.
     */
    @FXML
    private void handleLeitor() {
        if (inputLeitura.isDisabled()) { return; }
        String valorLido = inputLeitura.getText();
        if (valorLido == null || valorLido.isBlank()) { return; }
        inputLeitura.setDisable(true);

        if (leitorService.isTicketVinculadoAPessoa(valorLido)) {
            // --- CAMINHO 1: TICKET VINCULADO (FLUXO DIRETO) ---
            System.out.println("FLUXO VINCULADO: Ticket '" + valorLido + "' pertence a um mensalista. Finalizando...");

            try {
                Ticket ticketEncontrado = leitorService.buscarTicket(valorLido);
                leitorService.setTicketAtual(ticketEncontrado);

                // Criamos um "Resultado Falso" de pagamento aprovado para a tela de resultado entender.
                ResultadoTEF resultadoVinculado = new ResultadoTEF(true, "APROVADO", "MENSALISTA - SAÍDA LIBERADA");
                pagamentoTEFService.setUltimoResultado(resultadoVinculado);

                // Navega direto para a tela de pagamento selecionado (que mostrará "APROVADO")
                navegaPara.trocaTela("/fxml/tela_processando.fxml", inputLeitura);

            } catch (Exception e) {
                System.err.println("Erro ao processar ticket vinculado: " + e.getMessage());
                iniciarCooldownParaNovaLeitura();
            }
        } else {
            // --- CAMINHO 2: TICKET NÃO VINCULADO (FLUXO DE PAGAMENTO NORMAL QUE JÁ FUNCIONAVA) ---
            System.out.println("FLUXO NORMAL: Ticket '" + valorLido + "' não vinculado. Indo para a tela de pagamento.");
            try {
                Ticket ticketEncontrado = leitorService.buscarTicket(valorLido);
                this.ultimoTicketCodeLido = ticketEncontrado.getTicketCode();
                leitorService.setTicketAtual(ticketEncontrado);
                navegaPara.trocaTela("/fxml/tela_processando.fxml", inputLeitura);
            } catch (EmptyResultDataAccessException e) {
                this.ultimoTicketCodeLido = null;
                iniciarCooldownParaNovaLeitura();
            } catch (Exception e) {
                this.ultimoTicketCodeLido = null;
                e.printStackTrace();
                iniciarCooldownParaNovaLeitura();
            }
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