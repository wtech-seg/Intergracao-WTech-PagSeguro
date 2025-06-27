package br.com.wtech.totem.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TelaInicialController {

    @FXML
    private GridPane root;

    @FXML
    private ImageView imgFundo;

    @FXML
    private Label labelHora;

    @FXML
    private Button btnIniciar;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        // Faz o fundo ocupar 100% da largura e 80% da altura
        imgFundo.fitWidthProperty().bind(root.widthProperty());
        imgFundo.fitHeightProperty().bind(root.heightProperty().multiply(0.8));
        // imgFundo.setPreserveRatio(true);

        iniciarRelogio();
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
        // trocar de tela aqui
    }
}
