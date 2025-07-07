package br.com.wtech.totem.controller;

import br.com.wtech.totem.util.NavegacaoUtil; // Importe o utilitário
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired; // Importe o Autowired
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class TelaInicialController {

    @FXML private GridPane root;
    @FXML private ImageView imgFundo;
    @FXML private Label labelHora;
    @FXML private Button btnIniciar;

    // 1. INJETE A DEPENDÊNCIA DO UTILITÁRIO DE NAVEGAÇÃO
    @Autowired
    private NavegacaoUtil navegaPara;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        imgFundo.fitWidthProperty().bind(root.widthProperty());
        imgFundo.fitHeightProperty().bind(root.heightProperty().multiply(0.8));
        iniciarRelogio();

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(evt -> {
                    if (evt.getCode() == KeyCode.ESCAPE) {
                        ((Stage) root.getScene().getWindow()).setIconified(true);
                    }
                });
            }
        });
    }

    private void iniciarRelogio() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> labelHora.setText(LocalTime.now().format(formatter))),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void handleIniciar() {
        System.out.println("Botão INICIAR pressionado!");

        // 2. USE O UTILITÁRIO PARA TROCAR DE TELA
        // Ele garante que o controller da próxima tela será gerenciado pelo Spring.
        navegaPara.trocaTela("/fxml/tela_leitor.fxml", btnIniciar);
    }
}