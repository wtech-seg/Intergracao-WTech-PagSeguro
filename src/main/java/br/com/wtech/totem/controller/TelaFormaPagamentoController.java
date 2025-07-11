package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.FormaPagamentoService;
import br.com.wtech.totem.service.LeitorService;
import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.service.tef.TefClientMCLibrary;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class TelaFormaPagamentoController {

    @FXML private Label labelValorTotal;
    @FXML private AnchorPane logoContainer;
    @FXML private HBox cancelarContainer;
    @FXML private Button btnDebito;
    @FXML private Button btnCredito;
    @FXML private Button btnPix;

    private final TefClientMCLibrary tef = TefClientMCLibrary.INSTANCE;

    @Autowired private NavegacaoUtil navegaPara;
    @Autowired private LeitorService leitorService;
    @Autowired private FormaPagamentoService formaPagamentoService;
    @Autowired private PagamentoTEFService pagamentoTEFService;

    @FXML
    private void initialize() {
        ImageView imgLogo = (ImageView) logoContainer.lookup("#imgLogo");
        if (imgLogo != null) {
            imgLogo.setOnMouseClicked(this::handleLogoClick);
        }

        Button btnCancelar = (Button) cancelarContainer.lookup("#btnCancelar");
        if (btnCancelar != null) {
            btnCancelar.setOnAction(this::handleCancelar);
            btnCancelar.setFocusTraversable(false);
        }

        if (btnDebito != null) btnDebito.setFocusTraversable(false);
        if (btnCredito != null) btnCredito.setFocusTraversable(false);
        if (btnPix != null) btnPix.setFocusTraversable(false);

        labelValorTotal.setText(leitorService.getValorTotalFormatado()); // continua funcionando!
    }

    @FXML
    private void handleLogoClick(MouseEvent event) {
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("Voltando para a tela inicial");
        pagamentoTEFService.solicitarCancelamento();
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleDebito(ActionEvent event) {
        System.out.println("BOTÃO Débito: TESTANDO CENÁRIO DE RECUSA.");
        iniciarFluxoTef(2);
        pagamentoTEFService.simularProximoComoRecusado(true);
        formaPagamentoService.setFormaPagamento("Débito");
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }

    @FXML
    private void handleCredito(ActionEvent event) {
        System.out.println("BOTÃO Crédito: TESTANDO CENÁRIO DE SUCESSO.");
        iniciarFluxoTef(1);
        pagamentoTEFService.simularProximoComoRecusado(false);
        formaPagamentoService.setFormaPagamento("Crédito");
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }

    @FXML
    private void handlePix(ActionEvent event) {
        System.out.println("Pagamento com PIX selecionado.");
        formaPagamentoService.atualizarTipoPagamentoNoBanco(3);
        iniciarFluxoTef(500);
        formaPagamentoService.setFormaPagamento("Pix");
        navegaPara.trocaTela("/fxml/tela_forma_escolhida.fxml", (Node) event.getSource());
    }

    private void iniciarFluxoTef(int operacao) {
        try {
            String ticket = leitorService.getTicketAtual().getTicketCode();
            int cents     = leitorService.getVlFinal(); // em centavos
            String valor  = formatarValor(cents);

            String nsu  = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            int ret = tef.IniciaFuncaoMCInterativo(
                    operacao,
                    "60177876000130",
                    1,
                    ticket,
                    valor,
                    nsu,
                    data,
                    "1",
                    "167",
                    0,
                    ""
            );

            if (ret != 0) throw new RuntimeException("Erro IniciaFuncao: " + ret);

            String resp;
            while (true) {
                resp = tef.AguardaFuncaoMCInterativo();

                if (resp.startsWith("[MENU]")) {
                    String[] ops = resp.substring(6).split("\\|");
                    ChoiceDialog<String> dlg = new ChoiceDialog<>(ops[1], ops);
                    dlg.setTitle("TEF – Menu");
                    dlg.setHeaderText("Escolha opção");
                    String escolha = dlg.showAndWait()
                            .orElseThrow(() -> new RuntimeException("Operador cancelou"));
                    tef.ContinuaFuncaoMCInterativo(escolha);

                } else if (resp.startsWith("[PERGUNTA]")) {
                    String pergunta = resp.substring(10);
                    TextInputDialog dlg = new TextInputDialog();
                    dlg.setTitle("TEF – Pergunta");
                    dlg.setHeaderText(pergunta);
                    String ans = dlg.showAndWait()
                            .orElseThrow(() -> new RuntimeException("Operador cancelou"));
                    tef.ContinuaFuncaoMCInterativo(ans);

                } else if (resp.startsWith("[MSG]")) {
                    new Alert(Alert.AlertType.INFORMATION, resp.substring(5)).showAndWait();

                } else if (resp.startsWith("[ERROABORTAR]") || resp.startsWith("[ERRODISPLAY]")) {
                    tef.CancelarFluxoMCInterativo();
                    throw new RuntimeException("Erro TEF: " + resp);

                } else if (resp.startsWith("[RETORNO]")) {
                    // Aqui você poderia logar ou salvar o recibo, se quiser
                    tef.FinalizaFuncaoMCInterativo(
                            98,
                            "12345678000100",
                            1,
                            ticket,
                            valor,
                            nsu,
                            data,
                            "1",
                            "001",
                            0,
                            ""
                    );
                    break;
                }
            }

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private String formatarValor(int cents) {
        var bd  = java.math.BigDecimal.valueOf(cents, 2);
        var fmt = new java.text.DecimalFormat("0.00",
                new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
        return fmt.format(bd).replace('.', ',');
    }
}
