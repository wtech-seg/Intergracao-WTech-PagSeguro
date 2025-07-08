package br.com.wtech.totem.controller;

import br.com.wtech.totem.service.LeitorService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlocoPermanenciaController {

    @FXML
    private Label lblTempo;

    @Autowired
    private LeitorService leitorService;

    @FXML
    private void initialize() {
        // Agora, basta pegar o valor já calculado e formatado do serviço.
        // Sem relógio, sem atualizações, apenas um valor fixo.
        lblTempo.setText(leitorService.getPermanenciaFormatada());
    }
}