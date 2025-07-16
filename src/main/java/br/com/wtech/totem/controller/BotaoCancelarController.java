package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.PagamentoTEFService;
import br.com.wtech.totem.util.NavegacaoUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Este Controller gerencia a lógica APENAS para o componente do botão de cancelar.
 */
@Component
public class BotaoCancelarController {

    @Autowired
    private PagamentoTEFService pagamentoTEFService;

    @Autowired
    private NavegacaoUtil navegaPara;

    /**
     * Este método é chamado quando o botão dentro do componente é clicado.
     * Ele executa a lógica universal de cancelamento.
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("BOTÃO CANCELAR UNIVERSAL: Ação disparada.");

        // 1. Solicita o cancelamento de qualquer operação TEF em andamento.
        // Se nenhuma operação estiver ativa, o método no serviço simplesmente não fará nada prejudicial.
        if (pagamentoTEFService != null) {
            pagamentoTEFService.solicitarCancelamento();
        }

        // 2. Navega de volta para a tela inicial.
        // O Node de origem é o próprio botão, que nos dá o contexto da cena atual.
        Node source = (Node) event.getSource();
        navegaPara.trocaTela("/fxml/tela_inicial.fxml", source);
    }
}